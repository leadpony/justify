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

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;

/**
 * Assertion specified with "maxProperties" validation keyword.
 * 
 * @author leadpony
 */
class MaxProperties implements Assertion {
    
    private final int bound;
    
    MaxProperties(int bound) {
        this.bound = bound;
    }

    @Override
    public String name() {
        return "maxProperties";
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type == InstanceType.OBJECT) {
            appender.append(new InnerEvaluator());
        }
    }

    @Override
    public Assertion negate() {
        return new MinProperties(bound + 1);
    }
    
    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), bound);
    }
    
    private class InnerEvaluator implements ShallowEvaluator {

        private int currentCount;
        
        @Override
        public Result evaluateShallow(Event event, JsonParser parser, int depth, Reporter reporter) {
            if (depth == 1) {
                if (event == Event.KEY_NAME) {
                    ++currentCount;
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (currentCount <= bound) {
                    return Result.TRUE;
                } else {
                    Problem p = ProblemBuilder.newBuilder(parser)
                            .withMessage("instance.problem.max.properties")
                            .withParameter("actual", currentCount)
                            .withParameter("bound", bound)
                            .build();
                    reporter.reportProblem(p);
                    return Result.FALSE;
                }
            }
            return Result.PENDING;
        }
    }
}
