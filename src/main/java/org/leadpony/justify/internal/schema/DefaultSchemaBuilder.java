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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.json.JsonBuilderFactory;
import javax.json.JsonValue;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.internal.base.Sets;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.annotation.Default;
import org.leadpony.justify.internal.keyword.annotation.Description;
import org.leadpony.justify.internal.keyword.annotation.Title;
import org.leadpony.justify.internal.keyword.assertion.Assertions;
import org.leadpony.justify.internal.keyword.combiner.Combiners;
import org.leadpony.justify.internal.keyword.combiner.Definitions;
import org.leadpony.justify.internal.keyword.combiner.Dependencies;
import org.leadpony.justify.internal.keyword.combiner.PatternProperties;
import org.leadpony.justify.internal.keyword.combiner.Properties;

/**
 * Default implementation of {@link JsonSchemaBuilder}.
 * 
 * @author leadpony
 */
class DefaultSchemaBuilder implements EnhancedSchemaBuilder {
    
    private final JsonBuilderFactory builderFactory;
    
    private boolean empty;
    private URI id;
    private URI schema;
    private final Map<String, Keyword> keywords = new LinkedHashMap<>();
    private URI ref;
    
    public DefaultSchemaBuilder(JsonBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
        this.empty = true;
    }
    
    URI getId() {
        return id;
    }
    
    URI getSchema() {
        return schema;
    }
    
    Map<String, Keyword> getKeywordMap() {
        return keywords;
    }
    
    JsonBuilderFactory getBuilderFactory() {
        return builderFactory;
    }
    
    @Override
    public JsonSchema build() {
        linkAllKeywords();
        if (empty) {
            return JsonSchema.EMPTY;
        } else if (ref != null) {
            return new SchemaReference(ref, this.keywords, getBuilderFactory());
        } else {
            return new BasicSchema(this);
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
    public JsonSchemaBuilder withConst(JsonValue value) {
        Objects.requireNonNull(value, "value must not be null.");
        addKeyword(Assertions.const_(value));
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
        addKeyword(Assertions.enum_(values));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withType(InstanceType... types) {
        Objects.requireNonNull(types, "types must not be null.");
        if (types.length == 0) {
            throw new IllegalArgumentException("types must not be empty.");
        }
        return withType(Sets.asSet(types));
    }
    
    @Override
    public JsonSchemaBuilder withType(Set<InstanceType> types) {
        Objects.requireNonNull(types, "types must not be null.");
        if (types.isEmpty()) {
            throw new IllegalArgumentException("types must not be empty.");
        }
        addKeyword(Assertions.type(types));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withMaxProperties(int bound) {
        addKeyword(Assertions.maxProperties(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinProperties(int bound) {
        addKeyword(Assertions.minProperties(bound));
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
        addKeyword(Assertions.required(names));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMaximum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        addKeyword(Assertions.maximum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withExclusiveMaximum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        addKeyword(Assertions.exclusiveMaximum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinimum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        addKeyword(Assertions.minimum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withExclusiveMinimum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        addKeyword(Assertions.exclusiveMinimum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMultipleOf(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "divisor must not be null.");
        addKeyword(Assertions.multipleOf(divisor));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withMaxLength(int bound) {
        addKeyword(Assertions.maxLength(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinLength(int bound) {
        addKeyword(Assertions.minLength(bound));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null.");
        Pattern compiled = Pattern.compile(pattern);
        addKeyword(Assertions.pattern(compiled));
        return builderNonempty();
    }
    
    /* Validation Keywords for Arrays */
    
    @Override
    public JsonSchemaBuilder withItem(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.items(subschema));
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withItems(List<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        addKeyword(Combiners.items(subschemas));
        return builderWithSubschema();
    }
  
    @Override
    public JsonSchemaBuilder withAdditionalItems(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.additionalItems(subschema));
        return builderWithSubschema();
    } 

    @Override
    public JsonSchemaBuilder withMaxItems(int bound) {
        addKeyword(Assertions.maxItems(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinItems(int bound) {
        addKeyword(Assertions.minItems(bound));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withUniqueItems(boolean unique) {
        addKeyword(Assertions.uniqueItems(unique));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withContains(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.contains(subschema));
        return builderWithSubschema();
    }
    
    @Override
    public JsonSchemaBuilder withMaxContains(int value) {
        addKeyword(Combiners.maxContains(value));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinContains(int value) {
        addKeyword(Combiners.minContains(value));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withProperty(String name, JsonSchema subschema) {
        Objects.requireNonNull(name, "name must not be null.");
        Objects.requireNonNull(subschema, "subschema must not be null.");
        Properties properties = requireKeyword("properties", Combiners::properties);
        properties.addProperty(name, subschema);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withPatternProperty(String pattern, JsonSchema subschema) {
        Objects.requireNonNull(pattern, "pattern must not be null.");
        Objects.requireNonNull(subschema, "subschema must not be null.");
        Pattern compiled = Pattern.compile(pattern);
        PatternProperties properties = requireKeyword("patternProperties", Combiners::patternProperties);
        properties.addProperty(compiled, subschema);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withAdditionalProperties(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.additionalProperties(subschema));
        return builderWithSubschema();
    }
   
    @Override
    public JsonSchemaBuilder withDependency(String property, JsonSchema subschema) {
        Objects.requireNonNull(property, "property must not be null.");
        Objects.requireNonNull(subschema, "subschema must not be null.");
        Dependencies keyword = requireKeyword("dependencies", Combiners::dependencies);
        keyword.addDependency(property, subschema);
        return builderWithSubschema();
    }
    
    @Override
    public JsonSchemaBuilder withDependency(String property, Set<String> requiredProperties) {
        Objects.requireNonNull(property, "property must not be null.");
        Objects.requireNonNull(requiredProperties, "requiredProperties must not be null.");
        Dependencies keyword = requireKeyword("dependencies", Combiners::dependencies);
        keyword.addDependency(property, requiredProperties);
        return builderWithSubschema();
    }
    
    @Override
    public JsonSchemaBuilder withPropertyNames(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.propertyNames(subschema));
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
        addKeyword(Combiners.allOf(subschemas));
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
        addKeyword(Combiners.anyOf(subschemas));
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
        addKeyword(Combiners.oneOf(subschemas));
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withNot(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.not(subschema));
        return builderWithSubschema();
    }
  
    @Override
    public JsonSchemaBuilder withIf(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.if_(subschema));
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withThen(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.then_(subschema));
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withElse(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.else_(subschema));
        return builderWithSubschema();
    }
   
    @Override
    public JsonSchemaBuilder withDefinition(String name, JsonSchema schema) {
        Objects.requireNonNull(name, "name must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        Definitions definitions = requireKeyword("definitions", Combiners::definitions);
        definitions.addDefinition(name, schema);
        return builderWithSubschema();
    }
    
    @Override
    public JsonSchemaBuilder withTitle(String title) {
        Objects.requireNonNull(title, "title must not be null.");
        addKeyword(new Title(title));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withDescription(String description) {
        Objects.requireNonNull(description, "description must not be null.");
        addKeyword(new Description(description));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withDefault(JsonValue value) {
        Objects.requireNonNull(value, "value must not be null.");
        addKeyword(new Default(value));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withRef(URI ref) {
        Objects.requireNonNull(ref, "ref must not be null.");
        this.ref = ref;
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withUnknown(String name, JsonSchema subschema) {
        Objects.requireNonNull(name, "name must not be null.");
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addKeyword(Combiners.unknown(name, subschema));
        return builderWithSubschema();
    }

    private void addKeyword(Keyword keyword) {
        this.keywords.put(keyword.name(), keyword);
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Keyword> T requireKeyword(String name, Supplier<T> supplier) {
        T keyword = null;
        if (keywords.containsKey(name)) {
            keyword = (T)keywords.get(name);
        } else {
            keyword = supplier.get();
            keywords.put(name, keyword);
        }
        return keyword;
    }
  
    /**
     * Links all keywords found in the schema.
     */
    private void linkAllKeywords() {
        for (Keyword keyword : this.keywords.values()) {
            keyword.link(this.keywords);
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
