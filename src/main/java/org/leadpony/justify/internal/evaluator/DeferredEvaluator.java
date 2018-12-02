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

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.DefaultProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemList;

/**
 * Evaluator which retains the found problems and dispatches them later.
 * 
 * @author leadpony
 */
class DeferredEvaluator implements Evaluator, DefaultProblemDispatcher {

    private final Evaluator evaluator;
    private ProblemList problems;
    
    /**
     * Constructs this evaluator.
     * 
     * @param evaluator the actual evaluator, cannot be {@code null}.
     */
    DeferredEvaluator(Evaluator evaluator) {
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
            this.problems = ProblemList.newList();
        }
        this.problems.add(problem);
    }

    /**
     * Returns the internal evaluator.
     * @return the internal evaluator.
     */
    Evaluator internalEvaluator() {
        return evaluator;
    }
    
    /**
     * Returns the problems found by this evaluator.
     * @return the problems found by this evaluator. 
     */
    ProblemList problems() {
        return this.problems;
    }
}
