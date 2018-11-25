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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Evaluator for "anyOf" boolean logic.
 * 
 * @author leadpony
 */
class SimpleDisjunctiveEvaluator extends AbstractLogicalEvaluator 
    implements AppendableLogicalEvaluator, Iterable<DeferredEvaluator> {
    
    private final List<DeferredEvaluator> operands = new ArrayList<>();
    private List<DeferredEvaluator> badEvaluators;
    
    SimpleDisjunctiveEvaluator() {
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        for (DeferredEvaluator child : operands) {
            Result result = child.evaluate(event, parser, depth, dispatcher);
            if (result == Result.TRUE) {
                return Result.TRUE;
            } else {
                addBadEvaluator(child);
            }
        }
        return dispatchProblems(parser, dispatcher);
    }

    @Override
    public void append(Evaluator evaluator) {
        this.operands.add(new DeferredEvaluator(evaluator));
    }
    
    @Override
    public Iterator<DeferredEvaluator> iterator() {
        return operands.iterator();
    }

    protected void addBadEvaluator(DeferredEvaluator evaluator) {
        if (this.badEvaluators == null) {
            this.badEvaluators = new ArrayList<>();
        }
        badEvaluators.add(evaluator);
    }
    
    protected Result dispatchProblems(JsonParser parser, ProblemDispatcher dispatcher) {
        final int count = (badEvaluators != null) ? badEvaluators.size() : 0;
        if (count == 1) {
            badEvaluators.get(0).problems().forEach(dispatcher::dispatchProblem);
        } else if (count > 1) {
            ProblemBuilder builder = createProblemBuilder(parser)
                    .withMessage("instance.problem.anyOf");
            badEvaluators.stream()
                .map(DeferredEvaluator::problems)
                .filter(Objects::nonNull)
                .forEach(builder::withBranch);
            dispatcher.dispatchProblem(builder.build());
        } else {
            dispatchDefaultProblem(parser, dispatcher);
        }
        return Result.FALSE;
    }
    
    protected void dispatchDefaultProblem(JsonParser parser, ProblemDispatcher dispatcher) {
        throw new IllegalStateException();
    }
}