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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.JsonSchema;

/**
 * @author leadpony
 */
class PropertySchemaFinder {
    
    private final Map<String, JsonSchema> properties;
    private final Map<Pattern, JsonSchema> patternProperties;
    private final JsonSchema additional;
    
    PropertySchemaFinder(
            Map<String, JsonSchema> properties,
            Map<Pattern, JsonSchema> patternProperties,
            JsonSchema additional) {
        this.properties = properties;
        this.patternProperties = patternProperties;
        this.additional = additional;
    }
    
    JsonSchema findSchema(String propertyName) {
        if (properties == null) {
            return JsonSchemas.ALWAYS_TRUE;
        } else if (properties.containsKey(propertyName)) {
            return properties.get(propertyName);
        } else if (this.patternProperties != null) {
            for (Pattern pattern : this.patternProperties.keySet()) {
                Matcher m = pattern.matcher(propertyName);
                if (m.lookingAt()) {
                    return this.patternProperties.get(pattern);
                }
            }
        }
        if (additional != null) {
            return additional;
        } else {
            return JsonSchemas.ALWAYS_TRUE;
        }
    }
    
    PropertySchemaFinder negate() {
        return new PropertySchemaFinder(
                negateMap(this.properties),
                negateMap(this.patternProperties),
                (this.additional != null) ? this.additional.negate() : null
                );
    }
    
    void toJson(JsonGenerator generator) {
        if (properties != null) {
            generator.writeKey("properties");
            generator.writeStartObject();
            properties.forEach((name, schema)->{
                generator.writeKey(name);
                schema.toJson(generator);
            });
            generator.writeEnd();
        }
        if (patternProperties != null) {
            generator.writeKey("patternProperties");
            generator.writeStartObject();
            patternProperties.forEach((pattern, schema)->{
                generator.writeKey(pattern.toString());
                schema.toJson(generator);
            });
            generator.writeEnd();
        }
        if (additional != null) {
            generator.writeKey("additionalProperties");
            additional.toJson(generator);
        }
    }
    
    private static <K> Map<K, JsonSchema> negateMap(Map<K, JsonSchema> original) {
        if (original == null) {
            return original;
        }
        Map<K, JsonSchema> negated = new HashMap<K, JsonSchema>(original);
        negated.replaceAll((k, v)->v.negate());
        return negated;
    }
}
