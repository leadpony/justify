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

import jakarta.json.JsonValue;

/**
 * A definition of a keyword.
 *
 * @author leadpony
 */
public interface KeywordType {

    /**
     * Returns the name of the keyword.
     *
     * @return the name of the keyword, cannot be {@code null}.
     */
    String name();

    /**
     * Creates an instance of this keyword.
     *
     * @param jsonValue the original JSON value given for this keyword.
     * @param context   the creation context.
     * @return newly created instance of this keyword.
     */
    Keyword newInstance(JsonValue jsonValue, CreationContext context);

    /**
     * A context of keyword creation.
     *
     * @author leadpony
     */
    interface CreationContext {

        /**
         * Returns the JSON schema generated from the specified JSON value.
         *
         * @param value the JSON value from which a JSON schema was generated.
         * @return the JSON schema.
         * @throws IllegalArgumentException if the specified {@code value} is not a JSON
         *                                  schema.
         */
        JsonSchema asJsonSchema(JsonValue value);
    }
}
