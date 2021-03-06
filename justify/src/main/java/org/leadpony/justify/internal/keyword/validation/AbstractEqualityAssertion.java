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

package org.leadpony.justify.internal.keyword.validation;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.json.JsonInstanceBuilder;
import org.leadpony.justify.internal.evaluator.AbstractKeywordBasedEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * The base class of {@link Const} and {@link Enum}.
 *
 * @author leadpony
 */
abstract class AbstractEqualityAssertion extends AbstractAssertionKeyword {

    protected AbstractEqualityAssertion(JsonValue json) {
        super(json);
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        EvaluatorContext context = parent.getContext();
        JsonBuilderFactory jsonBuilderFactory = context.getJsonBuilderFactory();
        JsonInstanceBuilder builder = new JsonInstanceBuilder(jsonBuilderFactory);
        return new AbstractKeywordBasedEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth) {
                if (builder.append(event, context.getParser())) {
                    return Result.PENDING;
                }
                JsonValue value = builder.build();
                if (testValue(value)) {
                    return Result.TRUE;
                }
                ProblemBuilder problemBuilder = newProblemBuilder()
                        .withParameter("actual", value);
                getDispatcher().dispatchProblem(createProblem(problemBuilder));
                return Result.FALSE;
            }
        };
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        EvaluatorContext context = parent.getContext();
        JsonBuilderFactory jsonBuilderFactory = context.getJsonBuilderFactory();
        JsonInstanceBuilder builder = new JsonInstanceBuilder(jsonBuilderFactory);
        return new AbstractKeywordBasedEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth) {
                if (builder.append(event, context.getParser())) {
                    return Result.PENDING;
                }
                JsonValue value = builder.build();
                if (!testValue(value)) {
                    return Result.TRUE;
                }
                ProblemBuilder problemBuilder = newProblemBuilder()
                        .withParameter("actual", value);
                getDispatcher().dispatchProblem(createNegatedProblem(problemBuilder));
                return Result.FALSE;
            }
        };
    }

    protected abstract boolean testValue(JsonValue value);

    protected abstract Problem createProblem(ProblemBuilder builder);

    protected abstract Problem createNegatedProblem(ProblemBuilder builder);
}
