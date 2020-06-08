/*
 * Copyright 2018-2020 the Justify authors.
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

package org.leadpony.justify.internal.keyword.validation;

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
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordAwareEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;

/**
 * An assertion representing "type" keyword.
 *
 * @author leadpony
 */
@KeywordClass("type")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public abstract class Type extends AbstractAssertionKeyword {

    public static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return "type";
        }

        @Override
        public Keyword parse(JsonValue jsonValue) {
            switch (jsonValue.getValueType()) {
            case STRING:
                return new Single(jsonValue, toInstanceType((JsonString) jsonValue));
            case ARRAY:
                Set<InstanceType> types = new LinkedHashSet<>();
                for (JsonValue item : jsonValue.asJsonArray()) {
                    if (item.getValueType() == ValueType.STRING) {
                        types.add(toInstanceType((JsonString) item));
                    } else {
                        return failed(jsonValue);
                    }
                }
                return new Multiple(jsonValue, types);
            default:
                return failed(jsonValue);
            }
        }
    };

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

    @Override
    public KeywordType getType() {
        return TYPE;
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
        public Evaluator createEvaluator(EvaluatorContext context, InstanceType type, ObjectJsonSchema schema) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new AbstractKeywordAwareEvaluator(context, schema, this) {
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
        public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type, ObjectJsonSchema schema) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (!testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new AbstractKeywordAwareEvaluator(context, schema, this) {
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
        public Evaluator createEvaluator(EvaluatorContext context, InstanceType type, ObjectJsonSchema schema) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new AbstractKeywordAwareEvaluator(context, schema, this) {
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
        public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type, ObjectJsonSchema schema) {
            InstanceType narrowerType = toNarrowType(type, context);
            if (!testType(narrowerType)) {
                return Evaluator.ALWAYS_TRUE;
            }
            return new AbstractKeywordAwareEvaluator(context, schema, this) {
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
