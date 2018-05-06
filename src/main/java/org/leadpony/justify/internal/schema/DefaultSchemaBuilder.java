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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.assertion.ExclusiveMaximum;
import org.leadpony.justify.internal.assertion.ExclusiveMinimum;
import org.leadpony.justify.internal.assertion.Maximum;
import org.leadpony.justify.internal.assertion.Minimum;
import org.leadpony.justify.internal.assertion.Required;
import org.leadpony.justify.internal.assertion.Type;

/**
 * @author leadpony
 */
public class DefaultSchemaBuilder implements JsonSchemaBuilder {
    
    private String title;
    private String description;
    private final List<Assertion> assertions = new ArrayList<>();
    
    private final Map<String, JsonSchema> properties = new HashMap<>();
    private final List<JsonSchema> items = new ArrayList<>();
    
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

    boolean isEmpty() {
        return title == null &&
               description == null &&
               assertions.isEmpty() &&
               properties.isEmpty() &&
               items.isEmpty()
               ;
    }
    
    @Override
    public JsonSchema build() {
        if (isEmpty()) {
            return JsonSchema.empty();
        } else {
            return new ObjectSchema(this);
        }
    }

    @Override
    public JsonSchemaBuilder withTitle(String title) {
        Objects.requireNonNull(title, "title must not be null");
        this.title = title;
        return this;
    }

    @Override
    public JsonSchemaBuilder withDescription(String description) {
        Objects.requireNonNull(description, "description must not be null");
        this.description = description;
        return this;
    }

    @Override
    public DefaultSchemaBuilder withType(InstanceType... types) {
        Objects.requireNonNull(types, "types must not be null");
        return withType(Arrays.asList(types));
    }
    
    @Override
    public DefaultSchemaBuilder withType(Iterable<InstanceType> types) {
        Objects.requireNonNull(types, "types must not be null");
        assertions.add(new Type(types));
        return this;
    }

    @Override
    public JsonSchemaBuilder withRequired(String... names) {
        Objects.requireNonNull(names, "names must not be null");
        return withRequired(Arrays.asList(names));
    }

    @Override
    public JsonSchemaBuilder withRequired(Iterable<String> names) {
        Objects.requireNonNull(names, "names must not be null");
        assertions.add(new Required(names));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMaximum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null");
        assertions.add(new Maximum(bound));
        return this;
    }

    @Override
    public JsonSchemaBuilder withExclusiveMaximum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null");
        assertions.add(new ExclusiveMaximum(bound));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMinimum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null");
        assertions.add(new Minimum(bound));
        return this;
    }

    @Override
    public JsonSchemaBuilder withExclusiveMinimum(BigDecimal bound) {
        Objects.requireNonNull(bound, "bound must not be null");
        assertions.add(new ExclusiveMinimum(bound));
        return this;
    }

    @Override
    public JsonSchemaBuilder withProperty(String name, JsonSchema subschema) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(subschema, "subschema must not be null");
        properties.put(name, subschema);
        return this;
    }

    @Override
    public JsonSchemaBuilder withItem(JsonSchema subschema) {
        Objects.requireNonNull(subschema, "subschema must not be null");
        addItem(subschema);
        return this;
    }

    @Override
    public JsonSchemaBuilder withItems(JsonSchema... subschemas) {
        Objects.requireNonNull(subschemas, "subschemas must not be null");
        for (JsonSchema subschema : subschemas) {
            addItem(subschema);
        }
        return this;
    }
    
    private void addItem(JsonSchema subschema) {
        items.add(subschema);
    }
}
