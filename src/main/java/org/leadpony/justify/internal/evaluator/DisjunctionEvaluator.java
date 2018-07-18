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
import java.util.List;
import java.util.stream.Collectors;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Disjunction evaluator used for "oneOf" and "anyOf" boolean logic schemas.
 * 
 * @author leadpony
 */
class DisjunctionEvaluator extends AbstractLogicalEvaluator {

    protected int trueEvaluations;
    protected List<StoringEvaluator> falseEvaluators;
   
    static LogicalEvaluator.Builder builder(InstanceType type) {
        return new Builder(type);
    }
    
    protected DisjunctionEvaluator(List<Evaluator> children, Event stopEvent) {
        super(wrapChildren(children), stopEvent);
    }
    
    @Override
    protected boolean accumulateResult(Evaluator evaluator, Result result) {
        if (result == Result.TRUE) {
            ++trueEvaluations;
        } else {
            if (falseEvaluators == null) {
                falseEvaluators = new ArrayList<>();
            }
            falseEvaluators.add((StoringEvaluator)evaluator);
        }
        return (trueEvaluations == 0);
    }
    
    @Override
    protected Result conclude(JsonParser parser, Reporter reporter) {
        if (trueEvaluations > 0 || falseEvaluators == null || falseEvaluators.isEmpty()) {
            return Result.TRUE;
        }
        ProblemBuilder builder = ProblemBuilder.newBuilder(parser);
        builder.withMessage(getMessageKey())
               .withParameter("invalid", falseEvaluators.size());
        falseEvaluators.stream()
                .map(StoringEvaluator::problems)
                .forEach(builder::withSubproblems);
        reporter.reportProblem(builder.build());
        return Result.FALSE;
    }
    
    protected String getMessageKey() {
        return "instance.problem.anyOf";
    }
    
    private static List<Evaluator> wrapChildren(List<Evaluator> children) {
        return children.stream()
            .map(StoringEvaluator::new)
            .collect(Collectors.toList());
    }

    private static class Builder extends AbstractLogicalEvaluator.Builder {

        private Builder(InstanceType type) {
            super(type);
        }

        @Override
        protected LogicalEvaluator createEvaluator(List<Evaluator> children, Event stopEvent) {
            return new DisjunctionEvaluator(children, stopEvent);
        }
    }
}
