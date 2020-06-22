/*
 * Copyright 2020 the Justify authors.
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
import java.util.List;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.problem.ProblemBranch;

import jakarta.json.stream.JsonParser.Event;

/**
 * @author leadpony
 */
public abstract class CountingItemsEvaluator extends AbstractKeywordBasedEvaluator implements ProblemDispatcher {

    private final JsonSchema subschema;
    private int validItems;

    private Evaluator itemEvaluator;
    private ProblemBranch problems;
    private List<ProblemBranch> problemBranches;

    protected CountingItemsEvaluator(Evaluator parent, Keyword keyword, JsonSchema subschema) {
        super(parent, keyword);
        this.subschema = subschema;
    }

    @Override
    public Result evaluate(Event event, int depth) {
        if (depth == 1 && ParserEvents.isValue(event)) {
            InstanceType type = ParserEvents.toBroadInstanceType(event);
            this.itemEvaluator = createItemEvaluator(type);
        } else if (depth == 0 && event == Event.END_ARRAY) {
            return finish(this.validItems, this.problemBranches);
        }

        if (this.itemEvaluator != null && depth > 0) {
            return evaluateCurrentItem(event, depth);
        }

        return Result.PENDING;
    }

    @Override
    public ProblemDispatcher getDispatcherForChild(Evaluator evaluator) {
        return this;
    }

    protected Result handleValidItem(int validItems) {
        return Result.PENDING;
    }

    protected abstract Result finish(int validItems, List<ProblemBranch> branches);

    @Override
    public final void dispatchProblem(Problem problem) {
        if (this.problems == null) {
            this.problems = new ProblemBranch();
        }
        this.problems.add(problem);
    }

    protected Evaluator createItemEvaluator(InstanceType type) {
        return this.subschema.createEvaluator(this, type);
    }

    private Result evaluateCurrentItem(Event event, int depth) {
        final Result result = this.itemEvaluator.evaluate(event, depth - 1);
        if (result == Result.TRUE) {
            this.itemEvaluator = null;
            return handleValidItem(++this.validItems);
        } else if (result == Result.FALSE) {
            accumulateProblems();
            this.itemEvaluator = null;
        }
        return Result.PENDING;
    }

    private void accumulateProblems() {
        if (this.problemBranches == null) {
            this.problemBranches = new ArrayList<>();
        }
        this.problemBranches.add(this.problems);
        this.problems = null;
    }
}
