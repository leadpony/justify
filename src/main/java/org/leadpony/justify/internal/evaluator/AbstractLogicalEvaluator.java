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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * @author leadpony
 */
abstract class AbstractLogicalEvaluator implements LogicalEvaluator {

    protected final List<Evaluator> children;
    protected final Event stopEvent;
    
    protected AbstractLogicalEvaluator(List<Evaluator> children) {
        this(children, null);
    }
    
    protected AbstractLogicalEvaluator(List<Evaluator> children, Event stopEvent) {
        this.children = children;
        this.stopEvent = stopEvent;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        Iterator<Evaluator> it = children.iterator();
        while (it.hasNext()) {
            Evaluator evaluator = it.next();
            Result result = invokeChildEvaluator(evaluator, event, parser, depth, reporter);
            if (result != Result.PENDING) {
                it.remove();
                if (!accumulateResult(evaluator, result)) {
                    return getFinalResult(parser, reporter);
                }
            }
        }
        return tryToMakeDecision(event, parser, depth, reporter);
    }
   
    protected boolean isEmpty() {
        return children.isEmpty();
    }
    
    protected Result invokeChildEvaluator(Evaluator evaluator, Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        return evaluator.evaluate(event, parser, depth, reporter);
    }

    protected Result tryToMakeDecision(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        if (stopEvent == null) {
            assert isEmpty();
            return getFinalResult(parser, reporter);
        } else if (isEmpty()) {
            return getFinalResult(parser, reporter);
        } else if (depth == 0 && event == stopEvent) {
            assert false;
            return getFinalResult(parser, reporter);
        } else {
            return Result.PENDING;
        }
    }
    
    protected abstract boolean accumulateResult(Evaluator evaluator, Result result);
    
    protected abstract Result getFinalResult(JsonParser parser, Consumer<Problem> reporter);
    
    protected static abstract class Builder implements LogicalEvaluator.Builder {
        
        private final InstanceType type;
        private final List<Evaluator> children = new ArrayList<>();
        private ProblemBuilderFactory problemBuilderFactory = ProblemBuilderFactory.DEFAULT;
        
        protected Builder(InstanceType type) {
            this.type = type;
        }
        
        @Override
        public void append(Evaluator evaluator) {
            requireNonNull(evaluator, "evaluator");
            this.children.add(evaluator);
        }
        
        @Override
        public Builder withProblemBuilderFactory(ProblemBuilderFactory problemBuilderFactory) {
            this.problemBuilderFactory = problemBuilderFactory;
            return this;
        }
        
        @Override
        public Evaluator build() {
            Event stopEvent = ParserEvents.lastEventOf(type);
            if (stopEvent == null && children.size() == 1) {
                return children.get(0);
            } else {
                return createEvaluator(children, stopEvent, problemBuilderFactory);
            }
        }
        
        protected abstract LogicalEvaluator createEvaluator(
                List<Evaluator> children, Event stopEvent, ProblemBuilderFactory problemBuilderFactory);
    }
}
