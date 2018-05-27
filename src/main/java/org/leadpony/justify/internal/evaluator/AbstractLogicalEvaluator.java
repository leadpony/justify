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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
abstract class AbstractLogicalEvaluator implements LogicalEvaluator {

    protected final List<Evaluator> evaluators;
    
    protected AbstractLogicalEvaluator() {
        this.evaluators = new LinkedList<>();
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        Iterator<Evaluator> it = evaluators.iterator();
        while (it.hasNext()) {
            Evaluator evaluator = it.next();
            Result result = evaluator.evaluate(event, parser, depth, consumer);
            if (result != Result.PENDING) {
                it.remove();
                if (!accumulateResult(evaluator, result)) {
                    return conclude(consumer);
                }
            }
        }
        return tryToMakeDecision(event, depth, consumer);
    }
   
    @Override
    public void append(Evaluator evaluator) {
        this.evaluators.add(evaluator);
    }
    
    protected boolean isEmpty() {
        return evaluators.isEmpty();
    }

    protected Result tryToMakeDecision(Event event, int depth, Consumer<Problem> consumer) {
        assert isEmpty();
        return conclude(consumer);
    }
    
    protected abstract boolean accumulateResult(Evaluator evaluator, Result result);
    
    protected abstract Result conclude(Consumer<Problem> consumer);
}
