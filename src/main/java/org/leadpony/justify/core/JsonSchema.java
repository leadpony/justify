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
 * StringReader reader = new StringReader("{\"type\": \"integer\"}");
 * JsonSchema schema = Jsonv.readSchema(reader);
 * </code></pre>
 *
 * <p>
 * Alternatively, a JSON schema can be built programmatically 
 * with {@link JsonSchemaBuilder}.
 * </p>
 * <pre><code>
 * JsonSchemaBuilderFactory factory = Jsonv.createSchemaBuilder();
 * JsonSchemaBuilder builder = factory.createBuilder();
 * JsonSchema schema = builder.withType(InstanceType.INTEGER).build();
 * </code></pre>
 * 
 * @author leadpony
 */
public interface JsonSchema {
 
    /**
     * Checks if this schema has an identifier assigned or not.
     * 
     * @return {@code true} if this schema has an identifier, {@code false} otherwise.
     */
    default boolean hasId() {
        return false;
    }
    
    /**
     * Returns the identifier of this schema, specified with "$id" keyword.
     * 
     * @return the identifier of this schema, or {@code null}.
     */
    default URI id() {
        return null;
    }
    
    /**
     * Returns the version identifier of this schema, specified with "$schema" keyword.
     * 
     * @return the version identifier of this schema, or {@code null}.
     */
    default URI schemaId() {
        return null;
    }
    
    /**
     * Checks if this schema is a boolean schema.
     * 
     * @return {@code true} if this schema is a boolean schema, {@code false} otherwise.
     */
    default boolean isBoolean() {
        return false;
    }
    
    /**
     * Returns the all subschemas contained in this schema.
     * 
     * @return the stream of subschemas contained in this schema.
     */
    default Stream<JsonSchema> subschemas() {
        return Stream.empty();
    }
    
    /**
     * Returns the subschema at the location specified with a JSON pointer.
     * 
     * @param jsonPointer the valid escaped JSON Pointer string.
     *                    It must be an empty string or a sequence of '/' prefixed tokens.
     * @return the subschema found or {@code null} if the subschema does not exist.
     * @throws NullPointerException if {@code jsonPointer} is {@code null}.
     * @throws JsonException {@code jsonPointer} is not a valid JSON Pointer.
     */
    default JsonSchema subschemaAt(String jsonPointer) {
        Objects.requireNonNull(jsonPointer, "jsonPointer must not be null.");
        return jsonPointer.isEmpty() ? this : null;
    }
    
    /**
     * Creates an evaluator of this schema.
     * 
     * @param type the type of the instance to which this schema will be applied.
     * @param factory the factory of basic evaluators.
     * @return the evaluator of this schema. It must not be {@code null}.
     * @throws NullPointerException if the specified {@code type} or {@code factory} is {@code null}.
     */
    Evaluator createEvaluator(InstanceType type, EvaluatorFactory factory);

    /**
     * Returns the negation of this schema.
     * 
     * @return the negation of this schema, never be {@code null}.
     */
    JsonSchema negate();
    
    /**
     * Returns the JSON representation of this schema.
     * 
     * @return the JSON representation of this schema, never be {@code null}.
     */
    JsonValue toJson();
    
    /**
     * Returns the string representation of this schema.
     * 
     * @return the string representation of this schema, never be {@code null}.
     * @throws JsonException if an error occurred while generating a JSON.
     */
    @Override
    String toString();
    
    /**
     * Factory for producing the predefined basic evaluators.
     * 
     * @author leadpony
     */
    public static interface EvaluatorFactory {

        /**
         * Returns the evaluator which evaluates any JSON schema as true ("valid").
         * 
         * @return the evaluator, never be {@code null}.
         */
        Evaluator alwaysTrue();
        
        /**
         * Returns the evaluator which evaluates any JSON schema as false ("invalid").
         * 
         * @param schema the JSON schema to be evaluated, cannot be {@code null}.
         * @return the evaluator, never be {@code null}.
         */
        Evaluator alwaysFalse(JsonSchema schema);
    }

    /**
     * JSON Schema represented by an empty JSON object.
     * This schema is always evaluated as true.  
     */
    JsonSchema EMPTY = new JsonSchema() {
        
        @Override
        public Evaluator createEvaluator(InstanceType type, EvaluatorFactory factory) {
            return factory.alwaysTrue();
        }

        @Override
        public JsonSchema negate() {
            return FALSE;
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

    /**
     * The JSON schema which is always evaluated as true.
     * Any JSON instance satisfy this schema.
     */
    JsonSchema TRUE = new JsonSchema() {
        
        @Override
        public boolean isBoolean() {
            return true;
        }
        
        @Override
        public Evaluator createEvaluator(InstanceType type, EvaluatorFactory factory) {
            return factory.alwaysTrue();
        }
        
        @Override
        public JsonSchema negate() {
            return FALSE;
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
    JsonSchema FALSE = new JsonSchema() {
        
        @Override
        public boolean isBoolean() {
            return true;
        }

        @Override
        public Evaluator createEvaluator(InstanceType type, EvaluatorFactory factory) {
            return factory.alwaysFalse(this);
        }

        @Override
        public JsonSchema negate() {
            return TRUE;
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
}
