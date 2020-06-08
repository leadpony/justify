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

import org.leadpony.justify.api.JsonSchema;

import jakarta.json.stream.JsonParser;

/**
 * A JSON parser which is available for construction of keywords.
 *
 * @author leadpony
 * @since 4.0
 */
public interface KeywordParser extends JsonParser {

    /**
     * Checks if a JSON schema can be retrieved by the parser in the current state.
     * @return {@code true} if the parser can retrieve a JSON schema, {@code false} otherwise.
     */
    boolean canGetSchema();

    /**
     * Returns a {@link JsonSchema} at the current parser position.
     *
     * @return the {@code JsonSchema} at the current parser position.
     * @throws IllegalStateException if schema is not found at the current position.
     */
    JsonSchema getSchema();
}
