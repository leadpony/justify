/*
 * Copyright 2018-2020 the Justify authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.leadpony.justify.internal.keyword.format;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser.Event;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.InvalidKeywordException;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.SubschemaParser;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordBasedEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * An assertion representing "format" keyword.
 *
 * @author leadpony
 */
@KeywordClass("format")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Format extends AbstractAssertionKeyword implements FormatKeyword {

    /**
     * A keyword type for "type" keyword.
     *
     * @author leadpony
     */
    static class FormatType implements KeywordType {

        private final Map<String, FormatAttribute> attributeMap;

        FormatType() {
            this(Collections.emptyMap());
        }

        FormatType(Map<String, FormatAttribute> attributeMap) {
            this.attributeMap = attributeMap;
        }

        @Override
        public String name() {
            return "format";
        }

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() != ValueType.STRING) {
                throw new InvalidKeywordException("Must be a string");
            }
            JsonString string = (JsonString) jsonValue;
            return createFormat(jsonValue, string.getString());
        }

        private Keyword createFormat(JsonValue jsonValue, String name) {
            FormatAttribute attribute = attributeMap.get(name);
            if (attribute == null) {
                throw new InvalidFormatException("Unknown format attribute", name);
            }
            return new Format(jsonValue, attribute);
        }
    }

    static final KeywordType TYPE = new FormatType();

    private final FormatAttribute attribute;

    public Format(JsonValue json, FormatAttribute attribute) {
        super(json);
        this.attribute = attribute;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean supportsType(InstanceType type) {
        return type == attribute.valueType();
    }

    @Override
    public Set<InstanceType> getSupportedTypes() {
        return EnumSet.of(attribute.valueType());
    }

    @Override
    public boolean canEvaluate() {
        return true;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        JsonValue value = parent.getContext().getParser().getValue();
        if (test(value)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new FormatEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth) {
                ProblemBuilder builder = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_FORMAT);
                getDispatcher().dispatchProblem(builder.build());
                return Result.FALSE;
            }
        };
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        JsonValue value = parent.getContext().getParser().getValue();
        if (!test(value)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new FormatEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth) {
                ProblemBuilder builder = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_FORMAT);
                getDispatcher().dispatchProblem(builder.build());
                return Result.FALSE;
            }
        };
    }

    @Override
    public String getAttributeName() {
        return attribute.name();
    }

    private boolean test(JsonValue value) {
        return attribute.test(value);
    }

    abstract class FormatEvaluator extends AbstractKeywordBasedEvaluator {

        FormatEvaluator(Evaluator parent, Keyword keyword) {
            super(parent, keyword);
        }

        @Override
        protected ProblemBuilder newProblemBuilder() {
            return super.newProblemBuilder()
                .withParameter("attribute", attribute.name())
                .withParameter("localizedAttribute", attribute.localizedName());
        }
    }
}
