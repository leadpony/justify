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

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;

/**
 * @author leadpony
 */
class SimpleNotExclusiveEvaluator extends AbstractLogicalEvaluator
    implements Iterable<DeferredEvaluator> {

    private final List<DeferredEvaluator> operands = new ArrayList<>();
    private List<Problem> problemList;
    private int evaluationsAsFalse;

    SimpleNotExclusiveEvaluator(EvaluatorContext context) {
        super(context);
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        Iterator<DeferredEvaluator> it = operands.iterator();
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            if (current.evaluate(event, depth, dispatcher) == Result.FALSE) {
                addBadEvaluator(current);
            }
        }
        return finalizeResult(dispatcher);
    }

    @Override
    public void append(Evaluator evaluator) {
        if (evaluator == Evaluator.ALWAYS_TRUE) {
            return;
        }
        operands.add(new DeferredEvaluator(evaluator));
    }

    @Override
    public Iterator<DeferredEvaluator> iterator() {
        return operands.iterator();
    }

    protected void addBadEvaluator(DeferredEvaluator evaluator) {
        if (this.problemList == null) {
            this.problemList = evaluator.problems();
        }
        ++evaluationsAsFalse;
    }

    protected Result finalizeResult(ProblemDispatcher dispatcher) {
        if (evaluationsAsFalse == 1) {
            problemList.forEach(dispatcher::dispatchProblem);
            return Result.FALSE;
        } else {
            return Result.TRUE;
        }
    }
}
