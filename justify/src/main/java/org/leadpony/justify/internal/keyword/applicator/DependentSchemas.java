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
package org.leadpony.justify.internal.keyword.applicator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractPropertyDependentEvaluator;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.KeywordTypes;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

/**
 * @author leadpony
 */
public class DependentSchemas extends AbstractObjectApplicatorKeyword {

    static final KeywordType TYPE = KeywordTypes.mappingSchemaMap("dependentSchemas", DependentSchemas::new);

    private final Map<String, JsonSchema> schemaMap;
    private final Map<String, Dependent> dependentMap;

    public DependentSchemas(JsonValue json, Map<String, JsonSchema> schemaMap) {
        super(json);
        this.schemaMap = schemaMap;
        Map<String, Dependent> dependentMap = new LinkedHashMap<>();
        schemaMap.forEach((key, value) -> {
            dependentMap.put(key, createDependent(key, value));
        });
        this.dependentMap = dependentMap;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        LogicalEvaluator combined = Evaluators.conjunctive(parent, type);
        for (Dependent dependent : dependentMap.values()) {
            combined.append(p -> dependent.createEvaluator(p));
        }
        return combined;
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        LogicalEvaluator combined = Evaluators.disjunctive(parent, this, type);
        for (Dependent dependent : dependentMap.values()) {
            combined.append(p -> dependent.createNegatedEvaluator(p));
        }
        return combined;
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CURRENT;
    }

    @Override
    public Map<String, JsonSchema> getSchemasAsMap() {
        return schemaMap;
    }

    private Dependent createDependent(String propertyName, JsonSchema schema) {
        if (schema == JsonSchema.TRUE || schema == JsonSchema.EMPTY) {
            return new TrueDependent(propertyName, schema);
        } else if (schema == JsonSchema.FALSE) {
            return new FalseDependent(propertyName, schema);
        }
        return new Dependent(propertyName, schema);
    }

    private class Dependent {

        private final String propertyName;
        private final JsonSchema subschema;

        protected Dependent(String propertyName, JsonSchema subschema) {
            this.propertyName = propertyName;
            this.subschema = subschema;
        }

        final String getPropertyName() {
            return propertyName;
        }

        final JsonSchema getSubschema() {
            return subschema;
        }

        Evaluator createEvaluator(Evaluator parent) {
            Keyword keyword = DependentSchemas.this;
            return new DependentEvaluator(parent, keyword, this.propertyName, subschema);
        }

        Evaluator createNegatedEvaluator(Evaluator parent) {
            Keyword keyword = DependentSchemas.this;
            return new NegatedDependentEvaluator(parent, keyword, this.propertyName, subschema);
        }
    }

    private class TrueDependent extends Dependent {

        TrueDependent(String propertyName, JsonSchema schema) {
            super(propertyName, schema);
        }

        @Override
        Evaluator createEvaluator(Evaluator parent) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        Evaluator createNegatedEvaluator(Evaluator parent) {
            return Evaluator.alwaysFalse(parent, getSubschema());
        }
    }

    private class FalseDependent extends Dependent {

        FalseDependent(String propertyName, JsonSchema schema) {
            super(propertyName, JsonSchema.FALSE);
        }

        @Override
        Evaluator createEvaluator(Evaluator parent) {
            Keyword keyword = DependentSchemas.this;
            return new ForbiddenPropertyEvaluator(parent, keyword, getPropertyName());
        }
    }

    private abstract static class AbstractDependentEvaluator extends AbstractPropertyDependentEvaluator
            implements ProblemDispatcher {

        private final Evaluator internalEvaluator;
        private boolean active;
        private Result result;
        private List<Problem> problems;

        protected AbstractDependentEvaluator(Evaluator parent, Keyword keyword,
                String propertyName, JsonSchema subschema) {
            super(parent, keyword, propertyName);
            this.internalEvaluator = createInternalEvaluator(subschema);
        }

        @Override
        public Result evaluate(Event event, int depth) {
            if (!active) {
                if (depth == 1 && event == Event.KEY_NAME) {
                    String keyName = getParser().getString();
                    if (keyName.equals(getPropertyName())) {
                        active = true;
                    }
                }
            }
            if (this.result == null) {
                invokeInternalEvaluator(event, depth);
            }
            if (active) {
                if (result != null) {
                    if (result == Result.FALSE) {
                        dispatchAllProblems();
                    }
                    return result;
                }
                return Result.PENDING;
            } else {
                if (depth == 0 && event == Event.END_OBJECT) {
                    return testMissingProperty();
                } else {
                    return Result.PENDING;
                }
            }
        }

        @Override
        public ProblemDispatcher getDispatcherForChild(Evaluator evaluator) {
            return this;
        }

        @Override
        public void dispatchProblem(Problem problem) {
            if (problems == null) {
                problems = new ArrayList<>();
            }
            problems.add(problem);
        }

        private void invokeInternalEvaluator(Event event, int depth) {
            Result result = internalEvaluator.evaluate(event, depth);
            if (result != Result.PENDING) {
                this.result = result;
            }
        }

        private void dispatchAllProblems() {
            if (problems != null) {
                getDispatcher().dispatchAllProblems(problems);
            }
        }

        protected abstract Result testMissingProperty();

        protected abstract Evaluator createInternalEvaluator(JsonSchema schema);
    }

    public static class DependentEvaluator extends AbstractDependentEvaluator {

        public DependentEvaluator(Evaluator parent, Keyword keyword, String propertyName,
                JsonSchema subschema) {
            super(parent, keyword, propertyName, subschema);
        }

        @Override
        protected Result testMissingProperty() {
            return Result.TRUE;
        }

        @Override
        protected Evaluator createInternalEvaluator(JsonSchema schema) {
            return schema.createEvaluator(this, InstanceType.OBJECT);
        }
    }

    public static class NegatedDependentEvaluator extends AbstractDependentEvaluator {

        public NegatedDependentEvaluator(Evaluator parent, Keyword keyword,
                String propertyName,
                JsonSchema subschema) {
            super(parent, keyword, propertyName, subschema);
        }

        @Override
        protected Result testMissingProperty() {
            return dispatchMissingPropertyProblem();
        }

        @Override
        protected Evaluator createInternalEvaluator(JsonSchema schema) {
            return schema.createNegatedEvaluator(this, InstanceType.OBJECT);
        }
    }

    public static class ForbiddenPropertyEvaluator extends AbstractPropertyDependentEvaluator {

        public ForbiddenPropertyEvaluator(Evaluator parent, Keyword keyword,
                String propertyName) {
            super(parent, keyword, propertyName);
        }

        @Override
        public Result evaluate(Event event, int depth) {
            if (depth == 1 && event == Event.KEY_NAME) {
                if (getParser().getString().equals(getPropertyName())) {
                    return dispatchProblem();
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return Result.TRUE;
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
