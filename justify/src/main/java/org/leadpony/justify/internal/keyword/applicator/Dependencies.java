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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.ObjectEvaluatorSource;
import org.leadpony.justify.internal.keyword.validation.DependentRequired;

/**
 * A keyword type representing "dependencies".
 *
 * @author leadpony
 */
@KeywordClass("dependencies")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Dependencies extends AbstractApplicatorKeyword implements ObjectEvaluatorSource {

    public static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return "dependencies";
        }

        @Override
        public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
            return Dependencies.newInstance(jsonValue, context);
        }
    };

    private final Map<String, Dependency> dependencyMap;

    private static Dependencies newInstance(JsonValue jsonValue, KeywordType.CreationContext context) {
        if (jsonValue.getValueType() == ValueType.OBJECT) {
            Map<String, Object> map = new LinkedHashMap<>();
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
                    map.put(k, context.asJsonSchema(v));
                }
            }
            return new Dependencies(jsonValue, map);
        }
        throw new IllegalArgumentException();
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
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.conjunctive(type);
        dependencyMap.values().stream()
                .map(d -> d.createEvaluator(context, schema))
                .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.disjunctive(context, schema, this, type);
        dependencyMap.values().stream()
                .map(d -> d.createNegatedEvaluator(context, schema))
                .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CHILD;
    }

    @Override
    public boolean containsSchemas() {
        return dependencyMap.values().stream().anyMatch(Dependency::hasSubschema);
    }

    @Override
    public Stream<JsonSchema> getSchemas() {
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
        abstract Evaluator createEvaluator(EvaluatorContext context, JsonSchema schema);

        /**
         * Creates a new evaluator for the negation of this dependency.
         *
         * @return newly created evaluator.
         */
        abstract Evaluator createNegatedEvaluator(EvaluatorContext context, JsonSchema schema);
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
        Evaluator createEvaluator(EvaluatorContext context, JsonSchema schema) {
            Keyword keyword = Dependencies.this;
            return new DependentSchemas.DependentEvaluator(context, schema, keyword, getProperty(), subschema);
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context, JsonSchema schema) {
            Keyword keyword = Dependencies.this;
            return new DependentSchemas.NegatedDependentEvaluator(context, schema, keyword, getProperty(), subschema);
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

        @Override
        Evaluator createEvaluator(EvaluatorContext context, JsonSchema schema) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context, JsonSchema schema) {
            return context.createAlwaysFalseEvaluator(getSubschema());
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
        Evaluator createEvaluator(EvaluatorContext context, JsonSchema schema) {
            Keyword keyword = Dependencies.this;
            return new DependentSchemas.ForbiddenPropertyEvaluator(context, schema, keyword, getProperty());
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
        Evaluator createEvaluator(EvaluatorContext context, JsonSchema schema) {
            Keyword keyword = Dependencies.this;
            return new DependentRequired.DependentEvaluator(context, schema, keyword, getProperty(),
                    requiredProperties);
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context, JsonSchema schema) {
            Keyword keyword = Dependencies.this;
            return new DependentRequired.NegatedDependentEvaluator(context, schema, keyword, getProperty(),
                    requiredProperties);
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
        Evaluator createEvaluator(EvaluatorContext context, JsonSchema schema) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        Evaluator createNegatedEvaluator(EvaluatorContext context, JsonSchema schema) {
            Keyword keyword = Dependencies.this;
            return new DependentRequired.NegatedEmptyDependentEvaluator(context, schema, keyword, getProperty());
        }
    }
}
