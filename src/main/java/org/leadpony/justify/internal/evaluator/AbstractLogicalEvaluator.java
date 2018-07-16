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
import java.util.Objects;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;

/**
 * @author leadpony
 */
abstract class AbstractLogicalEvaluator implements LogicalEvaluator, LogicalEvaluator.Builder {

    protected final List<Evaluator> evaluators;
    
    protected AbstractLogicalEvaluator() {
        this.evaluators = new LinkedList<>();
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
        Iterator<Evaluator> it = evaluators.iterator();
        while (it.hasNext()) {
            Evaluator evaluator = it.next();
            Result result = invokeChildEvaluator(evaluator, event, parser, depth, reporter);
            if (result != Result.PENDING) {
                it.remove();
                if (!accumulateResult(evaluator, result)) {
                    return conclude(parser, reporter);
                }
            }
        }
        return tryToMakeDecision(event, parser, depth, reporter);
    }
   
    @Override
    public void append(Evaluator evaluator) {
        Objects.requireNonNull(evaluator, "evaluator must not be null.");
        this.evaluators.add(evaluator);
    }
    
    @Override
    public Evaluator build() {
        if (evaluators.isEmpty()) {
            return Evaluator.ALWAYS_TRUE;
        } else if (evaluators.size() == 1) {
            return evaluators.get(0);
        } else {
            return this;
        }
    }
    
    protected boolean isEmpty() {
        return evaluators.isEmpty();
    }
    
    protected Result invokeChildEvaluator(Evaluator evaluator, Event event, JsonParser parser, int depth, Reporter reporter) {
        return evaluator.evaluate(event, parser, depth, reporter);
    }

    protected Result tryToMakeDecision(Event event, JsonParser parser, int depth, Reporter reporter) {
        assert isEmpty();
        return conclude(parser, reporter);
    }
    
    protected abstract boolean accumulateResult(Evaluator evaluator, Result result);
    
    protected abstract Result conclude(JsonParser parser, Reporter reporter);
}
