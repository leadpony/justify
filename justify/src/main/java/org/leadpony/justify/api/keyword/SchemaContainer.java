/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.api.keyword;

import java.util.Optional;
import java.util.stream.Stream;

import org.leadpony.justify.api.JsonSchema;

/**
 * A container of JSON schemas.
 *
 * @author leadpony
 * @since 4.0
 */
public interface SchemaContainer {

    /**
     * Checks whether this container contains any schemas or not.
     *
     * @return {@code true} if this container contains any schemas, {@code false}
     *         if it does not.
     */
    default boolean containsSchemas() {
        return false;
    }

    /**
     * Returns all schemas contained by this container as a stream.
     *
     * @return the stream of schemas.
     */
    default Stream<JsonSchema> getSchemasAsStream() {
        return Stream.empty();
    }

    /**
     * Searches this container for a schema located at the position specified by a
     * reference token of a JSON pointer.
     *
     * @param token the unescaped reference token of the JSON pointer, which
     *              specifies the location of the schema in this container.
     * @return the schema found or empty if not found.
     */
    default Optional<JsonSchema> findSchema(String token) {
        return Optional.empty();
    }
}
