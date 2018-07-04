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

package org.leadpony.justify.internal.schema;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.assertion.Assertions;
import org.leadpony.justify.internal.base.Sets;
import org.leadpony.justify.internal.base.SimpleJsonPointer;

/**
 * @author leadpony
 */
class DefaultSchemaBuilder implements SchemaReferenceBuilder {
    
    private final JsonProvider jsonProvider;
    
    private boolean empty;

    private URI id;
    private URI schema;
    
    private String title;
    private String description;
    private JsonValue defaultValue;
    
    private final List<Assertion> assertions = new ArrayList<>();
    
    private Map<String, JsonSchema> properties;
    private Map<Pattern, JsonSchema> patternProperties;
    private JsonSchema additionalProperties;
    
    private Object items;
    private JsonSchema additionalItems;

    private final List<SchemaComponent> subschemas = new ArrayList<>();
    
    private JsonSchema ifSchema;
    private JsonSchema thenSchema;
    private JsonSchema elseSchema;
    
    private final NavigableSchemaMap subschemaMap = new NavigableSchemaMap();
    
    private URI ref;
    
    public DefaultSchemaBuilder(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.empty = true;
    }
    
    URI getId() {
        return id;
    }
    
    URI getSchema() {
        return schema;
    }
    
    String getTitle() {
        return title;
    }

    String getDescription() {
        return description;
    }
    
    JsonValue getDefault() {
        return defaultValue;
    }

    List<Assertion> getAssertions() {
        return Collections.unmodifiableList(assertions);
    }
    
    ItemSchemaFinder getItemSchemaFinder() {
        if (items == null || items instanceof JsonSchema) {
            JsonSchema item = (JsonSchema)this.items;
            return ItemSchemaFinder.of(item);
        } else {
            @SuppressWarnings("unchecked")
            List<JsonSchema> items = (List<JsonSchema>)this.items;
            return ItemSchemaFinder.of(items, additionalItems);
        }
    }
    
    PropertySchemaFinder getPropertySchemaFinder() {
        return new PropertySchemaFinder(
                this.properties,
                this.patternProperties,
                this.additionalProperties);
    }
    
    List<SchemaComponent> getSubschemas() {
        return Collections.unmodifiableList(subschemas);
    }
    
    NavigableSchemaMap getSubschemaMap() {
        return subschemaMap;
    }
    
    @Override
    public JsonSchema build() {
        if (empty) {
            return JsonSchema.EMPTY;
        } else if (ref != null) {
            return new SchemaReference(ref, this.subschemaMap);
        } else if (subschemaMap.isEmpty()) {
            return new SimpleSchema(this);
        } else {
            addConditionalSchemaIfExists();
            return new CompositeSchema(this);
        }
    }

    @Override
    public JsonSchemaBuilder withId(URI id) {
        Objects.requireNonNull(id, "id must not be null.");
        this.id = id;
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withSchema(URI schema) {
        Objects.requireNonNull(schema, "schema must not be null.");
        this.schema = schema;
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withTitle(String title) {
        Objects.requireNonNull(title, "title must not be null.");
        this.title = title;
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withDescription(String description) {
        Objects.requireNonNull(description, "description must not be null.");
        this.description = description;
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withDefault(JsonValue value) {
        Objects.requireNonNull(value, "value must not be null.");
        this.defaultValue = value;
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withConst(JsonValue value) {
        Objects.requireNonNull(value, "value must not be null.");
        assertions.add(Assertions.const_(value, this.jsonProvider));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withEnum(JsonValue... values) {
        Objects.requireNonNull(values, "values must not be null.");
        return withEnum(Sets.asSet(values));
    }
    
    @Override
    public JsonSchemaBuilder withEnum(Set<JsonValue> values) {
        Objects.requireNonNull(values, "values must not be null.");
        assertions.add(Assertions.enum_(values, this.jsonProvider));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withType(InstanceType... types) {
        Objects.requireNonNull(types, "types must not be null.");
        return withType(Sets.asSet(types));
    }
    
    @Override
    public JsonSchemaBuilder withType(Set<InstanceType> types) {
        Objects.requireNonNull(types, "types must not be null.");
        assertions.add(Assertions.type(types));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withMaxProperties(int bound) {
        assertions.add(Assertions.maxProperties(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinProperties(int bound) {
        assertions.add(Assertions.minProperties(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withRequired(String... names) {
        Objects.requireNonNull(names, "names must not be null.");
        return withRequired(Sets.asSet(names));
    }

    @Override
    public JsonSchemaBuilder withRequired(Set<String> names) {
        Objects.requireNonNull(names, "names must not be null.");
        assertions.add(Assertions.required(names));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMaximum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        assertions.add(Assertions.maximum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withExclusiveMaximum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        assertions.add(Assertions.exclusiveMaximum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinimum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        assertions.add(Assertions.minimum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withExclusiveMinimum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        assertions.add(Assertions.exclusiveMinimum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMultipleOf(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "divisor must not be null.");
        assertions.add(Assertions.multipleOf(divisor));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withMaxLength(int bound) {
        assertions.add(Assertions.maxLength(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinLength(int bound) {
        assertions.add(Assertions.minLength(bound));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null.");
        Pattern compiled = Pattern.compile(pattern);
        assertions.add(Assertions.pattern(compiled));
        return builderNonempty();
    }
    
    /* Validation Keywords for Arrays */
    
    @Override
    public JsonSchemaBuilder withItem(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.items = subschema;
        registerSubschema("/items", subschema);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withItems(List<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        this.items = new ArrayList<JsonSchema>(subschemas);
        registerSubschemas("items", subschemas);
        return builderWithSubschema();
    }
  
    @Override
    public JsonSchemaBuilder withAdditionalItems(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.additionalItems = subschema;
        registerSubschema("/additionalItems", subschema);
        return builderWithSubschema();
    } 

    @Override
    public JsonSchemaBuilder withMaxItems(int bound) {
        assertions.add(Assertions.maxItems(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinItems(int bound) {
        assertions.add(Assertions.minItems(bound));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withUniqueItems(boolean unique) {
        assertions.add(Assertions.uniqueItems(unique, jsonProvider));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withContains(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        // TODO:
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withProperty(String name, JsonSchema subschema) {
        Objects.requireNonNull(name, "name must not be null.");
        Objects.requireNonNull(subschema, "subschema must not be null.");
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(name, subschema);
        registerSubschema(SimpleJsonPointer.concat("properties", name), subschema);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withPatternProperty(String pattern, JsonSchema subschema) {
        Objects.requireNonNull(pattern, "pattern must not be null.");
        Objects.requireNonNull(subschema, "subschema must not be null.");
        Pattern compiled = Pattern.compile(pattern);
        if (this.patternProperties == null) {
            this.patternProperties = new HashMap<>();
        }
        this.patternProperties.put(compiled, subschema);
        registerSubschema(SimpleJsonPointer.concat("additionalProperties", pattern), subschema);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withAdditionalProperties(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.additionalProperties = subschema;
        registerSubschema("/additionalProperties", subschema);
        return builderWithSubschema();
    }
    
    @Override
    public JsonSchemaBuilder withAllOf(JsonSchema... subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        return withAllOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withAllOf(Collection<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        this.subschemas.add(new AllOf(subschemas));
        registerSubschemas("allOf", subschemas);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withAnyOf(JsonSchema... subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        return withAnyOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withAnyOf(Collection<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        this.subschemas.add(new AnyOf(subschemas));
        registerSubschemas("anyOf", subschemas);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withOneOf(JsonSchema... subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        return withOneOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withOneOf(Collection<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        this.subschemas.add(new OneOf(subschemas));
        registerSubschemas("oneOf", subschemas);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withNot(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.subschemas.add(new Not(subschema));
        registerSubschema("/not", subschema);
        return builderWithSubschema();
    }
  
    @Override
    public JsonSchemaBuilder withIf(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.ifSchema = subschema;
        registerSubschema("/if", subschema);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withThen(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.thenSchema = subschema;
        registerSubschema("/then", subschema);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withElse(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.elseSchema = subschema;
        registerSubschema("/else", subschema);
        return builderWithSubschema();
    }
   
    @Override
    public JsonSchemaBuilder withDefinition(String name, JsonSchema schema) {
        Objects.requireNonNull(name, "name must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        registerSubschema(SimpleJsonPointer.concat("definitions", name), schema);
        return builderWithSubschema();
    }
    
    @Override
    public JsonSchemaBuilder withSubschema(String jsonPointer, JsonSchema subschema) {
        Objects.requireNonNull(jsonPointer, "jsonPointer must not be null.");
        Objects.requireNonNull(subschema, "subschema must not be null.");
        if (jsonPointer.isEmpty()) {
            throw new IllegalArgumentException("jsonPointer must not be empty.");
        }
        registerSubschema(jsonPointer, subschema);
        return builderWithSubschema();
    }
    
    @Override
    public JsonSchemaBuilder withRef(URI ref) {
        Objects.requireNonNull(ref, "ref must not be null.");
        this.ref = ref;
        return builderNonempty();
    }
  
    private void registerSubschema(String jsonPointer, JsonSchema subschema) {
        this.subschemaMap.put(jsonPointer, subschema);
    }
    
    private void registerSubschemas(String jsonPointer, Iterable<JsonSchema> subschemas) {
        SimpleJsonPointer basePointer = SimpleJsonPointer.of(jsonPointer);
        int i = 0;
        for (JsonSchema subschema : subschemas) {
            registerSubschema(basePointer.concat(i++).toString(), subschema);
        }
    }

    private void addConditionalSchemaIfExists() {
        if (ifSchema != null) {
            this.subschemas.add(new IfThenElse(ifSchema, thenSchema, elseSchema));
        }
    }

    /**
     * Marks this builder as non-empty.
     * 
     * @return this builder.
     */
    private JsonSchemaBuilder builderNonempty() {
        this.empty = false;
        return this;
    }
    
    /**
     * Marks this builder as having subschema.
     * 
     * @return this builder.
     */
    private JsonSchemaBuilder builderWithSubschema() {
        return builderNonempty();
    }
}
