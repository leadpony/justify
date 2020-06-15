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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBranch;

/**
 * Evaluator for "anyOf" boolean logic.
 *
 * @author leadpony
 */
class SimpleDisjunctiveEvaluator extends AbstractLogicalEvaluator
    implements Iterable<DeferredEvaluator> {

    private final List<DeferredEvaluator> operands = new ArrayList<>();
    private List<ProblemBranch> problemBranches;

    SimpleDisjunctiveEvaluator(Evaluator parent, Keyword keyword) {
        super(parent, keyword);
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        for (DeferredEvaluator operand : operands) {
            Result result = operand.evaluate(event, depth, dispatcher);
            if (result == Result.TRUE) {
                return Result.TRUE;
            } else {
                addBadEvaluator(operand);
            }
        }
        return dispatchProblems(dispatcher);
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
        if (this.problemBranches == null) {
            this.problemBranches = new ArrayList<>();
        }
        problemBranches.add(evaluator.problems());
    }

    protected Result dispatchProblems(ProblemDispatcher dispatcher) {
        if (problemBranches == null) {
            dispatchDefaultProblem(dispatcher);
        } else {
            assert !problemBranches.isEmpty();
            dispatchProblemBranches(dispatcher);
        }
        return Result.FALSE;
    }

    private void dispatchProblemBranches(ProblemDispatcher dispatcher) {
        List<ProblemBranch> filteredBranches = this.problemBranches.stream()
            .filter(ProblemBranch::isResolvable)
            .collect(Collectors.toList());
        if (filteredBranches.isEmpty()) {
            filteredBranches = this.problemBranches;
        }
        ProblemBuilder builder = newProblemBuilder()
                .withMessage(getMessage())
                .withBranches(filteredBranches);
        dispatcher.dispatchProblem(builder.build());
    }

    /**
     * Returns the problem message.
     *
     * @return the problem message.
     */
    protected Message getMessage() {
        return Message.INSTANCE_PROBLEM_ANYOF;
    }

    protected void dispatchDefaultProblem(ProblemDispatcher dispatcher) {
        throw new IllegalStateException();
    }
}
