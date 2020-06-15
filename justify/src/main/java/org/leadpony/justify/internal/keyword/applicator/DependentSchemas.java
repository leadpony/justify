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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
import org.leadpony.justify.internal.keyword.ObjectEvaluatorSource;
import org.leadpony.justify.internal.problem.DefaultProblemDispatcher;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

/**
 * @author leadpony
 */
public class DependentSchemas extends AbstractApplicatorKeyword implements ObjectEvaluatorSource {

    static final KeywordType TYPE = KeywordTypes.mappingSchemaMap("dependentSchemas", DependentSchemas::new);

    private final Map<String, Dependent> dependentMap;

    public DependentSchemas(JsonValue json, Map<String, JsonSchema> map) {
        super(json);
        Map<String, Dependent> dependentMap = new HashMap<>();
        map.forEach((key, value) -> {
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
        LogicalEvaluator combined = Evaluators.conjunctive(type);
        dependentMap.values().stream()
                .map(d -> d.createEvaluator(parent))
                .forEach(combined::append);
        return combined;
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        LogicalEvaluator combined = Evaluators.disjunctive(parent, this, type);
        dependentMap.values().stream()
                .map(d -> d.createNegatedEvaluator(parent))
                .forEach(combined::append);
        return combined;
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CURRENT;
    }

    @Override
    public boolean containsSchemas() {
        return !dependentMap.isEmpty();
    }

    @Override
    public Stream<JsonSchema> getSchemasAsStream() {
        return dependentMap.values().stream().map(Dependent::getSubschema);
    }

    @Override
    public Optional<JsonSchema> findSchema(String token) {
        if (dependentMap.containsKey(token)) {
            return Optional.of(dependentMap.get(token).getSubschema());
        }
        return Optional.empty();
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
            return parent.getContext().createAlwaysFalseEvaluator(getSubschema());
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
            implements DefaultProblemDispatcher {

        private final Evaluator internalEvaluator;
        private boolean active;
        private Result result;
        private List<Problem> problems;

        protected AbstractDependentEvaluator(Evaluator parent, Keyword keyword,
                String propertyName, Evaluator internalEvaluator) {
            super(parent, keyword, propertyName);
            this.internalEvaluator = internalEvaluator;
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            if (!active) {
                if (depth == 1 && event == Event.KEY_NAME) {
                    String keyName = getParser().getString();
                    if (keyName.equals(getPropertyName())) {
                        active = true;
                        dispatchAllProblems(dispatcher);
                    }
                }
            }
            if (this.result == null) {
                invokeInternalEvaluator(event, depth, dispatcher);
            }
            if (active) {
                return (result != null) ? result : Result.PENDING;
            } else {
                if (depth == 0 && event == Event.END_OBJECT) {
                    return testMissingProperty(dispatcher);
                } else {
                    return Result.PENDING;
                }
            }
        }

        @Override
        public void dispatchProblem(Problem problem) {
            if (problems == null) {
                problems = new ArrayList<>();
            }
            problems.add(problem);
        }

        private void invokeInternalEvaluator(Event event, int depth, ProblemDispatcher dispatcher) {
            Result result = internalEvaluator.evaluate(event, depth, active ? dispatcher : this);
            if (result != Result.PENDING) {
                this.result = result;
            }
        }

        private void dispatchAllProblems(ProblemDispatcher dispatcher) {
            if (problems == null) {
                return;
            }
            for (Problem problem : problems) {
                dispatcher.dispatchProblem(problem);
            }
        }

        protected Result testMissingProperty(ProblemDispatcher dispatcher) {
            return Result.TRUE;
        }
    }

    public static class DependentEvaluator extends AbstractDependentEvaluator {

        public DependentEvaluator(Evaluator parent, Keyword keyword, String propertyName,
                JsonSchema subschema) {
            super(parent, keyword, propertyName,
                    subschema.createEvaluator(parent.getContext(), InstanceType.OBJECT));
        }

        @Override
        protected Result testMissingProperty(ProblemDispatcher dispatcher) {
            return Result.TRUE;
        }
    }

    public static class NegatedDependentEvaluator extends AbstractDependentEvaluator {

        public NegatedDependentEvaluator(Evaluator parent, Keyword keyword,
                String propertyName,
                JsonSchema subschema) {
            super(parent, keyword, propertyName,
                    subschema.createNegatedEvaluator(parent.getContext(), InstanceType.OBJECT));
        }

        @Override
        protected Result testMissingProperty(ProblemDispatcher dispatcher) {
            return dispatchMissingPropertyProblem(dispatcher);
        }
    }

    public static class ForbiddenPropertyEvaluator extends AbstractPropertyDependentEvaluator {

        public ForbiddenPropertyEvaluator(Evaluator parent, Keyword keyword,
                String propertyName) {
            super(parent, keyword, propertyName);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                if (getParser().getString().equals(getPropertyName())) {
                    return dispatchProblem(dispatcher);
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return Result.TRUE;
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
