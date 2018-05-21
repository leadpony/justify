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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * Skeletal implementation of {@link Combiner}.
 * 
 * @author leadpony
 */
abstract class LogicalCombiner implements Combiner {
    
    private final List<Evaluator> entries = new LinkedList<>();
    private EndCondition endCondition = EndCondition.DEFAULT;
    
    @Override
    public Combiner append(Evaluator entry) {
        Objects.requireNonNull(entry, "entry must not be null.");
        this.entries.add(entry);
        return this;
    }
    
    @Override
    public Combiner withEndCondition(EndCondition condition) {
        this.endCondition = condition;
        return this;
    }
    
    @Override
    public Optional<Evaluator> getCombined() {
        if (this.entries.isEmpty()) {
            return Optional.empty();
        } else if (this.entries.size() == 1) {
            Evaluator evaluator = this.entries.get(0);
            if (endCondition != EndCondition.DEFAULT) {
                evaluator = new SingleEvaluator(evaluator, endCondition);
            }
            return Optional.of(evaluator);
        } else {
            return Optional.of(getAppendable());
        }
    }
    
    List<Evaluator> evaluators() {
        return entries;
    }
    
    EndCondition endCondition() {
        return endCondition;
    }
    
    private static class SingleEvaluator implements Evaluator {
        
        private final Evaluator evaluator;
        private final EndCondition endCondition;
        
        private SingleEvaluator(Evaluator evaluator, EndCondition endCondition) {
            this.evaluator = evaluator;
            this.endCondition = endCondition;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            Result result = evaluator.evaluate(event, parser, depth, consumer);
            if (result == Result.TRUE || result == Result.FALSE) {
                return result;
            }
            return endCondition.test(event, depth, false) ? Result.TRUE : Result.PENDING;
        }
    }
}
