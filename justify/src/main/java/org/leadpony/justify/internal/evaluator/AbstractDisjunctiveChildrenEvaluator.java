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

package org.leadpony.justify.internal.evaluator;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;

/**
 * @author leadpony
 */
abstract class AbstractDisjunctiveChildrenEvaluator extends DisjunctiveEvaluator implements ChildrenEvaluator {

    protected AbstractDisjunctiveChildrenEvaluator(Evaluator parent, Keyword keyword,
            Event closingEvent) {
        super(parent, keyword, closingEvent);
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        if (depth == 1) {
            updateChildren(event, getParser());
        }
        return super.evaluate(event, depth, dispatcher);
    }

    @Override
    protected Result invokeOperandEvaluators(Event event, int depth, ProblemDispatcher dispatcher) {
        if (depth > 0) {
            return super.invokeOperandEvaluators(event, depth - 1, dispatcher);
        }
        return Result.PENDING;
    }
}
