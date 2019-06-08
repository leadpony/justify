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

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
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
     * Converts the broader type into the narrower type.
     *
     * <p>
     * According to the JSON Schema Test Suite, 1.0 must be treated as an integer
     * rather than a number.
     * </p>
     *
     * @param type    the broader type.
     * @param context the context for the evaluator.
     * @return the narrower type.
     */
    protected InstanceType toNarrowType(InstanceType type, EvaluatorContext context) {
        if (type != InstanceType.NUMBER) {
            return type;
        }
        JsonParser parser = context.getParser();
        if (parser.isIntegralNumber()) {
            return InstanceType.INTEGER;
        } else {
            BigDecimal value = parser.getBigDecimal().stripTrailingZeros();
            if (value.scale() == 0) {
                return InstanceType.INTEGER;
            }
            return type;
        }
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
            InstanceType narrowerType = toNarrowType(type, context);
            if (testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new Evaluator() {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = createProblemBuilder(context)
                            .withMessage(Message.INSTANCE_PROBLEM_TYPE)
                            .withParameter("actual", narrowerType)
                            .withParameter("expected", expectedType)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        @Override
        protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (!testType(narrowerType)) {
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
        public JsonValue getValueAsJson(JsonProvider jsonProvider) {
            return jsonProvider.createValue(expectedType.name().toLowerCase());
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
            InstanceType narrowerType = toNarrowType(type, context);
            if (testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new Evaluator() {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = createProblemBuilder(context)
                            .withMessage(Message.INSTANCE_PROBLEM_TYPE_PLURAL)
                            .withParameter("actual", narrowerType)
                            .withParameter("expected", expectedTypes)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        @Override
        protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (!testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new Evaluator() {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = createProblemBuilder(context)
                            .withMessage(Message.INSTANCE_PROBLEM_NOT_TYPE_PLURAL)
                            .withParameter("actual", narrowerType)
                            .withParameter("expected", expectedTypes)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        @Override
        public JsonValue getValueAsJson(JsonProvider jsonProvider) {
            JsonArrayBuilder builder = jsonProvider.createArrayBuilder();
            expectedTypes.stream()
                .map(InstanceType::name)
                .map(String::toLowerCase)
                .forEach(builder::add);
            return builder.build();
        }

        private boolean testType(InstanceType type) {
            return expectedTypes.contains(type) ||
                    (type == InstanceType.INTEGER && expectedTypes.contains(InstanceType.NUMBER));
        }
    }
}
