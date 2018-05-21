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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
abstract class DisjunctionEvaluator extends LogicalEvaluator implements Consumer<Problem> {

    protected Evaluator currentEvaluator;
    private Map<Evaluator, List<Problem>> problemMap;
    protected int numberOfTrues;
    
    protected DisjunctionEvaluator(LogicalCombiner combiner) {
        super(combiner);
    }
    
    @Override
    public void accept(Problem problem) {
        addProblem(this.currentEvaluator, problem);
    }
    
    @Override
    protected Result evaluate(Evaluator evaluator, Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        this.currentEvaluator = evaluator;
        return evaluator.evaluate(event, parser, depth, this);
    }
    
    @Override
    protected Result getFinalResult(Consumer<Problem> consumer) {
        if (numberOfTrues > 0 || problemMap == null || problemMap.isEmpty()) {
            return Result.TRUE;
        }
        List<Evaluator> failed = new ArrayList<>(problemMap.keySet());
        Collections.sort(failed, (a, b)->
            problemMap.get(a).size() - problemMap.get(b).size()
        );
        Evaluator first = failed.get(0);
        problemMap.get(first).forEach(consumer);
        return Result.FALSE;
    }

    private void addProblem(Evaluator evaluator, Problem problem) {
        if (problemMap == null) {
            problemMap = new HashMap<>();
        }
        List<Problem> problems = problemMap.get(currentEvaluator);
        if (problems == null) {
            problems = new ArrayList<>();
            problemMap.put(evaluator, problems);
        }
        problems.add(problem);
    }
}
