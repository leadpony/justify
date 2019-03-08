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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.ObjectKeyword;
import org.leadpony.justify.internal.problem.DefaultProblemDispatcher;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * Combiner representing "dependencies" keyword.
 *
 * @author leadpony
 */
public class Dependencies extends Combiner implements ObjectKeyword {

    private final Map<String, Dependency> dependencyMap = new HashMap<>();

    Dependencies() {
    }

    @Override
    public String name() {
        return "dependencies";
    }

    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        LogicalEvaluator evaluator = Evaluators.conjunctive(type).withProblemBuilderFactory(this);
        dependencyMap.values().stream()
            .map(Dependency::createEvaluator)
            .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        LogicalEvaluator evaluator = Evaluators.disjunctive(type).withProblemBuilderFactory(this);
        dependencyMap.values().stream()
            .map(Dependency::createNegatedEvaluator)
            .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonObjectBuilder dependencyBuilder = builderFactory.createObjectBuilder();
        for (Dependency dependency : this.dependencyMap.values()) {
            dependency.addToJson(dependencyBuilder, builderFactory);
        }
        builder.add(name(), dependencyBuilder.build());
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
                .map(d->(SchemaDependency)d)
                .map(SchemaDependency::getSubschema);
    }

    /**
     * Adds a dependency whose value is a JSON schema.
     * @param property the key of the dependency.
     * @param subschema the value of the dependency.
     */
    public void addDependency(String property, JsonSchema subschema) {
        dependencyMap.put(property, newDependency(property, subschema));
    }

    /**
     * Adds a dependency whose value is a set of property names.
     * @param property the key of the dependency.
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
    private abstract class DependencyEvaluator implements Evaluator {

        protected final String property;
        protected boolean active;

        protected DependencyEvaluator(String property) {
            this.property = property;
            this.active = false;
        }

        protected Result getResultWithoutDependant(EvaluatorContext context, ProblemDispatcher dispatcher) {
            return Result.TRUE;
        }

        protected Evaluator.Result dispatchMissingDependantProblem(EvaluatorContext context, ProblemDispatcher dispatcher) {
            Problem p = createProblemBuilder(context)
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
    private static abstract class Dependency {

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
         * @return newly created evaluator.
         */
        abstract Evaluator createEvaluator();

        /**
         * Creates a new evaluator for the negation of this dependency.
         * @return newly created evaluator.
         */
        abstract Evaluator createNegatedEvaluator();

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

        private SchemaDependency(String property, JsonSchema subschema) {
            super(property);
            this.subschema = subschema;
        }

        @Override
        Evaluator createEvaluator() {
            Evaluator subschemaEvaluator = subschema.createEvaluator(InstanceType.OBJECT);
            return new SchemaDependencyEvaluator(getProperty(), subschemaEvaluator);
        }

        @Override
        Evaluator createNegatedEvaluator() {
            Evaluator subschemaEvaluator = subschema.createNegatedEvaluator(InstanceType.OBJECT);
            return new NegatedSchemaDependencyEvaluator(getProperty(), subschemaEvaluator);
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

    private class TrueSchemaDependency extends SchemaDependency {

        private TrueSchemaDependency(String property, JsonSchema subschema) {
            super(property, subschema);
        }

        Evaluator createEvaluator() {
            return Evaluators.alwaysTrue(getSubschema());
        }

        @Override
        Evaluator createNegatedEvaluator() {
            return Evaluators.alwaysFalse(getSubschema());
        }
    }

    private class FalseSchemaDependency extends SchemaDependency {

        private FalseSchemaDependency(String property) {
            super(property, JsonSchema.FALSE);
        }

        @Override
        Evaluator createEvaluator() {
            return new ForbiddenDependantEvaluator(getProperty());
        }
    }

    private class SchemaDependencyEvaluator extends DependencyEvaluator implements DefaultProblemDispatcher {

        private final Evaluator subschemaEvaluator;
        private Result result;
        private List<Problem> problems;

        SchemaDependencyEvaluator(String property, Evaluator subschemaEvaluator) {
            super(property);
            this.subschemaEvaluator = subschemaEvaluator;
        }

        @Override
        public Result evaluate(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
            if (!active) {
                if (depth == 1 && event == Event.KEY_NAME) {
                    String keyName = context.getParser().getString();
                    if (keyName.equals(property)) {
                        active = true;
                        dispatchAllProblems(dispatcher);
                    }
                }
            }
            if (this.result == null) {
                evaluateSubschema(event, context, depth, dispatcher);
            }
            if (active) {
                return (result != null) ? result : Result.PENDING;
            } else {
                if (depth == 0 && event == Event.END_OBJECT) {
                    return getResultWithoutDependant(context, dispatcher);
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

        private void evaluateSubschema(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
            Result result = subschemaEvaluator.evaluate(event, context, depth, active ? dispatcher : this);
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
    private class NegatedSchemaDependencyEvaluator extends SchemaDependencyEvaluator {

        NegatedSchemaDependencyEvaluator(String property, Evaluator subschemaEvaluator) {
            super(property, subschemaEvaluator);
        }

        @Override
        protected Result getResultWithoutDependant(EvaluatorContext context, ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(context, dispatcher);
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
        Evaluator createEvaluator() {
            return new PropertyDependencyEvaluator(getProperty(), requiredProperties);
        }

        @Override
        Evaluator createNegatedEvaluator() {
            return new NegatedPropertyDependencyEvaluator(getProperty(), requiredProperties);
        }

        @Override
        void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            JsonArrayBuilder valueBuilder = builderFactory.createArrayBuilder();
            requiredProperties.forEach(valueBuilder::add);
            builder.add(getProperty(), valueBuilder.build());
        }
    }

    private class EmptyPropertyDependency extends PropertyDependency {

        EmptyPropertyDependency(String property) {
            super(property, Collections.emptySet());
        }

        @Override
        Evaluator createEvaluator() {
            return createAlwaysTrueEvaluator();
        }

        @Override
        Evaluator createNegatedEvaluator() {
            return new NegatedForbiddenDependantEvaluator(getProperty());
        }
    }

    private class PropertyDependencyEvaluator extends DependencyEvaluator {

        protected final Set<String> required;
        protected final Set<String> missing;

        PropertyDependencyEvaluator(String property, Set<String> required) {
            super(property);
            this.required = required;
            this.missing = new LinkedHashSet<>(required);
        }

        @Override
        public Result evaluate(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                String keyName = context.getParser().getString();
                if (keyName.equals(property)) {
                    active = true;
                }
                missing.remove(keyName);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (active) {
                    return test(context, dispatcher);
                } else {
                    return getResultWithoutDependant(context, dispatcher);
                }
            }
            return Result.PENDING;
        }

        protected Result test(EvaluatorContext context, ProblemDispatcher dispatcher) {
            if (missing.isEmpty()) {
                return Result.TRUE;
            } else {
                for (String entry : missing) {
                    Problem p = createProblemBuilder(context)
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

    private class NegatedPropertyDependencyEvaluator extends PropertyDependencyEvaluator {

        NegatedPropertyDependencyEvaluator(String property, Set<String> required) {
            super(property, required);
        }

        @Override
        protected Result getResultWithoutDependant(EvaluatorContext context, ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(context, dispatcher);
        }

        @Override
        protected Result test(EvaluatorContext context, ProblemDispatcher dispatcher) {
            if (required.isEmpty()) {
                Problem p = createProblemBuilder(context)
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                        .withParameter("required", this.property)
                        .build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            } else if (missing.isEmpty()) {
                ProblemBuilder b = createProblemBuilder(context)
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
     * Evaluator which dispatches a problem when it encounters the forbidden key.
     *
     * @author leadpony
     */
    private class ForbiddenDependantEvaluator extends DependencyEvaluator {

        ForbiddenDependantEvaluator(String property) {
            super(property);
        }

        @Override
        public Result evaluate(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                if (context.getParser().getString().equals(property)) {
                    return dispatchProblem(context, dispatcher);
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return getResultWithoutDependant(context, dispatcher);
            }
            return Result.PENDING;
        }

        private Result dispatchProblem(EvaluatorContext context, ProblemDispatcher dispatcher) {
            Problem problem = createProblemBuilder(context)
                    .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                    .withParameter("required", this.property)
                    .build();
            dispatcher.dispatchProblem(problem);
            return Result.FALSE;
        }
    }

    private class NegatedForbiddenDependantEvaluator extends ForbiddenDependantEvaluator {

        NegatedForbiddenDependantEvaluator(String property) {
            super(property);
        }

        @Override
        protected Result getResultWithoutDependant(EvaluatorContext context, ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(context, dispatcher);
        }
    }
}
