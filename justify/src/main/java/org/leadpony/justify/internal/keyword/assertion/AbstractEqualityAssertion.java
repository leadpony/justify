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

package org.leadpony.justify.internal.keyword.assertion;

import javax.json.JsonBuilderFactory;
import javax.json.JsonValue;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.Evaluator.Result;
import org.leadpony.justify.internal.base.json.JsonInstanceBuilder;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * The base class of {@link Const} and {@link Enum}.
 *
 * @author leadpony
 */
abstract class AbstractEqualityAssertion extends AbstractAssertion {

    protected AbstractEqualityAssertion(JsonValue json) {
        super(json);
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        JsonBuilderFactory jsonBuilderFactory = context.getJsonBuilderFactory();
        JsonInstanceBuilder builder = new JsonInstanceBuilder(jsonBuilderFactory);
        return (event, depth, dispatcher) -> {
            if (builder.append(event, context.getParser())) {
                return Result.PENDING;
            }
            JsonValue value = builder.build();
            if (testValue(value)) {
                return Result.TRUE;
            }
            ProblemBuilder problemBuilder = createProblemBuilder(context)
                    .withParameter("actual", value);
            dispatcher.dispatchProblem(createProblem(problemBuilder));
            return Result.FALSE;
        };
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        JsonBuilderFactory jsonBuilderFactory = context.getJsonBuilderFactory();
        JsonInstanceBuilder builder = new JsonInstanceBuilder(jsonBuilderFactory);
        return (event, depth, dispatcher) -> {
            if (builder.append(event, context.getParser())) {
                return Result.PENDING;
            }
            JsonValue value = builder.build();
            if (!testValue(value)) {
                return Result.TRUE;
            }
            ProblemBuilder problemBuilder = createProblemBuilder(context)
                    .withParameter("actual", value);
            dispatcher.dispatchProblem(createNegatedProblem(problemBuilder));
            return Result.FALSE;
        };
    }

    protected abstract boolean testValue(JsonValue value);

    protected abstract Problem createProblem(ProblemBuilder builder);

    protected abstract Problem createNegatedProblem(ProblemBuilder builder);
}
