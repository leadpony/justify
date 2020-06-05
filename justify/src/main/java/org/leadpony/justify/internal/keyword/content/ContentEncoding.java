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
package org.leadpony.justify.internal.keyword.content;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordAwareEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.spi.ContentEncodingScheme;

/**
 * A content keyword representing "contentEncoding".
 *
 * @author leadpony
 */
@KeywordClass("contentEncoding")
@Spec(SpecVersion.DRAFT_07)
public class ContentEncoding extends AbstractAssertionKeyword {

    static class ContentEncodingType implements KeywordType {

        private final Map<String, ContentEncodingScheme> schemes;

        ContentEncodingType(ContentEncodingScheme... schemes) {
            this(toMap(new HashMap<>(), Arrays.asList(schemes)));
        }

        private ContentEncodingType(Map<String, ContentEncodingScheme> schemes) {
            this.schemes = schemes;
        }

        ContentEncodingType withSchemes(Collection<ContentEncodingScheme> schemes) {
            if (schemes.isEmpty()) {
                return this;
            } else {
                Map<String, ContentEncodingScheme> newMap = toMap(new HashMap<>(this.schemes), schemes);
                return new ContentEncodingType(newMap);
            }
        }

        @Override
        public String name() {
            return "contentEncoding";
        }

        @Override
        public Keyword parse(JsonValue jsonValue) {
            if (jsonValue.getValueType() == ValueType.STRING) {
                final String name = ((JsonString) jsonValue).getString();
                return createKeyword(jsonValue, name);
            } else {
                throw new IllegalArgumentException();
            }
        }

        private Keyword createKeyword(JsonValue jsonValue, String name) {
            ContentEncodingScheme scheme = schemes.get(name);
            if (scheme != null) {
                return new ContentEncoding(jsonValue, scheme);
            } else {
                return new UnknownContentEncoding(jsonValue, name);
            }
        }

        private static Map<String, ContentEncodingScheme> toMap(Map<String, ContentEncodingScheme> map,
                Collection<ContentEncodingScheme> schemes) {
            for (ContentEncodingScheme scheme : schemes) {
                map.put(scheme.name().toLowerCase(), scheme);
            }
            return map;
        }
    }

    static final ContentEncodingType TYPE = new ContentEncodingType(Base64.INSTANCE);

    private final ContentEncodingScheme scheme;

    /**
     * Constructs this encoding.
     *
     * @param json   the original JSON value.
     * @param scheme the scheme of this encoding.
     */
    public ContentEncoding(JsonValue json, ContentEncodingScheme scheme) {
        super(json);
        assert scheme != null;
        this.scheme = scheme;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean supportsType(InstanceType type) {
        return type == InstanceType.STRING;
    }

    @Override
    public Set<InstanceType> getSupportedTypes() {
        return EnumSet.of(InstanceType.STRING);
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, InstanceType type, ObjectJsonSchema schema) {
        if (test(context.getParser().getString())) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new ContentEncodingEvaluator(context, schema, this) {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                Problem p = newProblemBuilder().withMessage(Message.INSTANCE_PROBLEM_CONTENTENCODING).build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            }
        };
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type, ObjectJsonSchema schema) {
        if (!test(context.getParser().getString())) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new ContentEncodingEvaluator(context, schema, this) {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                Problem p = newProblemBuilder().withMessage(Message.INSTANCE_PROBLEM_NOT_CONTENTENCODING).build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            }
        };
    }

    private boolean test(String src) {
        return scheme.canDecode(src);
    }

    /**
     * Returns the scheme of this content encoding.
     *
     * @return the scheme of this content encoding.
     */
    ContentEncodingScheme scheme() {
        return scheme;
    }

    abstract class ContentEncodingEvaluator extends AbstractKeywordAwareEvaluator {

        ContentEncodingEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword) {
            super(context, schema, keyword);
        }

        @Override
        protected ProblemBuilder newProblemBuilder() {
            return super.newProblemBuilder().withParameter("encoding", scheme.name());
        }
    }
}
