/*
 * Copyright 2018-2019 the Justify authors.
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

import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemList;

/**
 * Evaluator for "anyOf" boolean logic.
 *
 * @author leadpony
 */
class SimpleDisjunctiveEvaluator extends AbstractLogicalEvaluator
    implements Iterable<DeferredEvaluator> {

    private final List<DeferredEvaluator> operands = new ArrayList<>();
    private List<ProblemList> problemLists;

    SimpleDisjunctiveEvaluator() {
    }

    @Override
    public Result evaluate(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
        for (DeferredEvaluator operand : operands) {
            Result result = operand.evaluate(event, context, depth, dispatcher);
            if (result == Result.TRUE) {
                return Result.TRUE;
            } else {
                addBadEvaluator(operand);
            }
        }
        return dispatchProblems(context, dispatcher);
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

    protected Result dispatchProblems(EvaluatorContext context, ProblemDispatcher dispatcher) {
        if (problemLists == null) {
            dispatchDefaultProblem(context, dispatcher);
        } else {
            assert !problemLists.isEmpty();
            dispatchProblemBranches(context, dispatcher);
        }
        return Result.FALSE;
    }

    private void dispatchProblemBranches(EvaluatorContext context, ProblemDispatcher dispatcher) {
        List<ProblemList> filterdLists = this.problemLists.stream()
            .filter(ProblemList::isResolvable)
            .collect(Collectors.toList());
        if (filterdLists.isEmpty()) {
            filterdLists = this.problemLists;
        }
        if (filterdLists.size() == 1) {
            dispatcher.dispatchAllProblems(filterdLists.get(0));
        } else {
            ProblemBuilder builder = createProblemBuilder(context)
                    .withMessage("instance.problem.anyOf")
                    .withBranches(filterdLists);
            dispatcher.dispatchProblem(builder.build());
        }
    }

    protected void dispatchDefaultProblem(EvaluatorContext context, ProblemDispatcher dispatcher) {
        throw new IllegalStateException();
    }
}
