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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.JsonSchema;

/**
 * Finder of schemas for JSON object properties.
 * 
 * @author leadpony
 */
class PropertySchemaFinder {
    
    private final Map<String, JsonSchema> properties;
    private final Map<Pattern, JsonSchema> patternProperties;
    private final Optional<JsonSchema> additional;
    
    PropertySchemaFinder(
            Map<String, JsonSchema> properties,
            Map<Pattern, JsonSchema> patternProperties,
            JsonSchema additional) {
        this.properties = (properties != null) ? 
                properties : Collections.emptyMap();
        this.patternProperties = (patternProperties != null) ? 
                patternProperties : Collections.emptyMap();
        this.additional = Optional.ofNullable(additional);
    }
    
    /**
     * Finds schema for the property of specified name.
     * 
     * @param propertyName the name of the property.
     * @param found the found schemas. 
     * @return {@code true} if some schemas were found, {@code false} otherwise.
     */
    boolean findSchema(String propertyName, List<JsonSchema> found) {
        if (properties.containsKey(propertyName)) {
            found.add(properties.get(propertyName));
        }
        for (Pattern pattern : this.patternProperties.keySet()) {
            Matcher m = pattern.matcher(propertyName);
            if (m.lookingAt()) {
                found.add(this.patternProperties.get(pattern));
            }
        }
        if (found.isEmpty()) {
            JsonSchema schema = additional.orElse(JsonSchema.TRUE);
            if (schema == JsonSchema.FALSE) {
                return false;
            } else {
                found.add(schema);
            }
        }
        return true;
    }
    
    PropertySchemaFinder negate() {
        return new PropertySchemaFinder(
                negateMap(this.properties),
                negateMap(this.patternProperties),
                this.additional.map(JsonSchema::negate).orElse(null)
                );
    }
    
    void toJson(JsonGenerator generator) {
        if (!properties.isEmpty()) {
            generator.writeKey("properties");
            generator.writeStartObject();
            properties.forEach((name, schema)->{
                generator.writeKey(name);
                schema.toJson(generator);
            });
            generator.writeEnd();
        }
        if (!patternProperties.isEmpty()) {
            generator.writeKey("patternProperties");
            generator.writeStartObject();
            patternProperties.forEach((pattern, schema)->{
                generator.writeKey(pattern.toString());
                schema.toJson(generator);
            });
            generator.writeEnd();
        }
        additional.ifPresent(schema->{
            generator.writeKey("additionalProperties");
            schema.toJson(generator);
        });
    }
    
    private static <K> Map<K, JsonSchema> negateMap(Map<K, JsonSchema> original) {
        if (original.isEmpty()) {
            return original;
        }
        Map<K, JsonSchema> negated = new HashMap<K, JsonSchema>(original);
        negated.replaceAll((k, v)->v.negate());
        return negated;
    }
}
