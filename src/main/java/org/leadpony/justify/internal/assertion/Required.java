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

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * @author leadpony
 */
public class Required implements Assertion {
    
    private final Set<String> names;
    
    public Required(Iterable<String> names) {
        this.names = new HashSet<>();
        names.forEach(this.names::add);
    }

    @Override
    public boolean canApplyTo(InstanceType type) {
        return type == InstanceType.OBJECT;
    }

    @Override
    public AssertionEvaluator createEvaluator() {
        return new PropertyEvaluator(names);
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartArray("required");
        names.forEach(generator::write);
        generator.writeEnd();
    }
    
    private static class PropertyEvaluator implements AssertionEvaluator {
        
        private final Set<String> remaining;
        
        private PropertyEvaluator(Set<String> required) {
            this.remaining = new HashSet<>(required);
        }

        @Override
        public Status evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            if (event == Event.KEY_NAME) {
                remaining.remove(parser.getString());
                return remaining.isEmpty() ? Status.TRUE : Status.CONTINUED;
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return checkRequiredProperties(consumer);
            } else {
                return Status.CONTINUED;
            }
        }
        
        private Status checkRequiredProperties(Consumer<Problem> consumer) {
            if (remaining.isEmpty()) {
                return Status.TRUE;
            } else {
                Problem p = ProblemBuilder.newBuilder()
                        .withMessage("instance.problem.required")
                        .withParameter("expected", remaining)
                        .build();
                consumer.accept(p);
                return Status.FALSE;
            }
        }
    }
}
