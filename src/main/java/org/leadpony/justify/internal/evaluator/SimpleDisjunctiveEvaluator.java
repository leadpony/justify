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
import java.util.stream.Collectors;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;
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
    private List<List<Problem>> problemLists;
    
    SimpleDisjunctiveEvaluator() {
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        for (DeferredEvaluator operand : operands) {
            Result result = operand.evaluate(event, parser, depth, dispatcher);
            if (result == Result.TRUE) {
                return Result.TRUE;
            } else {
                addBadEvaluator(operand);
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
        if (this.problemLists == null) {
            this.problemLists = new ArrayList<>();
        }
        problemLists.add(evaluator.problems());
    }
    
    protected Result dispatchProblems(JsonParser parser, ProblemDispatcher dispatcher) {
        if (problemLists == null) {
            dispatchDefaultProblem(parser, dispatcher);
        } else {
            assert !problemLists.isEmpty();
            dispatchProblemBranches(parser, dispatcher);
        }
        return Result.FALSE;
    }
    
    private void dispatchProblemBranches(JsonParser parser, ProblemDispatcher dispatcher) {
        List<List<Problem>> lists = this.problemLists.stream()
            .filter(SimpleDisjunctiveEvaluator::isResolvable)
            .collect(Collectors.toList());
        if (lists.isEmpty()) {
            List<Problem> firstList = this.problemLists.get(0);
            dispatchSingleBranch(firstList, dispatcher);
        } else if (lists.size() == 1) {
            dispatchSingleBranch(lists.get(0), dispatcher);
        } else {
            ProblemBuilder builder = createProblemBuilder(parser)
                    .withMessage("instance.problem.anyOf");
            lists.forEach(builder::withBranch);
            dispatcher.dispatchProblem(builder.build());
        }
    }
    
    private static boolean isResolvable(List<Problem> problems) {
        for (Problem problem : problems) {
            if (!problem.isResolvable()) {
                return false;
            }
        }
        return true;
    }
    
    private void dispatchSingleBranch(List<Problem> problems, ProblemDispatcher dispatcher) {
        for (Problem problem : problems) {
            dispatcher.dispatchProblem(problem);
        }
    }
    
    protected void dispatchDefaultProblem(JsonParser parser, ProblemDispatcher dispatcher) {
        throw new IllegalStateException();
    }
}
