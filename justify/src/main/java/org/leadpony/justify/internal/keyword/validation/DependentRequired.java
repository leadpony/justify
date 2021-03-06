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
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.keyword.InvalidKeywordException;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.SubschemaParser;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractPropertyDependentEvaluator;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.AbstractObjectAssertionKeyword;
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
public class DependentRequired extends AbstractObjectAssertionKeyword {

    static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return "dependentRequired";
        }

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() != ValueType.OBJECT) {
                throw new InvalidKeywordException("Must be an object");
            }
            Map<String, Set<String>> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonValue> entry : jsonValue.asJsonObject().entrySet()) {
                JsonValue entryValue = entry.getValue();
                if (entryValue.getValueType() != ValueType.ARRAY) {
                    throw new InvalidKeywordException("Must be an array");
                }
                Set<String> properties = new LinkedHashSet<>();
                for (JsonValue item : entryValue.asJsonArray()) {
                    if (item.getValueType() != ValueType.STRING) {
                        throw new InvalidKeywordException("Must be a string");
                    }
                    properties.add(((JsonString) item).getString());
                }
                map.put(entry.getKey(), properties);
            }
            return new DependentRequired(jsonValue, map);
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
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        LogicalEvaluator combined = Evaluators.conjunctive(parent, type);
        for (Dependent dependent : map.values()) {
            combined.append(p -> dependent.createEvaluator(p));
        }
        return combined;
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        LogicalEvaluator combined = Evaluators.disjunctive(parent, this, type);
        for (Dependent dependent : map.values()) {
            combined.append(p -> dependent.createNegatedEvaluator(p));
        }
        return combined;
    }

    private class Dependent {

        protected final String propertyName;

        Dependent(String propertyName) {
            this.propertyName = propertyName;
        }

        Evaluator createEvaluator(Evaluator parent) {
            return Evaluator.ALWAYS_TRUE;
        }

        Evaluator createNegatedEvaluator(Evaluator parent) {
            return new NegatedEmptyDependentEvaluator(parent, DependentRequired.this, propertyName);
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
        Evaluator createEvaluator(Evaluator parent) {
            return new DependentEvaluator(parent, DependentRequired.this, propertyName, required);
        }

        @Override
        Evaluator createNegatedEvaluator(Evaluator parent) {
            return new NegatedDependentEvaluator(parent, DependentRequired.this, propertyName, required);
        }
    }

    private abstract static class AbstractDependentEvaluator extends AbstractPropertyDependentEvaluator {

        protected final Set<String> required;
        protected final Set<String> missing;
        protected boolean active;

        protected AbstractDependentEvaluator(Evaluator parent, Keyword keyword,
                String propertyName, Set<String> required) {
            super(parent, keyword, propertyName);
            this.required = required;
            this.missing = new LinkedHashSet<>(required);
        }

        @Override
        public Result evaluate(Event event, int depth) {
            if (depth == 1 && event == Event.KEY_NAME) {
                String keyName = getParser().getString();
                if (keyName.equals(getPropertyName())) {
                    active = true;
                }
                missing.remove(keyName);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (active) {
                    return test();
                } else {
                    return testMissingProperty();
                }
            }
            return Result.PENDING;
        }

        protected abstract Result test();

        protected abstract Result testMissingProperty();
    }

    public static class DependentEvaluator extends AbstractDependentEvaluator {

        public DependentEvaluator(Evaluator parent, Keyword keyword, String propertyName,
                Set<String> required) {
            super(parent, keyword, propertyName, required);
        }

        @Override
        protected Result test() {
            if (missing.isEmpty()) {
                return Result.TRUE;
            } else {
                for (String entry : missing) {
                    Problem p = newProblemBuilder()
                            .withMessage(Message.INSTANCE_PROBLEM_DEPENDENCIES)
                            .withParameter("required", entry)
                            .withParameter("dependant", getPropertyName())
                            .build();
                    getDispatcher().dispatchProblem(p);
                }
                return Result.FALSE;
            }
        }

        @Override
        protected Result testMissingProperty() {
            return Result.TRUE;
        }
    }

    public static class NegatedDependentEvaluator extends AbstractDependentEvaluator {

        public NegatedDependentEvaluator(Evaluator parent, Keyword keyword,
                String propertyName, Set<String> required) {
            super(parent, keyword, propertyName, required);
        }

        @Override
        protected Result test() {
            if (required.isEmpty()) {
                Problem p = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                        .withParameter("required", getPropertyName())
                        .build();
                getDispatcher().dispatchProblem(p);
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
                getDispatcher().dispatchProblem(b.build());
                return Result.FALSE;
            } else {
                return Result.TRUE;
            }
        }

        @Override
        protected Result testMissingProperty() {
            return dispatchMissingPropertyProblem();
        }
    }

    public static class NegatedEmptyDependentEvaluator extends AbstractPropertyDependentEvaluator {

        public NegatedEmptyDependentEvaluator(Evaluator parent, Keyword keyword,
                String propertyName) {
            super(parent, keyword, propertyName);
        }

        @Override
        public Result evaluate(Event event, int depth) {
            if (depth == 1 && event == Event.KEY_NAME) {
                String keyName = getParser().getString();
                if (keyName.equals(getPropertyName())) {
                    return dispatchProblem();
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return dispatchMissingPropertyProblem();
            }
            return Result.PENDING;
        }

        private Result dispatchProblem() {
            Problem problem = newProblemBuilder()
                    .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                    .withParameter("required", getPropertyName())
                    .build();
            getDispatcher().dispatchProblem(problem);
            return Result.FALSE;
        }
    }
}
