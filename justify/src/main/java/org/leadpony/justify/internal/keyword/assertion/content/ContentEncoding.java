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
package org.leadpony.justify.internal.keyword.assertion.content;

import java.util.EnumSet;
import java.util.Set;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.spi.ContentEncodingScheme;

/**
 * A content keyword representing "contentEncoding".
 *
 * @author leadpony
 */
@KeywordType("contentEncoding")
@Spec(SpecVersion.DRAFT_07)
public class ContentEncoding extends AbstractAssertionKeyword {

    private final ContentEncodingScheme scheme;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.STRING) {
                final String name = ((JsonString) value).getString();
                ContentEncodingScheme scheme = context.getEncodingScheme(name);
                if (scheme != null) {
                    return new ContentEncoding(value, scheme);
                } else {
                    return new UnknownContentEncoding(value, name);
                }
            } else {
                throw new IllegalArgumentException();
            }
        };
    }

    /**
     * Constructs this encoding.
     *
     * @param json the original JSON value.
     * @param scheme the scheme of this encoding.
     */
    public ContentEncoding(JsonValue json, ContentEncodingScheme scheme) {
        super(json);
        assert scheme != null;
        this.scheme = scheme;
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
    protected Evaluator doCreateEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
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
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
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

    abstract class ContentEncodingEvaluator extends AbstractKeywordEvaluator {

        ContentEncodingEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword) {
            super(context, schema, keyword);
        }

        @Override
        protected ProblemBuilder newProblemBuilder() {
            return super.newProblemBuilder().withParameter("encoding", scheme.name());
        }
    }
}
