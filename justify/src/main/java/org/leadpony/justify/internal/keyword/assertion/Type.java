/*
 * Copyright 2018-2019 the Justify authors.
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
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;

/**
 * An assertion representing "type" keyword.
 *
 * @author leadpony
 */
public abstract class Type extends AbstractAssertion {

    public static Type of(InstanceType type) {
        return new Single(type);
    }

    public static Type of(Set<InstanceType> types) {
        if (types.size() == 1) {
            return new Single(types.iterator().next());
        } else {
            return new Multiple(types);
        }
    }

    @Override
    public String name() {
        return "type";
    }

    /**
     * Type assertion specialized for single type.
     *
     * @author leadpony
     */
    static class Single extends Type {

        private final InstanceType expectedType;

        Single(InstanceType expectedType) {
            this.expectedType = expectedType;
        }

        @Override
        protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
            if (testType(type)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new Evaluator() {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = createProblemBuilder(context)
                            .withMessage(Message.INSTANCE_PROBLEM_TYPE)
                            .withParameter("actual", type)
                            .withParameter("expected", expectedType)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        @Override
        protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            if (!testType(type)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new Evaluator() {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = createProblemBuilder(context)
                            .withMessage(Message.INSTANCE_PROBLEM_NOT_TYPE)
                            .withParameter("expected", expectedType)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        @Override
        public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            builder.add("type", expectedType.name().toLowerCase());
        }

        private boolean testType(InstanceType type) {
            if (type == this.expectedType) {
                return true;
            } else if (type == InstanceType.INTEGER) {
                return this.expectedType == InstanceType.NUMBER;
            } else {
                return false;
            }
        }
    }

    /**
     * Type assertion specialized for multiple types.
     *
     * @author leadpony
     */
    static class Multiple extends Type {

        private final Set<InstanceType> expectedTypes;

        Multiple(Set<InstanceType> types) {
            this.expectedTypes = new LinkedHashSet<>(types);
        }

        @Override
        protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
            if (testType(type)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new Evaluator() {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = createProblemBuilder(context)
                            .withMessage(Message.INSTANCE_PROBLEM_TYPE_PLURAL)
                            .withParameter("actual", type)
                            .withParameter("expected", expectedTypes)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        @Override
        protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            if (!testType(type)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new Evaluator() {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = createProblemBuilder(context)
                            .withMessage(Message.INSTANCE_PROBLEM_NOT_TYPE_PLURAL)
                            .withParameter("actual", type)
                            .withParameter("expected", expectedTypes)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        @Override
        public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            JsonArrayBuilder arrayBuilder = builderFactory.createArrayBuilder();
            expectedTypes.stream()
                    .map(InstanceType::name)
                    .map(String::toLowerCase)
                    .forEach(arrayBuilder::add);
            builder.add("type", arrayBuilder);
        }

        private boolean testType(InstanceType type) {
            return expectedTypes.contains(type) ||
                    (type == InstanceType.INTEGER && expectedTypes.contains(InstanceType.NUMBER));
        }
    }
}
