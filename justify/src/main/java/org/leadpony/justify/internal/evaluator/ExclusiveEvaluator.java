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
import java.util.stream.Stream;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.problem.ProblemList;

/**
 * Evaluator for "oneOf" boolean logic.
 *
 * @author leadpony
 */
class ExclusiveEvaluator extends AbstractExclusiveEvaluator {

    private final List<DeferredEvaluator> operands;
    private final List<DeferredEvaluator> negated;
    private List<ProblemList> problemLists;
    private List<ProblemList> negatedProblemLists;
    private long evaluationsAsTrue;
    private final Event closingEvent;

    ExclusiveEvaluator(EvaluatorContext context, Event closingEvent, Stream<Evaluator> operands,
            Stream<Evaluator> negated) {
        super(context);
        this.operands = createEvaluators(operands);
        this.negated = createEvaluators(negated);
        this.closingEvent = closingEvent;
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        if (evaluationsAsTrue <= 1) {
            evaluateAll(event, depth, dispatcher);
        }
        evaluateAllNegated(event, depth, dispatcher);
        if (depth == 0 && event == closingEvent) {
            if (evaluationsAsTrue == 0) {
                dispatchProblems(dispatcher, problemLists);
                return Result.FALSE;
            } else if (evaluationsAsTrue > 1) {
                dispatchNegatedProblems(dispatcher, negatedProblemLists);
                return Result.FALSE;
            }
            return Result.TRUE;
        }
        return Result.PENDING;
    }

    private void evaluateAll(Event event, int depth, ProblemDispatcher dispatcher) {
        Iterator<DeferredEvaluator> it = operands.iterator();
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            Result result = current.evaluate(event, depth, dispatcher);
            if (result != Result.PENDING) {
                if (result == Result.TRUE) {
                    evaluationsAsTrue++;
                } else if (result == Result.FALSE) {
                    addBadEvaluator(current);
                }
                it.remove();
            }
        }
    }

    private void evaluateAllNegated(Event event, int depth, ProblemDispatcher dispatcher) {
        Iterator<DeferredEvaluator> it = negated.iterator();
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            Result result = current.evaluate(event, depth, dispatcher);
            if (result != Result.PENDING) {
                if (result == Result.FALSE) {
                    addBadNegatedEvaluator(current);
                }
                it.remove();
            }
        }
    }

    private void addBadEvaluator(DeferredEvaluator evaluator) {
        if (this.problemLists == null) {
            this.problemLists = new ArrayList<>();
        }
        this.problemLists.add(evaluator.problems());
    }

    private void addBadNegatedEvaluator(DeferredEvaluator evaluator) {
        if (this.negatedProblemLists == null) {
            this.negatedProblemLists = new ArrayList<>();
        }
        this.negatedProblemLists.add(evaluator.problems());
    }

    private List<DeferredEvaluator> createEvaluators(Stream<Evaluator> stream) {
        return stream.map(DeferredEvaluator::new).collect(Collectors.toList());
    }
}
