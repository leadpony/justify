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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.problem.ProblemBranch;

/**
 * Evaluator which retains the found problems and dispatches them later.
 *
 * @author leadpony
 */
public class DeferredEvaluator implements Evaluator, ProblemDispatcher {

    private final Evaluator parent;
    private Evaluator evaluator;
    private ProblemBranch problemBranch;

    public DeferredEvaluator(Evaluator parent) {
        this.parent = parent;
    }

    /**
     * Returns the internal evaluator.
     *
     * @return the internal evaluator.
     */
    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        assert evaluator != null;
        this.evaluator = evaluator;
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        return evaluator.evaluate(event, depth, this);
    }

    @Override
    public Evaluator getParent() {
        return parent;
    }

    @Override
    public void dispatchProblem(Problem problem) {
        requireNonNull(problem, "problem");
        if (this.problemBranch == null) {
            this.problemBranch = new ProblemBranch();
        }
        this.problemBranch.add(problem);
    }

    /**
     * Returns the problems found by this evaluator.
     *
     * @return the problems found by this evaluator.
     */
    public ProblemBranch problems() {
        return this.problemBranch;
    }
}
