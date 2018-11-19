/*
 * Copyright 2018 the Justify authors.
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
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.DefaultProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.keyword.ObjectKeyword;
import org.leadpony.justify.internal.evaluator.AppendableLogicalEvaluator;

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
        AppendableLogicalEvaluator evaluator = Evaluators.conjunctive(type);
        dependencyMap.values().stream()
            .map(Dependency::createEvaluator)
            .forEach(evaluator::append);
        return evaluator.withProblemBuilderFactory(this);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        AppendableLogicalEvaluator evaluator = Evaluators.disjunctive(type);
        dependencyMap.values().stream()
            .map(Dependency::createNegatedEvaluator)
            .forEach(evaluator::append);
        return evaluator.withProblemBuilderFactory(this);
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
    public boolean hasSubschemas() {
        return dependencyMap.values().stream().anyMatch(Dependency::hasSubschema);
    }
  
    @Override
    public Stream<JsonSchema> subschemas() {
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
        if (subschema == JsonSchema.TRUE) {
            return new TrueSchemaDependency(property);
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

        protected Result getResultWithoutDependant(JsonParser parser, ProblemDispatcher dispatcher) {
            return Result.TRUE;
        }
        
        protected Evaluator.Result dispatchMissingDependantProblem(JsonParser parser, ProblemDispatcher dispatcher) {
            Problem p = createProblemBuilder(parser)
                    .withMessage("instance.problem.required")
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
        
        private TrueSchemaDependency(String property) {
            super(property, JsonSchema.TRUE);
        }

        Evaluator createEvaluator() {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        Evaluator createNegatedEvaluator() {
            return getSubschema().createAlwaysFalseEvaluator();
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
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            if (!active) {
                if (depth == 1 && event == Event.KEY_NAME) {
                    String keyName = parser.getString();
                    if (keyName.equals(property)) {
                        active = true;
                        dispatchAllProblems(dispatcher);
                    }
                }
            }
            if (this.result == null) {
                evaluateSubschema(event, parser, depth, dispatcher);
            }
            if (active) {
                return (result != null) ? result : Result.PENDING;
            } else {
                if (depth == 0 && event == Event.END_OBJECT) {
                    return getResultWithoutDependant(parser, dispatcher);
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
        
        private void evaluateSubschema(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            Result result = subschemaEvaluator.evaluate(event, parser, depth, active ? dispatcher : this);
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
        protected Result getResultWithoutDependant(JsonParser parser, ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(parser, dispatcher);
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
            return Evaluator.ALWAYS_TRUE;
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
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                String keyName = parser.getString();
                if (keyName.equals(property)) {
                    active = true;
                }
                missing.remove(keyName);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (active) {
                    return test(parser, dispatcher);
                } else {
                    return getResultWithoutDependant(parser, dispatcher);
                }
            }
            return Result.PENDING;
        }
        
        protected Result test(JsonParser parser, ProblemDispatcher dispatcher) {
            if (missing.isEmpty()) {
                return Result.TRUE;
            } else {
                for (String entry : missing) {
                    Problem p = createProblemBuilder(parser)
                            .withMessage("instance.problem.dependencies")
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
        protected Result getResultWithoutDependant(JsonParser parser, ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(parser, dispatcher);
        }
        
        @Override
        protected Result test(JsonParser parser, ProblemDispatcher dispatcher) {
            if (required.isEmpty()) {
                Problem p = createProblemBuilder(parser)
                        .withMessage("instance.problem.not.required")
                        .withParameter("required", this.property)
                        .build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            } else if (missing.isEmpty()) {
                ProblemBuilder b = createProblemBuilder(parser)
                        .withParameter("dependant", property);
                if (required.size() == 1) {
                    b.withMessage("instance.problem.not.dependencies")
                     .withParameter("required", required.iterator().next()); 
                } else {
                    b.withMessage("instance.problem.not.dependencies.plural")
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
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1 && event == Event.KEY_NAME) {
                if (parser.getString().equals(property)) {
                    return dispatchProblem(parser, dispatcher);
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return getResultWithoutDependant(parser, dispatcher);
            }
            return Result.PENDING;
        }
        
        private Result dispatchProblem(JsonParser parser, ProblemDispatcher dispatcher) {
            Problem problem = createProblemBuilder(parser)
                    .withMessage("instance.problem.not.required")
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
        protected Result getResultWithoutDependant(JsonParser parser, ProblemDispatcher dispatcher) {
            return dispatchMissingDependantProblem(parser, dispatcher);
        }
    }
}
