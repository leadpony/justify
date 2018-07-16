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

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Disjunction evaluator used for "oneOf" and "anyOf" boolean logic schemas.
 * 
 * @author leadpony
 */
class DisjunctionEvaluator extends AbstractLogicalEvaluator {

    protected int trueEvaluations;
    protected List<StoringEvaluator> falseEvaluators;
   
    public static LogicalEvaluator.Builder builder() {
        return new DisjunctionEvaluator();
    }
    
    @Override
    public void append(Evaluator evaluator) {
        super.append(new StoringEvaluator(evaluator));
    }

    @Override
    public Evaluator build() {
        if (evaluators.isEmpty()) {
            return null;
        } else if (evaluators.size() == 1) {
            return ((StoringEvaluator)evaluators.get(0)).internalEvaluator();
        } else {
            return this;
        }
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
}
