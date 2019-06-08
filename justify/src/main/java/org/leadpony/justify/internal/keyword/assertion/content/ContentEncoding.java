/*
 * Copyright 2018-2019 the Justify authors.
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

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.assertion.AbstractAssertion;
import org.leadpony.justify.spi.ContentEncodingScheme;

/**
 * A content keyword representing "contentEncoding".
 *
 * @author leadpony
 */
public class ContentEncoding extends AbstractAssertion {

    private final ContentEncodingScheme scheme;

    /**
     * Constructs this encoding.
     *
     * @param scheme the scheme of this encoding.
     */
    public ContentEncoding(ContentEncodingScheme scheme) {
        assert scheme != null;
        this.scheme = scheme;
    }

    @Override
    public String name() {
        return "contentEncoding";
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
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        if (test(context.getParser().getString())) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new Evaluator() {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                dispatcher.dispatchProblem(
                        buildProblem(context, Message.INSTANCE_PROBLEM_CONTENTENCODING));
                return Result.FALSE;
            }
        };
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        if (!test(context.getParser().getString())) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new Evaluator() {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                dispatcher.dispatchProblem(
                        buildProblem(context, Message.INSTANCE_PROBLEM_NOT_CONTENTENCODING));
                return Result.FALSE;
            }
        };
    }

    @Override
    public JsonValue getValueAsJson(JsonProvider jsonProvider) {
        return jsonProvider.createValue(scheme.name());
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

    private Problem buildProblem(EvaluatorContext context, Message message) {
        return createProblemBuilder(context).withMessage(message).withParameter("encoding", scheme.name()).build();
    }
}
