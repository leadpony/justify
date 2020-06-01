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
import java.util.Optional;
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

    private final Map<String, Dependent> dependentMap;

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
        Map<String, Dependent> dependentMap = new HashMap<>();
        map.forEach((property, value) -> {
            if (value instanceof JsonSchema) {
                dependentMap.put(property, createDependent(property, (JsonSchema) value));
            } else if (value instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> requiredProperties = (Set<String>) value;
                dependentMap.put(property, createDependent(property, requiredProperties));
            }
        });
        this.dependentMap = dependentMap;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.conjunctive(type);
        dependentMap.values().stream()
                .map(d -> d.createEvaluator(context, schema))
                .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.disjunctive(context, schema, this, type);
        dependentMap.values().stream()
                .map(d -> d.createNegatedEvaluator(context, schema))
                .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CURRENT;
    }

    @Override
    public boolean containsSchemas() {
        return dependentMap.values()
                .stream()
                .anyMatch(d -> d instanceof SchemaDependent);
    }

    @Override
    public Stream<JsonSchema> getSchemasAsStream() {
        return dependentMap.values().stream()
                .filter(d -> d instanceof SchemaDependent)
                .map(d -> (SchemaDependent) d)
                .map(SchemaDependent::getSubschema);
    }

    @Override
    public Optional<JsonSchema> findSchema(String token) {
        if (dependentMap.containsKey(token)) {
            Dependent dependent = dependentMap.get(token);
            if (dependent instanceof SchemaDependent) {
                SchemaDependent schemaDependent = (SchemaDependent) dependent;
                return Optional.of(schemaDependent.getSubschema());
            }
        }
        return Optional.empty();
    }

    private Dependent createDependent(String property, JsonSchema subschema) {
        if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
            return new TrueDependent(property, subschema);
        } else if (subschema == JsonSchema.FALSE) {
            return new FalseDependent(property);
        } else {
            return new SchemaDependent(property, subschema);
        }
    }

    private Dependent createDependent(String property, Set<String> requiredProperties) {
        if (requiredProperties.isEmpty()) {
            return new EmptyPropertyDependent(property);
        } else {
            return new PropertyDependent(property, requiredProperties);
        }
    }

    /**
     * The super type of all dependents.
     *
     * @author leadpony
     */
    private abstract static class Dependent {

        private final String property;

        protected Dependent(String property) {
            this.property = property;
        }

        String getProperty() {
            return property;
        }

        /**
         * Creates a new evaluator for this dependent.
         *
         * @return newly created evaluator.
         */
        abstract Evaluator createEvaluator(EvaluatorContext context, JsonSchema schema);

        /**
         * Creates a new negated version of evaluator for this dependent.
         *
         * @return newly created evaluator.
         */
        abstract Evaluator createNegatedEvaluator(EvaluatorContext context, JsonSchema schema);
    }

    /**
     * A dependent whose value is a JSON schema.
     *
     * @author leadpony
     */
    private class SchemaDependent extends Dependent {

        private final JsonSchema subschema;

        protected SchemaDependent(String property, JsonSchema subschema) {
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

        JsonSchema getSubschema() {
            return subschema;
        }
    }

    /**
     * @author leadpony
     */
    private final class TrueDependent extends SchemaDependent {

        private TrueDependent(String property, JsonSchema subschema) {
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
    private final class FalseDependent extends SchemaDependent {

        private FalseDependent(String property) {
            super(property, JsonSchema.FALSE);
        }

        @Override
        Evaluator createEvaluator(EvaluatorContext context, JsonSchema schema) {
            Keyword keyword = Dependencies.this;
            return new DependentSchemas.ForbiddenPropertyEvaluator(context, schema, keyword, getProperty());
        }
    }

    /**
     * A dependent whose value is an array of property names.
     *
     * @author leadpony
     */
    private class PropertyDependent extends Dependent {

        private final Set<String> requiredProperties;

        PropertyDependent(String property, Set<String> requiredProperties) {
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
    private class EmptyPropertyDependent extends PropertyDependent {

        EmptyPropertyDependent(String property) {
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
