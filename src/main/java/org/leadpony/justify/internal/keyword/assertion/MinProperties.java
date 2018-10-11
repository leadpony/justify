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

package org.leadpony.justify.internal.keyword.assertion;

import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;

/**
 * Assertion specified with "minProperties" validation keyword.
 * 
 * @author leadpony
 */
class MinProperties extends AbstractAssertion {
    
    private final int limit;
    
    MinProperties(int limit) {
        this.limit = limit;
    }

    @Override
    public String name() {
        return "minProperties";
    }

    @Override
    public Evaluator createEvaluator(InstanceType type, JsonBuilderFactory builderFactory, boolean affirmative) {
        if (type == InstanceType.OBJECT) {
            Evaluator evaluator;
            if (affirmative) {
                evaluator = new AssertionEvaluator(limit, this);
            } else if (limit > 0) {
                evaluator = new MaxProperties.AssertionEvaluator(limit - 1, this);
            } else {
                evaluator = Evaluators.alwaysFalse(getEnclosingSchema());
            }
            return evaluator;
        } else {
            return Evaluators.ALWAYS_IGNORED;
        }
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), limit);
    }
    
    static class AssertionEvaluator implements ShallowEvaluator {

        private final int minProperties;
        private final ProblemBuilderFactory factory;
        private int currentCount;
        
        AssertionEvaluator(int minProperties, ProblemBuilderFactory factory) {
            this.minProperties = minProperties;
            this.factory = factory;
        }
        
        @Override
        public Result evaluateShallow(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
            if (depth == 1) {
                if (event == Event.KEY_NAME && ++currentCount >= minProperties) {
                    return Result.TRUE;
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (currentCount >= minProperties) {
                    return Result.TRUE;
                } else {
                    Problem p = factory.createProblemBuilder(parser)
                            .withMessage("instance.problem.minProperties")
                            .withParameter("actual", currentCount)
                            .withParameter("limit", minProperties)
                            .build();
                    reporter.accept(p);
                    return Result.FALSE;
                }
            }
            return Result.PENDING;
        }
    }
}
