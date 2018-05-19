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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;

/**
 * Assertion specified by "required" keyword.
 * 
 * @author leadpony
 */
public class Required extends AbstractAssertion {
    
    protected final Set<String> names;
    
    public Required(Set<String> names) {
        this.names = new HashSet<>(names);
    }

    @Override
    public boolean canApplyTo(InstanceType type) {
        return type == InstanceType.OBJECT;
    }

    @Override
    public Evaluator createEvaluator() {
        return new PropertyEvaluator(names);
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartArray("required");
        names.forEach(generator::write);
        generator.writeEnd();
    }
    
    @Override
    protected AbstractAssertion createNegatedAssertion() {
        return new NotRequired(names);
    }

    private static class PropertyEvaluator implements ShallowEvaluator {
        
        protected final Set<String> remaining;
        
        private PropertyEvaluator(Set<String> required) {
            this.remaining = new HashSet<>(required);
        }

        @Override
        public Result evaluateShallow(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            if (event == Event.KEY_NAME) {
                remaining.remove(parser.getString());
                return test(consumer, false);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return test(consumer, true);
            } else {
                return Result.PENDING;
            }
        }
        
        protected Result test(Consumer<Problem> consumer, boolean last) {
            if (remaining.isEmpty()) {
                return Result.TRUE;
            } else if (last) {
                Problem p = ProblemBuilder.newBuilder()
                        .withMessage("instance.problem.required")
                        .withParameter("expected", remaining)
                        .build();
                consumer.accept(p);
                return Result.FALSE;
            } else {
                return Result.PENDING;
            }
        }
    }
    
    private static class NotRequired extends Required {

        private NotRequired(Set<String> names) {
            super(names);
        }

        @Override
        public Evaluator createEvaluator() {
            return new NegatedEvaluator(names);
        }

        @Override
        protected AbstractAssertion createNegatedAssertion() {
            return new Required(this.names);
        }
    }
    
    private static class NegatedEvaluator extends PropertyEvaluator {
        
        private final Set<String> names;
        
        private NegatedEvaluator(Set<String> names) {
            super(names);
            this.names = names;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            if (event == Event.KEY_NAME) {
                remaining.remove(parser.getString());
                return test(consumer, false);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return test(consumer, true);
            } else {
                return Result.PENDING;
            }
        }

        @Override
        protected Result test(Consumer<Problem> consumer, boolean last) {
            if (remaining.isEmpty()) {
                Problem p = ProblemBuilder.newBuilder()
                        .withMessage("instance.problem.not.required")
                        .withParameter("expected", this.names)
                        .build();
                consumer.accept(p);
                return Result.FALSE;
            } else if (last) {
                return Result.TRUE;
            } else {
                return Result.PENDING;
            }
        }
    }
}
