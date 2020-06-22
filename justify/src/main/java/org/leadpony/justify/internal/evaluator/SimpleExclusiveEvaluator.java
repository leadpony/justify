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
import java.util.List;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.problem.ProblemBranch;

import jakarta.json.stream.JsonParser.Event;

/**
 * @author leadpony
 */
class SimpleExclusiveEvaluator extends AbstractExclusiveEvaluator
    implements ProblemDispatcher {

    private final Iterable<JsonSchema> schemas;
    private final InstanceType type;

    private ProblemBranch branch;

    SimpleExclusiveEvaluator(Evaluator parent, Keyword keyword, Iterable<JsonSchema> schemas, InstanceType type) {
        super(parent, keyword);
        this.schemas = schemas;
        this.type = type;
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

    @Override
    public void dispatchProblem(Problem problem) {
        if (this.branch == null) {
            this.branch = new ProblemBranch();
        }
        this.branch.add(problem);
    }

    private int evaluateAll(Event event, int depth, ProblemDispatcher dispatcher) {
        int evaluationsAsTrue = 0;
        List<ProblemBranch> problemBranches = new ArrayList<>();
        for (JsonSchema schema : this.schemas) {
            Evaluator evaluator = schema.createEvaluator(this, this.type);
            Result result = evaluator.evaluate(event, depth, this);
            if (result == Result.TRUE) {
                ++evaluationsAsTrue;
            } else if (result == Result.FALSE) {
                problemBranches.add(this.branch);
                this.branch = null;
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
        for (JsonSchema schema : this.schemas) {
            Evaluator evaluator = schema.createNegatedEvaluator(this, this.type);
            Result result = evaluator.evaluate(event, depth, this);
            if (result == Result.FALSE) {
                problemBranches.add(this.branch);
                this.branch = null;
            }
        }

        dispatchNegatedProblems(dispatcher, problemBranches);
    }
}
