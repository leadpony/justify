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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.DefaultProblemDispatcher;

/**
 * Evaluator which retains the found problems internally.
 * 
 * @author leadpony
 */
class RetainingEvaluator implements Evaluator, DefaultProblemDispatcher, Comparable<RetainingEvaluator> {

    private final Evaluator evaluator;
    private List<Problem> problems;
    
    /**
     * Constructs this evaluator.
     * 
     * @param evaluator the actual evaluator, cannot be {@code null}.
     */
    RetainingEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        return evaluator.evaluate(event, parser, depth, this);
    }
    
    @Override
    public void dispatchProblem(Problem problem) {
        requireNonNull(problem, "problem");
        if (this.problems == null) {
            this.problems = new ArrayList<>();
        }
        this.problems.add(problem);
    }

    @Override
    public int compareTo(RetainingEvaluator other) {
        return countProblems() - other.countProblems();
    }
    
    Evaluator internalEvaluator() {
        return evaluator;
    }
    
    List<Problem> problems() {
        return this.problems;
    }
    
    int countProblems() {
        return (this.problems == null) ? 0 : this.problems.size();
    }
}
