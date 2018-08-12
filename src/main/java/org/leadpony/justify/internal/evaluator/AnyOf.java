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
import java.util.Objects;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Evaluator for "anyOf" boolean logic.
 * 
 * @author leadpony
 */
class AnyOf extends AbstractLogicalEvaluator {

    protected int trueEvaluations;
    protected List<StoringEvaluator> falseEvaluators;
   
    AnyOf() {
    }

    @Override
    public void append(Evaluator evaluator) {
        super.append(new StoringEvaluator(evaluator));
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
    protected Result getFinalResult(JsonParser parser, Consumer<Problem> reporter) {
        if (trueEvaluations > 0) {
            return Result.TRUE;
        } else if (falseEvaluators == null || falseEvaluators.isEmpty()) {
            Problem p = problemBuilderFactory.createProblemBuilder(parser)
                    .withMessage("instance.problem.unknown")
                    .build();
            reporter.accept(p);
            return Result.FALSE;
        } else {
            ProblemBuilder builder = problemBuilderFactory.createProblemBuilder(parser);
            builder.withMessage(getMessageKey())
                   .withParameter("invalid", falseEvaluators.size());
            falseEvaluators.stream()
                    .map(StoringEvaluator::problems)
                    .filter(Objects::nonNull)
                    .forEach(builder::withSubproblems);
            reporter.accept(builder.build());
            return Result.FALSE;
        }
    }
    
    protected String getMessageKey() {
        return "instance.problem.anyOf";
    }
}
