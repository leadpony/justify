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
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Evaluator.Reporter;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.internal.base.JsonInstanceBuilder;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.core.InstanceType;

/**
 * @author leadpony
 */
abstract class AbstractEqualityAssertion extends AbstractAssertion {
    
    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        appender.append(new AssertionEvaluator(builderFactory));
    }
    
    protected abstract Result testValue(JsonValue actual, JsonParser parser, Reporter reporter);

    private class AssertionEvaluator implements Evaluator {
        
        private final JsonInstanceBuilder builder;
        
        private AssertionEvaluator(JsonBuilderFactory builderFactory) {
            this.builder = new JsonInstanceBuilder(builderFactory);
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
            if (builder.append(event, parser)) {
                return Result.PENDING;
            }
            return testValue(builder.build(), parser, reporter);
        }
    }
}
