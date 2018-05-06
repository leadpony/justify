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

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.assertion.Assertion;

/**
 * Object-type JSON schema.
 * 
 * @author leadpony
 */
public class ObjectSchema implements JsonSchema {
    
    private final String title;
    private final String description;
    private final List<Assertion> assertions;
    private final Map<String, JsonSchema> properties;
    private final List<JsonSchema> items;
    
    ObjectSchema(DefaultSchemaBuilder builder) {
        this.title = builder.title();
        this.description = builder.description();
        this.assertions = builder.assertions();
        this.properties = builder.properties();
        this.items = builder.items();
    }
    
    @Override
    public Collection<Evaluator> createEvaluators(InstanceType type) {
        return this.assertions.stream()
            .filter(a->a.isApplicableTo(type))
            .map(Assertion::createEvaluator)
            .collect(Collectors.toList());
    }
    
    @Override
    public void collectChildSchema(String propertyName, Collection<JsonSchema> schemas) {
        if (properties.containsKey(propertyName)) {
            schemas.add(properties.get(propertyName));
        }
    }

    @Override
    public void collectChildSchema(int itemIndex, Collection<JsonSchema> schemas) {
        if (items.size() > itemIndex) {
            schemas.add(items.get(itemIndex));
        }
    }
    
    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartObject();
        if (this.title != null) {
            generator.write("title", this.title);
        }
        if (this.description != null) {
            generator.write("description", this.description);
        }
        this.assertions.forEach(assertion->assertion.toJson(generator));
        this.properties.forEach((name, schema)->{
            generator.writeKey(name);
            schema.toJson(generator);
        });
        if (!items.isEmpty()) {
            generator.writeStartArray("items");
            items.forEach(schema->schema.toJson(generator));
            generator.writeEnd();
        }
        generator.writeEnd();
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try (JsonGenerator generator = Json.createGenerator(writer)) {
            toJson(generator);
        } catch (JsonException e) {
        }
        return writer.toString();
    }
}
