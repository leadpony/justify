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
import java.util.stream.Stream;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.problem.ProblemBranch;

/**
 * @author leadpony
 */
class SimpleExclusiveEvaluator extends AbstractExclusiveEvaluator {

    private final Stream<Evaluator> operands;
    private final Stream<Evaluator> negated;

    SimpleExclusiveEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword,
            Stream<Evaluator> operands,
            Stream<Evaluator> negated) {
        super(context, schema, keyword);
        this.operands = operands;
        this.negated = negated;
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        int evaluationsAsTrue = evaluateAll(event, depth, dispatcher);
        if (evaluationsAsTrue == 1) {
            return Result.TRUE;
        } else if (evaluationsAsTrue > 1) {
            evaluateAllNegated(event, depth, dispatcher);
        }
        return Result.FALSE;
    }

    private static Iterator<DeferredEvaluator> iterator(Stream<Evaluator> stream) {
        return stream.map(DeferredEvaluator::new).iterator();
    }

    private int evaluateAll(Event event, int depth, ProblemDispatcher dispatcher) {
        List<ProblemBranch> problemBranches = new ArrayList<>();
        Iterator<DeferredEvaluator> it = iterator(operands);
        int evaluationsAsTrue = 0;
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            Result result = current.evaluate(event, depth, dispatcher);
            if (result == Result.TRUE) {
                ++evaluationsAsTrue;
            } else if (result == Result.FALSE) {
                problemBranches.add(current.problems());
            } else {
                assert false;
            }
        }
        if (evaluationsAsTrue == 0) {
            dispatchProblems(dispatcher, problemBranches);
        }
        return evaluationsAsTrue;
    }

    private void evaluateAllNegated(Event event, int depth, ProblemDispatcher dispatcher) {
        List<ProblemBranch> problemBranches = new ArrayList<>();
        Iterator<DeferredEvaluator> it = iterator(negated);
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            Result result = current.evaluate(event, depth, dispatcher);
            if (result == Result.FALSE) {
                problemBranches.add(current.problems());
            }
        }
        dispatchNegatedProblems(dispatcher, problemBranches);
    }
}
