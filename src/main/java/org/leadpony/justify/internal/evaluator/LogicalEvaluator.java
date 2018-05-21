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
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
abstract class LogicalEvaluator implements AppendableEvaluator {

    private final List<Evaluator> evaluators;
    private final EndCondition endCondition;
    
    protected LogicalEvaluator(LogicalCombiner combiner) {
        this.evaluators = combiner.evaluators();
        this.endCondition = combiner.endCondition();
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        Iterator<Evaluator> it = evaluators.iterator();
        while (it.hasNext()) {
            Evaluator evaluator = it.next();
            Result result = evaluate(evaluator, event, parser, depth, consumer);
            if (result != Result.PENDING) {
                it.remove();
                if (!accumulateResult(result)) {
                    return getFinalResult(consumer);
                }
            }
        }
        return hasCompleted(event, depth) ?
                getFinalResult(consumer) : Result.PENDING;
    }
    
    @Override
    public void append(Evaluator other) {
        this.evaluators.add(other);
    }
    
    protected Result evaluate(Evaluator evaluator, Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        return evaluator.evaluate(event, parser, depth, consumer);
    }
    
    protected boolean isEmpty() {
        return evaluators.isEmpty();
    }
    
    protected boolean hasCompleted(Event event, int depth) {
        return endCondition.test(event, depth, isEmpty());
    }
    
    protected abstract boolean accumulateResult(Result result);
    
    protected abstract Result getFinalResult(Consumer<Problem> consumer);
}
