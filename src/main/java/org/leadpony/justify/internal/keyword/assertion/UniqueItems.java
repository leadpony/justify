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

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.JsonInstanceBuilder;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * Assertion specified with "uniqueItems" validation keyword.
 * 
 * @author leadpony
 */
class UniqueItems implements Assertion {
    
    private final boolean unique;
    
    UniqueItems(boolean unique) {
        this.unique = unique;
    }

    @Override
    public String name() {
        return "uniqueItems";
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type == InstanceType.ARRAY && unique) {
            appender.append(new UniquenessEvaluator(builderFactory));
        }
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), unique);
    }
    
    private static class UniquenessEvaluator implements Evaluator {

        private final JsonBuilderFactory builderFactory;
        private final Map<JsonValue, Integer> values = new HashMap<>();
        private int index;
        private JsonInstanceBuilder builder;
        
        private UniquenessEvaluator(JsonBuilderFactory builderFactory) {
            this.builderFactory = builderFactory;
        }
        
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
            if (depth == 0) { 
                return event == Event.END_ARRAY ? 
                        Result.TRUE : Result.PENDING;
            }
            if (builder == null) {
                builder = new JsonInstanceBuilder(builderFactory);
            }
            if (builder.append(event, parser)) {
                return Result.PENDING;
            } else {
                JsonValue value = builder.build();
                builder = null;
                return testValue(value, index++, parser, reporter);
            }
        }
        
        private Result testValue(JsonValue value, int index, JsonParser parser, Reporter reporter) {
            if (values.containsKey(value)) {
                int lastIndex = values.get(value);
                Problem p = ProblemBuilder.newBuilder(parser)
                        .withMessage("instance.problem.uniqueItems")
                        .withParameter("index", index)
                        .withParameter("lastIndex", lastIndex)
                        .build();
                reporter.reportProblem(p);
                return Result.FALSE;
            }
            values.put(value, index);
            return Result.PENDING;
        }
    }
}
