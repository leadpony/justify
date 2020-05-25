/*
 * Copyright 2020 the Justify authors.
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractPropertyDependentEvaluator;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.keyword.ObjectEvaluatorSource;
import org.leadpony.justify.internal.problem.ProblemBuilder;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser.Event;

/**
 * A keyword representing "dependentRequired".
 *
 * @author leadpony
 */
public class DependentRequired extends AbstractAssertionKeyword implements ObjectEvaluatorSource {

    static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return "dependentRequired";
        }

        @Override
        public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
            if (jsonValue.getValueType() == ValueType.OBJECT) {
                Map<String, Set<String>> map = new LinkedHashMap<>();
                for (Map.Entry<String, JsonValue> entry : jsonValue.asJsonObject().entrySet()) {
                    String k = entry.getKey();
                    JsonValue v = entry.getValue();
                    if (v.getValueType() == ValueType.ARRAY) {
                        Set<String> properties = new LinkedHashSet<>();
                        for (JsonValue item : v.asJsonArray()) {
                            if (item.getValueType() == ValueType.STRING) {
                                properties.add(((JsonString) item).getString());
                            } else {
                                throw new IllegalArgumentException();
                            }
                        }
                        map.put(k, properties);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                return new DependentRequired(jsonValue, map);
            } else {
                throw new IllegalArgumentException();
            }
        }
    };

    private final Map<String, Dependent> map;

    public DependentRequired(JsonValue json, Map<String, Set<String>> map) {
        super(json);
        Map<String, Dependent> newMap = new HashMap<>();
        map.forEach((key, value) -> {
            newMap.put(key, createDependent(key, value));
        });
        this.map = newMap;
    }

    private Dependent createDependent(String propertyName, Set<String> required) {
        if (required.isEmpty()) {
            return new Dependent(propertyName);
        } else {
            return new NonEmptyDependent(propertyName, required);
        }
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        LogicalEvaluator combined = Evaluators.conjunctive(type);
        map.values().stream()
                .map(d -> d.createEvaluator(context, schema))
                .forEach(combined::append);
        return combined;
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        LogicalEvaluator combined = Evaluators.disjunctive(context, schema, this, type);
        map.values().stream()
                .map(d -> d.createNegatedEvaluator(context, schema))
                .forEach(combined::append);
        return combined;
    }

    private class Dependent {

        protected final String propertyName;

        Dependent(String propertyName) {
            this.propertyName = propertyName;
        }

        Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema) {
            return Evaluator.ALWAYS_TRUE;
        }

        Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema) {
            return new NegatedEmptyDependentEvaluator(context, schema, DependentRequired.this, propertyName);
        }
    }

    private class NonEmptyDependent extends Dependent {

        private final Set<String> required;

        NonEmptyDependent(String propertyName, Set<String> required) {
            super(propertyName);
            assert !required.isEmpty();
            this.required = required;
        }

        @Override
        Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema) {
            return new DependentEvaluator(context, schema, DependentRequired.this, propertyName, required);
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema) {
            return new NegatedDependentEvaluator(context, schema, DependentRequired.this, propertyName, required);
        }
    }

    private abstract static class AbstractDependentEvaluator extends AbstractPropertyDependentEvaluator {

        protected final Set<String> required;
        protected final Set<String> missing;
        protected boolean active;

        protected AbstractDependentEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword,
                String propertyName, Set<String> required) {
            super(context, schema, keyword, propertyName);
            this.required = required;
            this.missing = new LinkedHashSet<>(required);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                String keyName = getParser().getString();
                if (keyName.equals(getPropertyName())) {
                    active = true;
                }
                missing.remove(keyName);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (active) {
                    return test(dispatcher);
                } else {
                    return testMissingProperty(dispatcher);
                }
            }
            return Result.PENDING;
        }

        protected abstract Result test(ProblemDispatcher dispatcher);

        protected abstract Result testMissingProperty(ProblemDispatcher dispatcher);
    }

    public static class DependentEvaluator extends AbstractDependentEvaluator {

        public DependentEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword, String propertyName,
                Set<String> required) {
            super(context, schema, keyword, propertyName, required);
        }

        @Override
        protected Result test(ProblemDispatcher dispatcher) {
            if (missing.isEmpty()) {
                return Result.TRUE;
            } else {
                for (String entry : missing) {
                    Problem p = newProblemBuilder()
                            .withMessage(Message.INSTANCE_PROBLEM_DEPENDENCIES)
                            .withParameter("required", entry)
                            .withParameter("dependant", getPropertyName())
                            .build();
                    dispatcher.dispatchProblem(p);
                }
                return Result.FALSE;
            }
        }

        @Override
        protected Result testMissingProperty(ProblemDispatcher dispatcher) {
            return Result.TRUE;
        }
    }

    public static class NegatedDependentEvaluator extends AbstractDependentEvaluator {

        public NegatedDependentEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword,
                String propertyName, Set<String> required) {
            super(context, schema, keyword, propertyName, required);
        }

        @Override
        protected Result test(ProblemDispatcher dispatcher) {
            if (required.isEmpty()) {
                Problem p = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                        .withParameter("required", getPropertyName())
                        .build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            } else if (missing.isEmpty()) {
                ProblemBuilder b = newProblemBuilder()
                        .withParameter("dependant", getPropertyName());
                if (required.size() == 1) {
                    b.withMessage(Message.INSTANCE_PROBLEM_NOT_DEPENDENCIES)
                            .withParameter("required", required.iterator().next());
                } else {
                    b.withMessage(Message.INSTANCE_PROBLEM_NOT_DEPENDENCIES_PLURAL)
                            .withParameter("required", required);
                }
                dispatcher.dispatchProblem(b.build());
                return Result.FALSE;
            } else {
                return Result.TRUE;
            }
        }

        @Override
        protected Result testMissingProperty(ProblemDispatcher dispatcher) {
            return dispatchMissingPropertyProblem(dispatcher);
        }
    }

    public static class NegatedEmptyDependentEvaluator extends AbstractPropertyDependentEvaluator {

        public NegatedEmptyDependentEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword,
                String propertyName) {
            super(context, schema, keyword, propertyName);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                String keyName = getParser().getString();
                if (keyName.equals(getPropertyName())) {
                    return dispatchProblem(dispatcher);
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return dispatchMissingPropertyProblem(dispatcher);
            }
            return Result.PENDING;
        }

        private Result dispatchProblem(ProblemDispatcher dispatcher) {
            Problem problem = newProblemBuilder()
                    .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                    .withParameter("required", getPropertyName())
                    .build();
            dispatcher.dispatchProblem(problem);
            return Result.FALSE;
        }
    }
}
