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

import java.util.function.Consumer;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.InstanceBuilder;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * @author leadpony
 */
public class Const extends AbstractAssertion {

    private final JsonValue expected;
    private final JsonProvider jsonProvider;
    
    public Const(JsonValue expected, JsonProvider jsonProvider) {
        this.expected = expected;
        this.jsonProvider = jsonProvider;
    }
    
    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return new ConstEvaluator();
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.write("const", this.expected);
    }

    @Override
    protected AbstractAssertion createNegatedAssertion() {
        throw new UnsupportedOperationException();
    }
    
    protected class ConstEvaluator implements Evaluator {
        
        private final InstanceBuilder builder;
        
        ConstEvaluator() {
            this.builder = new InstanceBuilder(jsonProvider);
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            if (builder.append(event, parser)) {
                return Result.PENDING;
            }
            return testValue(builder.build(), consumer);
        }

        protected Result testValue(JsonValue actual, Consumer<Problem> consumer) {
            if (actual.equals(expected)) {
                return Result.TRUE;
            } else {
                Problem p = ProblemBuilder.newBuilder()
                        .withMessage("instance.problem.const")
                        .withParameter("actual", actual)
                        .withParameter("expected", expected)
                        .build();
                consumer.accept(p);
                return Result.FALSE;
            }
        }
    }
}
