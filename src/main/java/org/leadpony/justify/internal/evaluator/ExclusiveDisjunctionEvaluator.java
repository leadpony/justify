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

import java.util.function.Consumer;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * @author leadpony
 */
class ExclusiveDisjunctionEvaluator extends DisjunctionEvaluator {
    
    @Override
    protected boolean accumulateResult(Evaluator evaluator, Result result) {
        super.accumulateResult(evaluator, result);
        return (this.numberOfTrues <= 1);
    }

    @Override
    protected Result conclude(Consumer<Problem> consumer) {
        if (this.numberOfTrues > 1) {
            return tooManyTrueEvaluations(consumer);
        } else {
            return super.conclude(consumer);
        }
    }
    
    private Result tooManyTrueEvaluations(Consumer<Problem> consumer) {
        Problem p = ProblemBuilder.newBuilder()
                .withMessage("instance.problem.one.of")
                .withParameter("actual", numberOfTrues)
                .build();
        consumer.accept(p);
        return Result.FALSE;
    }
}
