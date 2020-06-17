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
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;

/**
 * @author leadpony
 */
class SimpleNotExclusiveEvaluator extends AbstractKeywordBasedEvaluator
    implements Iterable<DeferredEvaluator> {

    private final List<DeferredEvaluator> operands;
    private List<Problem> problemList;
    private int evaluationsAsFalse;

    SimpleNotExclusiveEvaluator(Evaluator parent, Keyword keyword,
            Iterable<JsonSchema> schemas,
            InstanceType type) {
        super(parent, keyword);
        this.operands = createEvaluators(schemas, type);
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

    private List<DeferredEvaluator> createEvaluators(Iterable<JsonSchema> schemas, InstanceType type) {
        List<DeferredEvaluator> result = new ArrayList<>();
        for (JsonSchema schema : schemas) {
            DeferredEvaluator deferred = new DeferredEvaluator(this);
            Evaluator evaluator = schema.createNegatedEvaluator(deferred, type);
            if (evaluator != Evaluator.ALWAYS_TRUE) {
                deferred.setEvaluator(evaluator);
                result.add(deferred);
            }
        }
        return result;
    }
}
