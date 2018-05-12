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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;

/**
 * Object-type JSON schema.
 * 
 * @author leadpony
 */
public class ComplexSchema extends SimpleSchema {
    
    private final Map<String, JsonSchema> properties;
    private final List<JsonSchema> items;
    private final List<JsonSchema> subschemas;
    
    ComplexSchema(DefaultSchemaBuilder builder) {
        super(builder);
        this.properties = builder.properties();
        this.items = builder.items();
        this.subschemas = builder.subschemas();
    }
    
    @Override
    public Evaluator createEvaluator(InstanceType type) {
        Objects.requireNonNull(type, "type must not be null.");
        return ComplexEvaluator.newValuator(type, this);
    }
    
    @Override
    public JsonSchema findChildSchema(String propertyName) {
        Objects.requireNonNull(propertyName, "propertyName must not be null.");
        return properties.get(propertyName);
    }

    @Override
    public JsonSchema findChildSchema(int itemIndex) {
        if (itemIndex < items.size()) {
            return items.get(itemIndex);
        } else {
            return null;
        }
    }
    
    @Override
    public List<JsonSchema> subschemas() {
        return subschemas;
    }

    @Override
    public void toJson(JsonGenerator generator) {
        Objects.requireNonNull(generator, "generator must not be null.");
        generator.writeStartObject();
        appendMembers(generator);
        generator.writeEnd();
    }

    @Override
    protected void appendMembers(JsonGenerator generator) {
        super.appendMembers(generator);
        if (!properties.isEmpty()) {
            generator.writeStartObject("properties");
            this.properties.forEach((name, schema)->{
                generator.writeKey(name);
                schema.toJson(generator);
            });
            generator.writeEnd();
        }
        if (!items.isEmpty()) {
            generator.writeStartArray("items");
            items.forEach(schema->schema.toJson(generator));
            generator.writeEnd();
        }
        subschemas.forEach(schema->schema.toJson(generator));
    }    
}
