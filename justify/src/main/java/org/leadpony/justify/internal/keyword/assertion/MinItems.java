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

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;
import org.leadpony.justify.internal.keyword.ArrayKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * Assertion specified with "minItems" validation keyword.
 *
 * @author leadpony
 */
public class MinItems extends AbstractAssertion implements ArrayKeyword {

    private final int limit;

    public MinItems(int limit) {
        this.limit = limit;
    }

    @Override
    public String name() {
        return "minItems";
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        return new AssertionEvaluator(context, limit, this);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        if (limit > 0) {
            return new MaxItems.AssertionEvaluator(context, limit - 1, this);
        } else {
            return createAlwaysFalseEvaluator(context);
        }
    }

    @Override
    public JsonValue getValueAsJson(JsonProvider jsonProvider) {
        return jsonProvider.createValue(limit);
    }

    /**
     * An evaluator of this keyword.
     *
     * @author leadpony
     */
    static class AssertionEvaluator extends ShallowEvaluator {

        private final int minItems;
        private final ProblemBuilderFactory factory;
        private int currentCount;

        AssertionEvaluator(EvaluatorContext context, int minItems, ProblemBuilderFactory factory) {
            super(context);
            this.minItems = minItems;
            this.factory = factory;
        }

        @Override
        public Result evaluateShallow(Event event, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1) {
                if (ParserEvents.isValue(event)) {
                    if (++currentCount >= minItems) {
                        return Result.TRUE;
                    }
                }
            } else if (depth == 0 && event == Event.END_ARRAY) {
                if (currentCount >= minItems) {
                    return Result.TRUE;
                } else {
                    Problem p = factory.createProblemBuilder(getContext())
                            .withMessage(Message.INSTANCE_PROBLEM_MINITEMS)
                            .withParameter("actual", currentCount)
                            .withParameter("limit", minItems)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            }
            return Result.PENDING;
        }
    }
}
