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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemReporter;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * @author leadpony
 */
public class Dependencies implements Combiner {

    private final Map<String, Dependency> dependencyMap = new HashMap<>();
    
    Dependencies() {
    }

    @Override
    public String name() {
        return "dependencies";
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type != InstanceType.OBJECT) {
            return;
        }
        dependencyMap.values().stream()
            .map(Dependency::createEvaluator)
            .filter(Objects::nonNull)
            .forEach(appender::append);
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
        return dependencyMap.values().stream()
                .anyMatch(Dependency::hasSubschema);
    }
  
    @Override
    public Stream<JsonSchema> subschemas() {
        return dependencyMap.values().stream()
                .filter(Dependency::hasSubschema)
                .map(d->(SubschemaDependency)d)
                .map(SubschemaDependency::getSubschema);
    }
    
    
    public void addDependency(String property, JsonSchema subschema) {
        dependencyMap.put(property, new SubschemaDependency(property, subschema));
    }

    public void addDependency(String property, Set<String> requiredProperties) {
        dependencyMap.put(property, new PropertyDependency(property, requiredProperties));
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

        abstract Evaluator createEvaluator();
        
        abstract void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory);
    }
    
    private static class SubschemaDependency extends Dependency {
        
        private final JsonSchema subschema;

        private SubschemaDependency(String property, JsonSchema subschema) {
            super(property);
            this.subschema = subschema;
        }
        
        @Override
        Evaluator createEvaluator() {
            Evaluator evaluator = subschema.createEvaluator(InstanceType.OBJECT);
            if (evaluator != null) {
                return new SubschemaEvaluator(getProperty(), evaluator);
            } else {
                return null;
            }
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
    
    private static class SubschemaEvaluator implements Evaluator, ProblemReporter {

        private final String property;
        private boolean active;
        private final Evaluator realEvaluator;
        private Result result;
        private List<Problem> problems;
        
        SubschemaEvaluator(String property, Evaluator realEvaluator) {
            this.property = property;
            this.active = false;
            this.realEvaluator = realEvaluator;
        }
        
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
            if (!active) {
                if (depth == 1 && event == Event.KEY_NAME) {
                    String keyName = parser.getString();
                    if (keyName.equals(property)) {
                        active = true;
                        dispatchAllProblems(reporter);
                    }
                }
            }
            if (this.result == null) {
                Result result = realEvaluator.evaluate(event, parser, depth, active ? reporter : this);
                if (result != Result.PENDING) {
                    this.result = result;
                }
            }
            if (active) {
                return (result != null) ? result : Result.PENDING;
            } else {
                if (depth == 0 && event == Event.END_OBJECT) {
                    return Result.IGNORED;
                } else {
                    return Result.PENDING;
                }
            }
        }
        
        @Override
        public void reportProblem(Problem problem) {
            if (problems == null) {
                problems = new ArrayList<>();
            }
            problems.add(problem);
        }

        private void dispatchAllProblems(Reporter reporter) {
            if (problems == null) {
                return;
            }
            for (Problem problem : problems) {
                reporter.reportProblem(problem);
            }
        }
    }
    
    private static class PropertyDependency extends Dependency {
        
        private final Set<String> requiredProperties;

        PropertyDependency(String property, Set<String> requiredProperties) {
            super(property);
            this.requiredProperties = requiredProperties; 
        }

        @Override
        Evaluator createEvaluator() {
            return new PropertyEvaluator(getProperty(), requiredProperties);
        }

        @Override
        void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            JsonArrayBuilder valueBuilder = builderFactory.createArrayBuilder();
            requiredProperties.forEach(valueBuilder::add);
            builder.add(getProperty(), valueBuilder.build());
        }
    }
    
    private static class PropertyEvaluator implements Evaluator {
        
        private final String property;
        private boolean active;
        private final Set<String> missing;
        
        PropertyEvaluator(String property, Set<String> required) {
            this.property = property;
            this.active = false;
            this.missing = new LinkedHashSet<>(required);
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
            if (depth == 1 && event == Event.KEY_NAME) {
                String keyName = parser.getString();
                if (keyName.equals(property)) {
                    active = true;
                }
                missing.remove(keyName);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (active) {
                    return test(parser, reporter);
                } else {
                    return Result.IGNORED;
                }
            }
            return Result.PENDING;
        }
        
        private Result test(JsonParser parser, Reporter reporter) {
            if (missing.isEmpty()) {
                return Result.TRUE;
            } else {
                Problem p = ProblemBuilder.newBuilder(parser)
                        .withMessage("instance.problem.dependencies")
                        .withParameter("missing", missing)
                        .withParameter("dependant", property)
                        .build();
                reporter.reportProblem(p);
                return Result.FALSE;
            }
        }
    }
}
