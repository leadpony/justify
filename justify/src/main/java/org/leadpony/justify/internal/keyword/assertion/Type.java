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

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * An assertion representing "type" keyword.
 *
 * @author leadpony
 */
@KeywordType("type")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public abstract class Type extends AbstractAssertionKeyword {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            switch (value.getValueType()) {
            case STRING:
                return new Single(value, toInstanceType((JsonString) value));
            case ARRAY:
                Set<InstanceType> types = new LinkedHashSet<>();
                for (JsonValue item : value.asJsonArray()) {
                    if (item.getValueType() == ValueType.STRING) {
                        types.add(toInstanceType((JsonString) item));
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                return new Multiple(value, types);
            default:
                throw new IllegalArgumentException();
            }
        };
    }

    public static Type of(JsonValue json, InstanceType type) {
        return new Single(json, type);
    }

    public static Type of(JsonValue json, Set<InstanceType> types) {
        if (types.size() == 1) {
            return new Single(json, types.iterator().next());
        } else {
            return new Multiple(json, types);
        }
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

    static InstanceType toInstanceType(JsonString value) {
        String name = value.getString().toUpperCase();
        return InstanceType.valueOf(name);
    }

    protected Type(JsonValue json) {
        super(json);
    }

    /**
     * Type assertion specialized for single type.
     *
     * @author leadpony
     */
    static class Single extends Type {

        private final InstanceType expectedType;

        Single(JsonValue json, InstanceType expectedType) {
            super(json);
            this.expectedType = expectedType;
        }

        @Override
        protected Evaluator doCreateEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new AbstractKeywordEvaluator(context, schema, this) {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = newProblemBuilder()
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
        protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (!testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new AbstractKeywordEvaluator(context, schema, this) {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = newProblemBuilder()
                            .withMessage(Message.INSTANCE_PROBLEM_NOT_TYPE)
                            .withParameter("expected", expectedType)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
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

        Multiple(JsonValue json, Set<InstanceType> types) {
            super(json);
            this.expectedTypes = new LinkedHashSet<>(types);
        }

        @Override
        protected Evaluator doCreateEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new AbstractKeywordEvaluator(context, schema, this) {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = newProblemBuilder()
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
        protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (!testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new AbstractKeywordEvaluator(context, schema, this) {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = newProblemBuilder()
                            .withMessage(Message.INSTANCE_PROBLEM_NOT_TYPE_PLURAL)
                            .withParameter("actual", narrowerType)
                            .withParameter("expected", expectedTypes)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        private boolean testType(InstanceType type) {
            return expectedTypes.contains(type)
                    || (type == InstanceType.INTEGER && expectedTypes.contains(InstanceType.NUMBER));
        }
    }
}
