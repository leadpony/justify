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

import java.util.List;
import java.util.stream.Collectors;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBranch;

/**
 * Skeletal implementation for {@link ExclusiveEvaluator} and {@link SimpleExclusiveEvaluator}.
 * @author leadpony
 */
abstract class AbstractExclusiveEvaluator extends AbstractKeywordBasedEvaluator {

    protected AbstractExclusiveEvaluator(Evaluator parent, Keyword keyword) {
        super(parent, keyword);
    }

    protected void dispatchProblems(List<ProblemBranch> problemBranches) {
        List<ProblemBranch> filteredBranches = problemBranches.stream()
                .filter(ProblemBranch::isResolvable)
                .collect(Collectors.toList());
        if (filteredBranches.isEmpty()) {
            filteredBranches = problemBranches;
        }
        ProblemBuilder builder = newProblemBuilder()
                .withMessage(Message.INSTANCE_PROBLEM_ONEOF_FEW)
                .withBranches(filteredBranches);
        getDispatcher().dispatchProblem(builder.build());
    }

    protected void dispatchNegatedProblems(List<ProblemBranch> problemBranches) {
        ProblemBuilder builder = newProblemBuilder()
                .withMessage(Message.INSTANCE_PROBLEM_ONEOF_MANY)
                .withBranches(problemBranches);
        getDispatcher().dispatchProblem(builder.build());
    }
}
