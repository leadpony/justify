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

import java.util.List;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Exclusive disjunction evaluator used for "oneOf" boolean logic schema.
 * 
 * @author leadpony
 */
class ExclusiveDisjunctionEvaluator extends DisjunctionEvaluator {
 
    static LogicalEvaluator.Builder builder(InstanceType type) {
        return new Builder(type);
    }
    
    protected ExclusiveDisjunctionEvaluator(List<Evaluator> children, Event stopEvent) {
        super(children, stopEvent);
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
        return "instance.problem.oneOf";
    }

    private Result reportTooManyTrueEvaluations(JsonParser parser, Reporter reporter) {
        Problem p = ProblemBuilder.newBuilder(parser)
                .withMessage("instance.problem.oneOf.over")
                .withParameter("valid", this.trueEvaluations)
                .build();
        reporter.reportProblem(p);
        return Result.FALSE;
    }

    private static class Builder extends AbstractLogicalEvaluator.Builder {

        private Builder(InstanceType type) {
            super(type);
        }

        @Override
        protected LogicalEvaluator createEvaluator(List<Evaluator> children, Event stopEvent) {
            return new ExclusiveDisjunctionEvaluator(children, stopEvent);
        }
    }
}
