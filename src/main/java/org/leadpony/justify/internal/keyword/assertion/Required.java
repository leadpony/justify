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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;
import org.leadpony.justify.internal.keyword.ObjectKeyword;

/**
 * Assertion specified with "required" validation keyword.
 * 
 * @author leadpony
 */
class Required extends AbstractAssertion implements ObjectKeyword {
    
    protected final Set<String> names;
    
    Required(Set<String> names) {
        this.names = new LinkedHashSet<>(names);
    }
    
    @Override
    public String name() {
        return "required";
    }

    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        if (names.isEmpty()) {
            return createAlwaysTrueEvaluator();
        } else {
            return new AssertionEvaluator(names);
        }
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        if (names.isEmpty()) {
            return createAlwaysFalseEvaluator();
        } else {
            return new NegatedAssertionEvaluator(names);    
        }
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonArrayBuilder arrayBuilder = builderFactory.createArrayBuilder();
        names.forEach(arrayBuilder::add);
        builder.add(name(), arrayBuilder);
    }

    private class AssertionEvaluator implements ShallowEvaluator {
        
        private final Set<String> missing;
        
        private AssertionEvaluator(Set<String> required) {
            this.missing = new LinkedHashSet<>(required);
        }

        @Override
        public Result evaluateShallow(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            if (event == Event.KEY_NAME) {
                missing.remove(parser.getString());
                return test(parser, dispatcher, false);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return test(parser, dispatcher, true);
            } else {
                return Result.PENDING;
            }
        }
        
        private Result test(JsonParser parser, ProblemDispatcher dispatcher, boolean last) {
            if (missing.isEmpty()) {
                return Result.TRUE;
            } else if (last) {
                for (String property : missing) {
                    Problem p = createProblemBuilder(parser)
                            .withMessage("instance.problem.required")
                            .withParameter("required", property)
                            .build();
                    dispatcher.dispatchProblem(p);
                }
                return Result.FALSE;
            } else {
                return Result.PENDING;
            }
        }
    }

    private class NegatedAssertionEvaluator implements ShallowEvaluator {
        
        private final Set<String> missing;

        private NegatedAssertionEvaluator(Set<String> names) {
            this.missing = new LinkedHashSet<>(names);
        }

        @Override
        public Result evaluateShallow(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            if (event == Event.KEY_NAME) {
                missing.remove(parser.getString());
                return test(parser, dispatcher, false);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return test(parser, dispatcher, true);
            } else {
                return Result.PENDING;
            }
        }

        private Result test(JsonParser parser, ProblemDispatcher dispatcher, boolean last) {
            if (missing.isEmpty()) {
                Problem p = null;
                if (names.size() == 1) {
                    String name = names.iterator().next();
                    p = createProblemBuilder(parser)
                            .withMessage("instance.problem.not.required")
                            .withParameter("required", name)
                            .build();
                } else {
                    p = createProblemBuilder(parser)
                        .withMessage("instance.problem.not.required.plural")
                        .withParameter("required", names)
                        .build();
                }
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            } else if (last) {
                return Result.TRUE;
            } else {
                return Result.PENDING;
            }
        }
    }
}
