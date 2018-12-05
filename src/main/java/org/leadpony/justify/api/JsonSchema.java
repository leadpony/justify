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
package org.leadpony.justify.api;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * JSON schema.
 * 
 * <p>
 * A JSON schema can be read from {@link InputStream} or {@link Reader}.
 * The following example shows how to read a JSON schema from a {@link StringReader}: 
 * </p>
 * <pre><code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * StringReader reader = new StringReader("{\"type\": \"integer\"}");
 * JsonSchema schema = service.readSchema(reader);
 * </code></pre>
 *
 * <p>
 * Alternatively, a JSON schema can be built programmatically 
 * with {@link JsonSchemaBuilder} as follows.
 * </p>
 * <pre><code>
 * JsonSchemaBuilderFactory factory = service.createSchemaBuilderFactory();
 * JsonSchemaBuilder builder = factory.createBuilder();
 * JsonSchema schema = builder.withType(InstanceType.INTEGER).build();
 * </code></pre>
 * 
 * @author leadpony
 */
public interface JsonSchema {
 
    /**
     * Checks if this schema has an identifier assigned or not.
     * @return {@code true} if this schema has an identifier, {@code false} otherwise.
     */
    default boolean hasId() {
        return false;
    }
    
    /**
     * Returns the identifier of this schema, specified with "$id" keyword.
     * @return the identifier of this schema, or {@code null}.
     */
    default URI id() {
        return null;
    }
    
    /**
     * Returns the version identifier of this schema, specified with "$schema" keyword.
     * @return the version identifier of this schema, or {@code null}.
     */
    default URI schemaId() {
        return null;
    }
    
    /**
     * Checks if this schema is a boolean schema.
     * @return {@code true} if this schema is a boolean schema, {@code false} otherwise.
     */
    default boolean isBoolean() {
        return false;
    }
    
    /**
     * Returns the all subschemas contained in this schema.
     * @return the stream of subschemas contained in this schema.
     */
    default Stream<JsonSchema> subschemas() {
        return Stream.empty();
    }
    
    /**
     * Returns the subschema at the location specified with a JSON pointer.
     * @param jsonPointer the valid escaped JSON Pointer string.
     *                    It must be an empty string or a sequence of '/' prefixed tokens.
     * @return the subschema found or {@code null} if the subschema does not exist.
     * @throws NullPointerException if the specified {@code jsonPointer} is {@code null}.
     * @throws JsonException if the specified {@code jsonPointer} is not a valid JSON Pointer.
     */
    default JsonSchema subschemaAt(String jsonPointer) {
        Objects.requireNonNull(jsonPointer, "jsonPointer must not be null.");
        return jsonPointer.isEmpty() ? this : null;
    }
    
    /**
     * Creates an evaluator of this schema.
     * <p>Note that this method is not intended to be used directly by end users.</p>
     * @param type the type of the JSON instance against which this schema will be evaluated.
     * @return newly created evaluator. It must not be {@code null}.
     * @throws NullPointerException if the specified {@code type} is {@code null}.
     */
    Evaluator createEvaluator(InstanceType type);

    /**
     * Creates an evaluator of the negated version of this schema.
     * <p>Note that this method is not intended to be used directly by end users.</p>
     * @param type the type of the JSON instance against which this schema will be evaluated.
     * @return newly created evaluator. It must not be {@code null}.
     * @throws NullPointerException if the specified {@code type} is {@code null}.
     */
    Evaluator createNegatedEvaluator(InstanceType type);
    
    /**
     * Returns the JSON representation of this schema.
     * @return the JSON representation of this schema, never be {@code null}.
     */
    JsonValue toJson();
    
    /**
     * Returns the string representation of this schema.
     * @return the string representation of this schema, never be {@code null}.
     * @throws JsonException if an error occurred while generating a JSON.
     */
    @Override
    String toString();
    
    /**
     * Returns a boolean schema instance representing the specified boolean value. 
     * @param value the boolean value.
     * @return {@link JsonSchema#TRUE} if the specified {@code value} is {@code true},
     *         or {@link JsonSchema#FALSE} if the specified {@code value} is {@code false}.
     */
    static JsonSchema valueOf(boolean value) {
        return value ? JsonSchema.TRUE : JsonSchema.FALSE;
    }
    
    /**
     * The JSON schema which is always evaluated as true.
     * Any JSON instance satisfy this schema.
     */
    JsonSchema TRUE = new SimpleJsonSchema() {
        
        @Override
        public boolean isBoolean() {
            return true;
        }
        
        @Override
        public Evaluator createEvaluator(InstanceType type) {
            return ALWAYS_TRUE;
        }
        
        @Override
        public Evaluator createNegatedEvaluator(InstanceType type) {
            return ALWAYS_FALSE;
        }

        @Override
        public JsonValue toJson() {
            return JsonValue.TRUE;
        }

        @Override
        public String toString() {
            return "true";
        }
    };
    
    /**
     * The JSON schema which is always evaluated as false.
     * Any JSON instance does not satisfy this schema.
     */
    JsonSchema FALSE = new SimpleJsonSchema() {
        
        @Override
        public boolean isBoolean() {
            return true;
        }

        @Override
        public Evaluator createEvaluator(InstanceType type) {
            return ALWAYS_FALSE;
        }

        @Override
        public Evaluator createNegatedEvaluator(InstanceType type) {
            return ALWAYS_TRUE;
        }

        @Override
        public JsonValue toJson() {
            return JsonValue.FALSE;
        }

        @Override
        public String toString() {
            return "false";
        }
    };

    /**
     * The JSON Schema represented by an empty JSON object.
     * This schema is always evaluated as true.  
     */
    JsonSchema EMPTY = new SimpleJsonSchema() {
        
        @Override
        public Evaluator createEvaluator(InstanceType type) {
            return ALWAYS_TRUE;
        }
        
        @Override
        public Evaluator createNegatedEvaluator(InstanceType type) {
            return ALWAYS_FALSE;
        }

        @Override
        public JsonValue toJson() {
            return JsonObject.EMPTY_JSON_OBJECT;
        }

        @Override
        public String toString() {
            return "{}";
        }
    };
}

