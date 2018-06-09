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
import org.leadpony.justify.internal.assertion.Const;
import org.leadpony.justify.internal.assertion.EnumAssertion;
import org.leadpony.justify.internal.assertion.ExclusiveMaximum;
import org.leadpony.justify.internal.assertion.ExclusiveMinimum;
import org.leadpony.justify.internal.assertion.MaxItems;
import org.leadpony.justify.internal.assertion.MaxLength;
import org.leadpony.justify.internal.assertion.Maximum;
import org.leadpony.justify.internal.assertion.MinItems;
import org.leadpony.justify.internal.assertion.MinLength;
import org.leadpony.justify.internal.assertion.Minimum;
import org.leadpony.justify.internal.assertion.MultipleOf;
import org.leadpony.justify.internal.assertion.Required;
import org.leadpony.justify.internal.assertion.Type;
import org.leadpony.justify.internal.base.Sets;

/**
 * @author leadpony
 */
public class DefaultSchemaBuilder implements JsonSchemaBuilder {
    
    private final JsonProvider jsonProvider;
    
    private boolean empty;

    private URI id;
    private URI schema;
    
    private String title;
    private String description;
    private final List<Assertion> assertions = new ArrayList<>();
    
    private Map<String, JsonSchema> properties;
    private Map<Pattern, JsonSchema> patternProperties;
    private JsonSchema additionalProperties;
    
    private Object items;
    private JsonSchema additionalItems;

    private final List<JsonSchema> subschemas = new ArrayList<>();
    
    private JsonSchema ifSchema;
    private JsonSchema thenSchema;
    private JsonSchema elseSchema;
    
    private Map<String, JsonSchema> definitions;
    
    private final SubschemaMap subschemaMap = new SubschemaMap();
    
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
    
    List<JsonSchema> getSubschemas() {
        return Collections.unmodifiableList(subschemas);
    }
    
    Map<String, JsonSchema> getDefinitions() {
        return (definitions != null) ? 
                definitions : Collections.emptyMap();
    }
    
    SubschemaMap getSubschemaMap() {
        return subschemaMap;
    }
    
    @Override
    public JsonSchema build() {
        if (empty) {
            return JsonSchema.EMPTY;
        } else if (subschemaMap.isEmpty()) {
            return new LeafSchema(this);
        } else {
            addConditionalSchemaIfExists();
            return new InternalSchema(this);
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
    public JsonSchemaBuilder withConst(JsonValue value) {
        Objects.requireNonNull(value, "value must not be null.");
        assertions.add(new Const(value, this.jsonProvider));
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
        assertions.add(new EnumAssertion(values, this.jsonProvider));
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
        assertions.add(new Type(types));
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
        assertions.add(new Required(names));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMaximum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        assertions.add(new Maximum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withExclusiveMaximum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        assertions.add(new ExclusiveMaximum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinimum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        assertions.add(new Minimum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withExclusiveMinimum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null.");
        assertions.add(new ExclusiveMinimum(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMultipleOf(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "divisor must not be null.");
        assertions.add(new MultipleOf(divisor));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withMaxLength(int bound) {
        assertions.add(new MaxLength(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinLength(int bound) {
        assertions.add(new MinLength(bound));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withProperty(String name, JsonSchema subschema) {
        Objects.requireNonNull(name, "name must not be null.");
        Objects.requireNonNull(subschema, "subschema must not be null.");
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(name, subschema);
        return builderWithSubschema("properties/" + name, subschema);
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
        return builderWithSubschema("patternProperties/" + pattern, subschema);
    }

    @Override
    public JsonSchemaBuilder withAdditionalProperties(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.additionalProperties = subschema;
        return builderWithSubschema("additionalProperties", subschema);
    }
    
    @Override
    public JsonSchemaBuilder withItem(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.items = subschema;
        return builderWithSubschema("items", subschema);
    }

    @Override
    public JsonSchemaBuilder withItems(List<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        this.items = new ArrayList<JsonSchema>(subschemas);
        for (int i = 0; i < subschemas.size(); ++i) {
            builderWithSubschema("items/" + i, subschemas.get(i));
        }
        return this;
    }
  
    @Override
    public JsonSchemaBuilder withMaxItems(int bound) {
        assertions.add(new MaxItems(bound));
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withMinItems(int bound) {
        assertions.add(new MinItems(bound));
        return builderNonempty();
    }
    
    @Override
    public JsonSchemaBuilder withAdditionalItems(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.additionalItems = subschema;
        return builderWithSubschema("additionalItems", subschema);
    } 
    
    @Override
    public JsonSchemaBuilder withAllOf(JsonSchema... subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        return withAllOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withAllOf(Collection<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        JsonSchema schema = new AllOf(subschemas);
        this.subschemas.add(schema);
        return builderWithSubschema("allOf", schema);
    }

    @Override
    public JsonSchemaBuilder withAnyOf(JsonSchema... subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        return withAnyOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withAnyOf(Collection<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        JsonSchema schema = new AnyOf(subschemas);
        this.subschemas.add(schema);
        return builderWithSubschema("anyOf", schema);
    }

    @Override
    public JsonSchemaBuilder withOneOf(JsonSchema... subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        return withOneOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withOneOf(Collection<JsonSchema> subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        JsonSchema schema = new OneOf(subschemas);
        this.subschemas.add(schema);
        return builderWithSubschema("oneOf", schema);
    }

    @Override
    public JsonSchemaBuilder withNot(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.subschemas.add(new Not(subschema));
        return builderWithSubschema("not", subschema);
    }
  
    @Override
    public JsonSchemaBuilder withIf(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.ifSchema = subschema;
        return builderWithSubschema("if", subschema);
    }

    @Override
    public JsonSchemaBuilder withThen(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.thenSchema = subschema;
        return builderWithSubschema("then", subschema);
    }

    @Override
    public JsonSchemaBuilder withElse(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.elseSchema = subschema;
        return builderWithSubschema("else", subschema);
    }
   
    @Override
    public JsonSchemaBuilder withDefinition(String name, JsonSchema schema) {
        Objects.requireNonNull(name, "name must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        if (this.definitions == null) {
            this.definitions = new HashMap<>();
        }
        this.definitions.put(name, schema);
        return builderWithSubschema("definitions/" + name, schema);
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
    private JsonSchemaBuilder builderWithSubschema(String key, JsonSchema schema) {
        this.subschemaMap.put(key, schema);
        return builderNonempty();
    }
    
    private void addConditionalSchemaIfExists() {
        if (ifSchema != null) {
            this.subschemas.add(new IfThenElse(ifSchema, thenSchema, elseSchema));
        }
    }
}
