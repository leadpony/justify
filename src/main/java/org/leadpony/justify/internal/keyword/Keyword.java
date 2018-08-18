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

package org.leadpony.justify.internal.keyword;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * Super type of all keywords that can compose a JSON schema.
 * 
 * @author leadpony
 */
public interface Keyword {

    /**
     * Returns the name of this keyword.
     * 
     * @return the name of this keyword, never be {@code null}.
     */
    String name();
    
    /**
     * Returns the JSON schema enclosing this keyword.
     * 
     * @return the enclosing schema of this keyword.
     */
    JsonSchema getEnclosingSchema();

    /**
     * Assigns the JSON schema enclosing this keyword.
     * @param schema the enclosing schema of this keyword.
     */
    void setEnclosingSchema(JsonSchema schema);
    
    /**
     * Checks if this keyword can be evaluated.
     * 
     * @return {@code true} if this keyword can be evaluated, 
     *         {@code false} otherwise.
     */
    boolean canEvaluate();
    
    /**
     * Creates an evaluator for this keyword.
     * 
     * @param type the type of the instance, cannot be {@code null}.
     * @param appender the type for appending evaluators, cannot be {@code null}.
     * @param builderFactory the factory for producing builders of JSON containers, cannot be {@code null}.
     * @param affirmative {@code true} to create normal evaluators,
     *                    {@code false} to create negated evaluators. 
     */
    default void createEvaluator(InstanceType type, EvaluatorAppender appender,
            JsonBuilderFactory builderFactory, boolean affirmative) {
        throw new UnsupportedOperationException(
                name() + " does not support evaluation.");
    }
    
    /**
     * Adds this keyword to the specified JSON object.
     * 
     * @param builder the builder for building a JSON object, cannot be {@code null}.
     * @param builderFactory the factory for producing builders, cannot be {@code null}.
     */
    void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory);

    /**
     * Links this keyword with the sibling keywords in the containing schema.
     *  
     * @param siblings the sibling keywords in the containing schema.
     */
    default void link(Map<String, Keyword> siblings) {
    }
    
    /**
     * Checks whether this keyword has any subschemas or not.
     * 
     * @return {@code true} if this keyword contains any subschemas, {@code false} otherwise.
     */
    default boolean hasSubschemas() {
        return false;
    }
    
    /**
     * Returns all subschemas contained in this keyword as a stream.
     * 
     * @return the stream of subschemas.
     */
    default Stream<JsonSchema> subschemas() {
        return Stream.empty();
    }

    /**
     * Searches this keyword for a subschema located at the position specified by a JSON pointer. 
     * 
     * @param jsonPointer the JSON pointer identifying the subschema in this keyword.
     * @return the subschema if found or {@code null} if not found.
     */
    default JsonSchema getSubschema(Iterator<String> jsonPointer) {
        return null;
    }
}
