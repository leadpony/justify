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
package org.leadpony.justify.core;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.spi.JsonValidationServiceProvider;

/**
 * JSON schema.
 * 
 * @author leadpony
 */
public interface JsonSchema {
  
    /**
     * Returns the empty JSON schema.
     * 
     * @return the instance of empty JSON schema, never be {@code null}.
     */
    static JsonSchema empty() {
        return JsonValidationServiceProvider.provider().emptySchema();
    }
    
    /**
     * Returns the JSON schema which approves any JSON instances.
     * 
     * @return the instance of the JSON schema, never be {@code null}.
     */
    static JsonSchema alwaysTrue() {
        return JsonValidationServiceProvider.provider().alwaysTrueSchema();
    }
    
    /**
     * Returns the JSON schema which rejects any JSON instances.
     * 
     * @return the instance of the JSON schema, never be {@code null}.
     */
    static JsonSchema alwaysFalse() {
        return JsonValidationServiceProvider.provider().alwaysFalseSchema();
    }

    static JsonSchema load(InputStream in) {
        return JsonValidationServiceProvider.provider().loadSchema(in);
    }

    static JsonSchema load(Reader reader) {
        return JsonValidationServiceProvider.provider().loadSchema(reader);
    }

    /**
     * Creates an evaluator of this schema.
     * 
     * @param type the type of the instance to which this schema will be applied.
     * @return the evaluator of this schema.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    Optional<Evaluator> createEvaluator(InstanceType type);

    /**
     * Finds child schemas to be applied to the specified property in object.
     * 
     * @param propertyName the name of the property.
     * @return the child schema if found , {@code null} otherwise.
     * @throws NullPointerException if {@code propertyName} is {@code null}.
     */
    default JsonSchema findChildSchema(String propertyName) {
        return null;
    }
    
    /**
     * Finds child schemas to be applied to the specified item in array.
     * 
     * @param itemIndex the index of the item.
     * @return the child schema if found , {@code null} otherwise.
     */
    default JsonSchema findChildSchema(int itemIndex) {
        return null;
    }
    
    /**
     * Returns all subschemas contained directly by this schema.
     * 
     * @return direct subschemas of this schema, may be empty but never be {@code null}.
     */
    default List<JsonSchema> subschemas() {
        return Collections.emptyList();
    }
    
    JsonSchema negate();
    
    /**
     * Generates a JSON representation of this schema.
     * 
     * @param generator the generator of the JSON document.
     * @throws NullPointerException if {@code generator} is {@code null}.
     */
    void toJson(JsonGenerator generator);

    /**
     * Returns the string representation of this schema.
     * 
     * @return the string representation of this schema, never be {@code null}.
     */
    @Override
    String toString();
}
