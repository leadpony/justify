/*
 * Copyright 2018, 2020 the Justify authors.
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

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.problem.ProblemBranch;

/**
 * Evaluator for "oneOf" boolean logic.
 *
 * @author leadpony
 */
class ExclusiveEvaluator extends AbstractExclusiveEvaluator {

    private final List<DeferredEvaluator> operands;
    private final List<DeferredEvaluator> negated;
    private List<ProblemBranch> problemBranches;
    private List<ProblemBranch> negatedProblemBranches;
    private long evaluationsAsTrue;
    private final Event closingEvent;

    ExclusiveEvaluator(Evaluator parent, Keyword keyword, Event closingEvent,
            Iterable<JsonSchema> schemas,
            InstanceType type) {
        super(parent, keyword);
        this.operands = createEvaluators(schemas, type);
        this.negated = createNegatedEvaluators(schemas, type);
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
                dispatchProblems(dispatcher, problemBranches);
                return Result.FALSE;
            } else if (evaluationsAsTrue > 1) {
                dispatchNegatedProblems(dispatcher, negatedProblemBranches);
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
        if (this.problemBranches == null) {
            this.problemBranches = new ArrayList<>();
        }
        this.problemBranches.add(evaluator.problems());
    }

    private void addBadNegatedEvaluator(DeferredEvaluator evaluator) {
        if (this.negatedProblemBranches == null) {
            this.negatedProblemBranches = new ArrayList<>();
        }
        this.negatedProblemBranches.add(evaluator.problems());
    }

    private List<DeferredEvaluator> createEvaluators(Iterable<JsonSchema> schemas, InstanceType type) {
        List<DeferredEvaluator> result = new ArrayList<>();
        for (JsonSchema schema : schemas) {
            DeferredEvaluator deferred = new DeferredEvaluator(this);
            deferred.setEvaluator(schema.createEvaluator(deferred, type));
            result.add(deferred);
        }
        return result;
    }

    private List<DeferredEvaluator> createNegatedEvaluators(Iterable<JsonSchema> schemas, InstanceType type) {
        List<DeferredEvaluator> result = new ArrayList<>();
        for (JsonSchema schema : schemas) {
            DeferredEvaluator deferred = new DeferredEvaluator(this);
            deferred.setEvaluator(schema.createNegatedEvaluator(deferred, type));
            result.add(deferred);
        }
        return result;
    }
}
