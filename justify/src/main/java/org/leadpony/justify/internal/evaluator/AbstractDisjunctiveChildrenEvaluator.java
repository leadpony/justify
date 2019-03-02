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

package org.leadpony.justify.internal.evaluator;

import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * @author leadpony
 */
abstract class AbstractDisjunctiveChildrenEvaluator extends DisjunctiveEvaluator implements ChildrenEvaluator {

    protected AbstractDisjunctiveChildrenEvaluator(InstanceType type, ProblemBuilderFactory problemBuilderFactory) {
        super(type);
        withProblemBuilderFactory(problemBuilderFactory);
    }

    @Override
    public Result evaluate(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
        if (depth == 1) {
            updateChildren(event, context.getParser());
        }
        return super.evaluate(event, context, depth, dispatcher);
    }

    @Override
    protected Result invokeOperandEvaluators(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
        if (depth > 0) {
            return super.invokeOperandEvaluators(event, context, depth - 1, dispatcher);
        }
        return Result.PENDING;
    }
}
