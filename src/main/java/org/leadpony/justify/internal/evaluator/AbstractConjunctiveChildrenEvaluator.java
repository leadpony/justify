/*
 * Copyright 2018 the Justify authors.
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

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * @author leadpony
 */
abstract class AbstractConjunctiveChildrenEvaluator extends ConjunctiveEvaluator implements ChildrenEvaluator {
    
    protected AbstractConjunctiveChildrenEvaluator(InstanceType type, ProblemBuilderFactory problemBuilderFactory) {
        super(type);
        withProblemBuilderFactory(problemBuilderFactory);
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        if (depth == 1) {
            updateChildren(event, parser);
        }
        return super.evaluate(event, parser, depth, dispatcher);
    }

    @Override
    protected Result invokeOperandEvaluators(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        if (depth > 0) {
            return super.invokeOperandEvaluators(event, parser, depth - 1, dispatcher);
        }
        return Result.PENDING;
    }
}
