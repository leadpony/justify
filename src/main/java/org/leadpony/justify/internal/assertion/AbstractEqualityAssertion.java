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

package org.leadpony.justify.internal.assertion;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Evaluator.ProblemReporter;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.internal.base.InstanceBuilder;
import org.leadpony.justify.core.InstanceType;

/**
 * @author leadpony
 */
abstract class AbstractEqualityAssertion extends AbstractAssertion {
    
    private final JsonProvider jsonProvider;
    
    protected AbstractEqualityAssertion(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return new InstanceEvaluator(jsonProvider);
    }
    
    protected abstract Result testValue(JsonValue actual, JsonParser parser, ProblemReporter reporter);

    private class InstanceEvaluator implements Evaluator {
        
        private final InstanceBuilder builder;
        
        private InstanceEvaluator(JsonProvider jsonProvider) {
            this.builder = new InstanceBuilder(jsonProvider);
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemReporter reporter) {
            if (builder.append(event, parser)) {
                return Result.PENDING;
            }
            return testValue(builder.build(), parser, reporter);
        }
    }
}
