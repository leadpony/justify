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

import java.util.Iterator;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.ProblemDispatcher;

/**
 * @author leadpony
 */
class DisjunctiveEvaluator extends SimpleDisjunctiveEvaluator {

    private final Event closingEvent;

    DisjunctiveEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword, Event closingEvent) {
        super(context, schema, keyword);
        this.closingEvent = closingEvent;
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        if (invokeOperandEvaluators(event, depth, dispatcher) == Result.TRUE) {
            return Result.TRUE;
        }
        if (depth == 0 && event == closingEvent) {
            return dispatchProblems(dispatcher);
        }
        return Result.PENDING;
    }

    protected Result invokeOperandEvaluators(Event event, int depth, ProblemDispatcher dispatcher) {
        Iterator<DeferredEvaluator> it = iterator();
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            Result result = current.evaluate(event, depth, dispatcher);
            if (result == Result.TRUE) {
                return Result.TRUE;
            } else if (result != Result.PENDING) {
                if (result == Result.FALSE) {
                    addBadEvaluator(current);
                }
                it.remove();
            }
        }
        return Result.PENDING;
    }
}
