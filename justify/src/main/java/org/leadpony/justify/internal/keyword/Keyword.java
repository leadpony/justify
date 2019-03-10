/*
 * Copyright 2018-2019 the Justify authors.
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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;

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
     *
     * @param schema the enclosing schema of this keyword.
     */
    void setEnclosingSchema(JsonSchema schema);

    /**
     * Checks if this keyword supports the specified type.
     *
     * @param type the type to check.
     * @return {@code true} if this keyword supports the type.
     */
    default boolean supportsType(InstanceType type) {
        return true;
    }

    /**
     * Returns the types supported by this keyword.
     *
     * @return the supported types.
     */
    default Set<InstanceType> getSupportedTypes() {
        return EnumSet.allOf(InstanceType.class);
    }

    /**
     * Creates an evaluator for this keyword.
     * @param context TODO
     * @param type           the type of the instance, cannot be {@code null}.
     */
    Evaluator createEvaluator(EvaluatorContext context, InstanceType type);

    /**
     * Creates an evaluator for the negated version of this keyword.
     * @param context TODO
     * @param type           the type of the instance, cannot be {@code null}.
     */
    Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type);

    /**
     * Adds this keyword to the specified JSON object.
     *
     * @param builder        the builder for building a JSON object, cannot be
     *                       {@code null}.
     * @param builderFactory the factory for producing builders, cannot be
     *                       {@code null}.
     */
    void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory);

    /**
     * Adds this keyword to the list if this keyword is evaluatables.
     *
     * @param evaluatables the list of evaluatable keywords.
     * @param keywords     all keywords in the enclosing schema.
     */
    default void addToEvaluatables(List<Keyword> evaluatables, Map<String, Keyword> keywords) {
    }

    /**
     * Checks whether the subschemas provided by this keyword will be applied to the
     * same location as the owner schema or not.
     *
     * @return {@code true} if the subschemas will be applied to the same location
     *         as the owner schema, {@code false} otherwise.
     */
    default boolean isInPlace() {
        return false;
    }

    /**
     * Checks whether this keyword has any subschemas or not.
     *
     * @return {@code true} if this keyword contains any subschemas, {@code false}
     *         otherwise.
     */
    default boolean hasSubschemas() {
        return false;
    }

    /**
     * Returns all subschemas contained in this keyword as a stream.
     *
     * @return the stream of subschemas.
     */
    default Stream<JsonSchema> getSubschemas() {
        return Stream.empty();
    }

    /**
     * Searches this keyword for a subschema located at the position specified by a
     * JSON pointer.
     *
     * @param jsonPointer the JSON pointer identifying the subschema in this
     *                    keyword.
     * @return the subschema if found or {@code null} if not found.
     */
    default JsonSchema getSubschema(Iterator<String> jsonPointer) {
        return null;
    }
}
