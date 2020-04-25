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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.spi.JsonProvider;
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
import org.leadpony.justify.internal.evaluator.AbstractEvaluator;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.ObjectKeyword;
import org.leadpony.justify.internal.problem.DefaultProblemDispatcher;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * A keyword type representing "dependencies".
 *
 * @author leadpony
 */
@KeywordType("dependencies")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Dependencies extends Applicator implements ObjectKeyword {

    private final Map<String, Dependency> dependencyMap;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.OBJECT) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (Map.Entry<String, JsonValue> entry : value.asJsonObject().entrySet()) {
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
                        map.put(k, context.asJsonSchema(v));
                    }
                }
                return new Dependencies(value, map);
            }
            throw new IllegalArgumentException();
        };
    }

    public Dependencies(JsonValue json, Map<String, Object> map) {
        super(json);
        dependencyMap = new HashMap<>();
        map.forEach((property, value) -> {
            if (value instanceof JsonSchema) {
                addDependency(property, (JsonSchema) value);
            } else if (value instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> requiredProperties = (Set<String>) value;
                addDependency(property, requiredProperties);
            }
        });
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.conjunctive(type);
        dependencyMap.values().stream()
                .map(d -> d.createEvaluator(context))
                .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.disjunctive(context, type).withProblemBuilderFactory(this);
        dependencyMap.values().stream()
                .map(d -> d.createNegatedEvaluator(context))
                .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    public boolean isInPlace() {
        return true;
    }

    @Override
    public boolean hasSubschemas() {
        return dependencyMap.values().stream().anyMatch(Dependency::hasSubschema);
    }

    @Override
    public Stream<JsonSchema> getSubschemas() {
        return dependencyMap.values().stream()
                .filter(Dependency::hasSubschema)
                .map(d -> (SchemaDependency) d)
                .map(SchemaDependency::getSubschema);
    }

    /**
     * Adds a dependency whose value is a JSON schema.
     *
     * @param property  the key of the dependency.
     * @param subschema the value of the dependency.
     */
    public void addDependency(String property, JsonSchema subschema) {
        dependencyMap.put(property, newDependency(property, subschema));
    }

    /**
     * Adds a dependency whose value is a set of property names.
     *
     * @param property           the key of the dependency.
     * @param requiredProperties the names of the required properties.
     */
    public void addDependency(String property, Set<String> requiredProperties) {
        dependencyMap.put(property, newDependency(property, requiredProperties));
    }

    private Dependency newDependency(String property, JsonSchema subschema) {
        if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
            return new TrueSchemaDependency(property, subschema);
        } else if (subschema == JsonSchema.FALSE) {
            return new FalseSchemaDependency(property);
        } else {
            return new SchemaDependency(property, subschema);
        }
    }

    private Dependency newDependency(String property, Set<String> requiredProperties) {
        if (requiredProperties.isEmpty()) {
            return new EmptyPropertyDependency(property);
        } else {
            return new PropertyDependency(property, requiredProperties);
        }
    }

    /**
     * Base evaluator for dependency.
     *
     * @author leadpony
     */
    private abstract class DependencyEvaluator extends AbstractEvaluator {

        protected final String property;
        protected boolean active;

        protected DependencyEvaluator(EvaluatorContext context, String property) {
            super(context);
            this.property = property;
            this.active = false;
        }

        protected Result getResultWithoutDependant(ProblemDispatcher dispatcher) {
            return Result.TRUE;
        }

        protected Evaluator.Result dispatchMissingDependantProblem(ProblemDispatcher dispatcher) {
            Problem p = createProblemBuilder(getContext())
                    .withMessage(Message.INSTANCE_PROBLEM_REQUIRED)
                    .withParameter("required", property)
                    .build();
            dispatcher.dispatchProblem(p);
            return Evaluator.Result.FALSE;
        }
    }

    /**
     * Super type of dependencies.
     *
     * @author leadpony
     */
    private abstract static class Dependency {

        private final String property;

        protected Dependency(String property) {
            this.property = property;
        }

        String getProperty() {
            return property;
        }

        boolean hasSubschema() {
            return false;
        }

        /**
         * Creates a new evaluator for this dependency.
         *
         * @return newly created evaluator.
         */
        abstract Evaluator createEvaluator(EvaluatorContext context);

        /**
         * Creates a new evaluator for the negation of this dependency.
         *
         * @return newly created evaluator.
         */
        abstract Evaluator createNegatedEvaluator(EvaluatorContext context);

        abstract JsonValue getValue(JsonProvider jsonProvider);

        abstract void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory);
    }

    /**
     * A dependency whose value is a JSON schema.
     *
     * @author leadpony
     */
    private class SchemaDependency extends Dependency {

        /*
         * The subschema to be evaluated.
         */
        private final JsonSchema subschema;

        protected SchemaDependency(String property, JsonSchema subschema) {
            super(property);
            this.subschema = subschema;
        }

        @Override
        Evaluator createEvaluator(EvaluatorContext context) {
            Evaluator subschemaEvaluator = subschema.createEvaluator(context, InstanceType.OBJECT);
            return new SchemaDependencyEvaluator(context, getProperty(), subschemaEvaluator);
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context) {
            Evaluator subschemaEvaluator = subschema.createNegatedEvaluator(context, InstanceType.OBJECT);
            return new NegatedSchemaDependencyEvaluator(context, getProperty(), subschemaEvaluator);
        }

        @Override
        JsonValue getValue(JsonProvider jsonProvider) {
            return subschema.toJson();
        }

        @Override
        void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            builder.add(getProperty(), subschema.toJson());
        }

        @Override
        boolean hasSubschema() {
            return true;
        }

        JsonSchema getSubschema() {
            return subschema;
        }
    }

    /**
     * @author leadpony
     */
    private final class TrueSchemaDependency extends SchemaDependency {

        private TrueSchemaDependency(String property, JsonSchema subschema) {
            super(property, subschema);
        }

        Evaluator createEvaluator(EvaluatorContext context) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context) {
            return Evaluators.alwaysFalse(getSubschema(), context);
        }
    }

    /**
     * @author leadpony
     */
    private final class FalseSchemaDependency extends SchemaDependency {

        private FalseSchemaDependency(String property) {
            super(property, JsonSchema.FALSE);
        }

        @Override
        Evaluator createEvaluator(EvaluatorContext context) {
            return new ForbiddenDependantEvaluator(context, getProperty());
        }
    }

    /**
     * @author leadpony
     */
    private class SchemaDependencyEvaluator extends DependencyEvaluator implements DefaultProblemDispatcher {

        private final Evaluator subschemaEvaluator;
        private Result result;
        private List<Problem> problems;

        SchemaDependencyEvaluator(EvaluatorContext context, String property, Evaluator subschemaEvaluator) {
            super(context, property);
            this.subschemaEvaluator = subschemaEvaluator;
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            if (!active) {
                if (depth == 1 && event == Event.KEY_NAME) {
                    String keyName = getParser().getString();
                    if (keyName.equals(property)) {
                        active = true;
                        dispatchAllProblems(dispatcher);
                    }
                }
            }
            if (this.result == null) {
                evaluateSubschema(event, depth, dispatcher);
            }
            if (active) {
                return (result != null) ? result : Result.PENDING;
            } else {
                if (depth == 0 && event == Event.END_OBJECT) {
                    return getResultWithoutDependant(dispatcher);
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

        private void evaluateSubschema(Event event, int depth, ProblemDispatcher dispatcher) {
            Result result = subschemaEvaluator.evaluate(event, depth, active ? dispatcher : this);
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
    }

    /**
     * Negated version of {@link SchemaDependencyEvaluator}.
     *
     * @author leadpony
     */
    private final class NegatedSchemaDependencyEvaluator extends SchemaDependencyEvaluator {

        NegatedSchemaDependencyEvaluator(EvaluatorContext context, String property, Evaluator subschemaEvaluator) {
            super(context, property, subschemaEvaluator);
        }

        @Override
        protected Result getResultWithoutDependant(ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(dispatcher);
        }
    }

    /**
     * A dependency whose value is an array of property names.
     *
     * @author leadpony
     */
    private class PropertyDependency extends Dependency {

        private final Set<String> requiredProperties;

        PropertyDependency(String property, Set<String> requiredProperties) {
            super(property);
            this.requiredProperties = requiredProperties;
        }

        @Override
        Evaluator createEvaluator(EvaluatorContext context) {
            return new PropertyDependencyEvaluator(context, getProperty(), requiredProperties);
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context) {
            return new NegatedPropertyDependencyEvaluator(context, getProperty(), requiredProperties);
        }

        @Override
        JsonValue getValue(JsonProvider jsonProvider) {
            JsonArrayBuilder builder = jsonProvider.createArrayBuilder();
            this.requiredProperties.forEach(builder::add);
            return builder.build();
        }

        @Override
        void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            JsonArrayBuilder valueBuilder = builderFactory.createArrayBuilder();
            requiredProperties.forEach(valueBuilder::add);
            builder.add(getProperty(), valueBuilder.build());
        }
    }

    /**
     * @author leadpony
     */
    private class EmptyPropertyDependency extends PropertyDependency {

        EmptyPropertyDependency(String property) {
            super(property, Collections.emptySet());
        }

        @Override
        Evaluator createEvaluator(EvaluatorContext context) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context) {
            return new NegatedForbiddenDependantEvaluator(context, getProperty());
        }
    }

    /**
     * An evaluator which will check dependency properties.
     *
     * @author leadpony
     */
    private class PropertyDependencyEvaluator extends DependencyEvaluator {

        protected final Set<String> required;
        protected final Set<String> missing;

        PropertyDependencyEvaluator(EvaluatorContext context, String property, Set<String> required) {
            super(context, property);
            this.required = required;
            this.missing = new LinkedHashSet<>(required);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                String keyName = getParser().getString();
                if (keyName.equals(property)) {
                    active = true;
                }
                missing.remove(keyName);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (active) {
                    return test(dispatcher);
                } else {
                    return getResultWithoutDependant(dispatcher);
                }
            }
            return Result.PENDING;
        }

        protected Result test(ProblemDispatcher dispatcher) {
            if (missing.isEmpty()) {
                return Result.TRUE;
            } else {
                for (String entry : missing) {
                    Problem p = createProblemBuilder(getContext())
                            .withMessage(Message.INSTANCE_PROBLEM_DEPENDENCIES)
                            .withParameter("required", entry)
                            .withParameter("dependant", property)
                            .build();
                    dispatcher.dispatchProblem(p);
                }
                return Result.FALSE;
            }
        }
    }

    /**
     * @author leadpony
     */
    private class NegatedPropertyDependencyEvaluator extends PropertyDependencyEvaluator {

        NegatedPropertyDependencyEvaluator(EvaluatorContext context, String property, Set<String> required) {
            super(context, property, required);
        }

        @Override
        protected Result getResultWithoutDependant(ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(dispatcher);
        }

        @Override
        protected Result test(ProblemDispatcher dispatcher) {
            if (required.isEmpty()) {
                Problem p = createProblemBuilder(getContext())
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                        .withParameter("required", this.property)
                        .build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            } else if (missing.isEmpty()) {
                ProblemBuilder b = createProblemBuilder(getContext())
                        .withParameter("dependant", property);
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
    }

    /**
     * An evaluator which dispatches a problem when it encounters the forbidden key.
     *
     * @author leadpony
     */
    private class ForbiddenDependantEvaluator extends DependencyEvaluator {

        ForbiddenDependantEvaluator(EvaluatorContext context, String property) {
            super(context, property);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                if (getParser().getString().equals(property)) {
                    return dispatchProblem(dispatcher);
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return getResultWithoutDependant(dispatcher);
            }
            return Result.PENDING;
        }

        private Result dispatchProblem(ProblemDispatcher dispatcher) {
            Problem problem = createProblemBuilder(getContext())
                    .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                    .withParameter("required", this.property)
                    .build();
            dispatcher.dispatchProblem(problem);
            return Result.FALSE;
        }
    }

    /**
     * @author leadpony
     */
    private class NegatedForbiddenDependantEvaluator extends ForbiddenDependantEvaluator {

        NegatedForbiddenDependantEvaluator(EvaluatorContext context, String property) {
            super(context, property);
        }

        @Override
        protected Result getResultWithoutDependant(ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(dispatcher);
        }
    }
}
