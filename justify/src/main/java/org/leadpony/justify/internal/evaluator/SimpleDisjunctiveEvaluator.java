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

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemList;

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
        List<ProblemList> filterdLists = this.problemLists.stream()
            .filter(ProblemList::isResolvable)
            .collect(Collectors.toList());
        if (filterdLists.isEmpty()) {
            filterdLists = this.problemLists;
        }
        if (filterdLists.size() == 1) {
            dispatcher.dispatchAllProblems(filterdLists.get(0));
        } else {
            ProblemBuilder builder = createProblemBuilder(parser)
                    .withMessage("instance.problem.anyOf")
                    .withBranches(filterdLists);
            dispatcher.dispatchProblem(builder.build());
        }
    }

    protected void dispatchDefaultProblem(JsonParser parser, ProblemDispatcher dispatcher) {
        throw new IllegalStateException();
    }
}
