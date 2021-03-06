/*
 * Copyright 2018, 2020 the Justify authors.
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.InvalidKeywordException;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.SubschemaParser;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.json.JsonPointers;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.JsonSchemaMap;
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
public class Dependencies extends AbstractObjectApplicatorKeyword {

    public static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return "dependencies";
        }

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() != ValueType.OBJECT) {
                throw new InvalidKeywordException("Must be an object");
            }
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonValue> entry : jsonValue.asJsonObject().entrySet()) {
                String name = entry.getKey();
                JsonValue value = entry.getValue();
                if (value.getValueType() == ValueType.ARRAY) {
                    Set<String> properties = new LinkedHashSet<>();
                    for (JsonValue item : value.asJsonArray()) {
                        if (item.getValueType() != ValueType.STRING) {
                            throw new InvalidKeywordException("Must be a string");
                        }
                        properties.add(((JsonString) item).getString());
                    }
                    map.put(name,  properties);
                } else {
                    map.put(name, schemaParser.parseSubschema(value, name));
                }
            }
            return new Dependencies(jsonValue, map);
        }
    };

    private final JsonSchemaMap schemaMap;
    private final Map<String, Dependent> dependentMap;

    public Dependencies(JsonValue json, Map<String, Object> map) {
        super(json);
        JsonSchemaMap schemaMap = new JsonSchemaMap();
        Map<String, Dependent> dependentMap = new LinkedHashMap<>();
        map.forEach((property, value) -> {
            if (value instanceof JsonSchema) {
                JsonSchema schema = (JsonSchema) value;
                schemaMap.put(JsonPointers.encode(property), schema);
                dependentMap.put(property, createDependent(property, schema));
            } else if (value instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> requiredProperties = (Set<String>) value;
                dependentMap.put(property, createDependent(property, requiredProperties));
            }
        });
        this.schemaMap = schemaMap;
        this.dependentMap = dependentMap;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.conjunctive(parent, type);
        for (Dependent dependent : dependentMap.values()) {
            evaluator.append(p -> dependent.createEvaluator(p));
        }
        return evaluator;
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.disjunctive(parent, this, type);
        for (Dependent dependent : dependentMap.values()) {
            evaluator.append(p -> dependent.createNegatedEvaluator(p));
        }
        return evaluator;
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CURRENT;
    }

    @Override
    public Map<String, JsonSchema> getSchemasAsMap() {
        return schemaMap;
    }

    @Override
    public Optional<JsonSchema> findSchema(String jsonPointer) {
        return schemaMap.findSchema(jsonPointer);
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
        abstract Evaluator createEvaluator(Evaluator parent);

        /**
         * Creates a new negated version of evaluator for this dependent.
         *
         * @return newly created evaluator.
         */
        abstract Evaluator createNegatedEvaluator(Evaluator parent);
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
        Evaluator createEvaluator(Evaluator parent) {
            Keyword keyword = Dependencies.this;
            return new DependentSchemas.DependentEvaluator(parent, keyword, getProperty(), subschema);
        }

        @Override
        Evaluator createNegatedEvaluator(Evaluator parent) {
            Keyword keyword = Dependencies.this;
            return new DependentSchemas.NegatedDependentEvaluator(parent, keyword, getProperty(), subschema);
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
        Evaluator createEvaluator(Evaluator parent) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        Evaluator createNegatedEvaluator(Evaluator parent) {
            return Evaluator.alwaysFalse(parent, getSubschema());
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
        Evaluator createEvaluator(Evaluator parent) {
            Keyword keyword = Dependencies.this;
            return new DependentSchemas.ForbiddenPropertyEvaluator(parent, keyword, getProperty());
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
        Evaluator createEvaluator(Evaluator parent) {
            Keyword keyword = Dependencies.this;
            return new DependentRequired.DependentEvaluator(parent, keyword, getProperty(),
                    requiredProperties);
        }

        @Override
        Evaluator createNegatedEvaluator(Evaluator parent) {
            Keyword keyword = Dependencies.this;
            return new DependentRequired.NegatedDependentEvaluator(parent, keyword, getProperty(),
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
        Evaluator createEvaluator(Evaluator parent) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        Evaluator createNegatedEvaluator(Evaluator parent) {
            Keyword keyword = Dependencies.this;
            return new DependentRequired.NegatedEmptyDependentEvaluator(parent, keyword, getProperty());
        }
    }
}
