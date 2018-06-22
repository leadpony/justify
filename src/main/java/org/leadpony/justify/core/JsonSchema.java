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

import java.net.URI;
import java.util.Collections;
import java.util.Objects;

import javax.json.stream.JsonGenerator;

/**
 * JSON schema.
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
     * Returns the schema URI of this schema, specified with "$schema" keyword.
     * 
     * @return the schema URI of this schema, or {@code null}.
     */
    default URI schemaURI() {
        return null;
    }
    
    /**
     * Finds the subschema at the location specified with a JSON pointer.
     * 
     * @param jsonPointer the valid escaped JSON Pointer string.
     *                    It must be an empty string or a sequence of '/' prefixed tokens.
     * @return the subschema found or {@code null} if the subschema does not exist.
     * @throws NullPointerException if {@code jsonPointer} is {@code null}.
     * @throws JsonException {@code jsonPointer} is not a valid JSON Pointer.
     */
    default JsonSchema findSubschema(String jsonPointer) {
        return null;
    }
    
    /**
     * Returns the all subschemas contained in this schema.
     * 
     * @return the object to iterate subschemas.
     */
    default Iterable<JsonSchema> getSubschemas() {
        return Collections.emptySet();
    }
    
    /**
     * Creates an evaluator of this schema.
     * 
     * @param type the type of the instance to which this schema will be applied.
     * @return the evaluator of this schema, 
     *         or {@code null} if there are no evaluator for the type.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    Evaluator createEvaluator(InstanceType type);

    /**
     * Returns the negation of this schema.
     * 
     * @return the negation of this schema, never be {@code null}.
     */
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

    /**
     * Empty JSON Schema.
     */
    JsonSchema EMPTY = new JsonSchema() {
        
        @Override
        public Evaluator createEvaluator(InstanceType type) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public JsonSchema negate() {
            return FALSE;
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.writeStartObject().writeEnd();
        }

        @Override
        public String toString() {
            return "{}";
        }
    };

    /**
     * The JSON schema which evaluates any instances as valid.
     */
    JsonSchema TRUE = new JsonSchema() {
        
        @Override
        public Evaluator createEvaluator(InstanceType type) {
            return Evaluator.ALWAYS_TRUE;
        }
        
        @Override
        public JsonSchema negate() {
            return FALSE;
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.write(true);
        }
        
        @Override
        public String toString() {
            return "true";
        }
    };
    
    /**
     * The JSON schema which evaluates any instances as invalid.
     */
    JsonSchema FALSE = new JsonSchema() {
        
        @Override
        public Evaluator createEvaluator(InstanceType type) {
            return Evaluator.ALWAYS_FALSE;
        }

        @Override
        public JsonSchema negate() {
            return TRUE;
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.write(false);
        }

        @Override
        public String toString() {
            return "false";
        }
    };
}
