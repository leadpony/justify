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

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Exclusive disjunction evaluator used for "oneOf" boolean logic schema.
 * 
 * @author leadpony
 */
class ExclusiveDisjunctionEvaluator extends DisjunctionEvaluator {
 
    public static LogicalEvaluator.Builder builder() {
        return new ExclusiveDisjunctionEvaluator();
    }
    
    @Override
    protected boolean accumulateResult(Evaluator evaluator, Result result) {
        super.accumulateResult(evaluator, result);
        return (this.trueEvaluations <= 1);
    }

    @Override
    protected Result conclude(JsonParser parser, Reporter reporter) {
        if (this.trueEvaluations > 1) {
            return reportTooManyTrueEvaluations(parser, reporter);
        } else {
            return super.conclude(parser, reporter);
        }
    }
    
    @Override
    protected String getMessageKey() {
        return "instance.problem.one.of";
    }

    private Result reportTooManyTrueEvaluations(JsonParser parser, Reporter reporter) {
        Problem p = ProblemBuilder.newBuilder(parser)
                .withMessage("instance.problem.one.of.over")
                .withParameter("valid", this.trueEvaluations)
                .build();
        reporter.reportProblem(p);
        return Result.FALSE;
    }
}
