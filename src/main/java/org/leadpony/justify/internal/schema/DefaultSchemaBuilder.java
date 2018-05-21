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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.assertion.ExclusiveMaximum;
import org.leadpony.justify.internal.assertion.ExclusiveMinimum;
import org.leadpony.justify.internal.assertion.MaxLength;
import org.leadpony.justify.internal.assertion.Maximum;
import org.leadpony.justify.internal.assertion.MinLength;
import org.leadpony.justify.internal.assertion.Minimum;
import org.leadpony.justify.internal.assertion.Required;
import org.leadpony.justify.internal.assertion.Type;

/**
 * @author leadpony
 */
public class DefaultSchemaBuilder implements JsonSchemaBuilder {
    
    private boolean empty;
    private boolean hasSubschema;

    private String title;
    private String description;
    private final List<Assertion> assertions = new ArrayList<>();
    
    private final Map<String, JsonSchema> properties = new HashMap<>();
    private final List<JsonSchema> items = new ArrayList<>();
    private final List<JsonSchema> subschemas = new ArrayList<>();
    
    public DefaultSchemaBuilder() {
        this.empty = true;
    }
    
    String title() {
        return title;
    }

    String description() {
        return description;
    }

    List<Assertion> assertions() {
        return Collections.unmodifiableList(assertions);
    }
    
    Map<String, JsonSchema> properties() {
        return Collections.unmodifiableMap(properties);
    }

    List<JsonSchema> items() {
        return Collections.unmodifiableList(items);
    }

    List<JsonSchema> subschemas() {
        return Collections.unmodifiableList(subschemas);
    }

    boolean isEmpty() {
        return empty;
    }
    
    boolean hasSubschema() {
        return hasSubschema;
    }
    
    @Override
    public JsonSchema build() {
        if (isEmpty()) {
            return JsonSchemas.EMPTY;
        } else if (hasSubschema()) {
            return new ComplexSchema(this);
        } else {
            return new SimpleSchema(this);
        }
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
    public JsonSchemaBuilder withType(InstanceType... types) {
        Objects.requireNonNull(types, "types must not be null.");
        return withType(new HashSet<>(Arrays.asList(types)));
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
        return withRequired(new HashSet<>(Arrays.asList(names)));
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
        properties.put(name, subschema);
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withItem(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        addItem(subschema);
        return builderNonempty();
    }

    @Override
    public JsonSchemaBuilder withItems(JsonSchema... subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null.");
        for (JsonSchema subschema : subschemas) {
            addItem(subschema);
        }
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
        return builderWithSubschema();
    }

    @Override
    public JsonSchemaBuilder withNot(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null.");
        this.subschemas.add(new Not(subschema));
        return builderWithSubschema();
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
        this.hasSubschema = true;
        return builderNonempty();
    }
    
    private void addItem(JsonSchema subschema) {
        items.add(subschema);
    }
}
