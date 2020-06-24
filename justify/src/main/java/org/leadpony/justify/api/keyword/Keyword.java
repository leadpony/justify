/*
 * Copyright 2018-2020 the Justify authors.
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

import java.util.Map;
import jakarta.json.JsonValue;

/**
 * A schema keyword which composes a JSON schema.
 *
 * @author leadpony
 */
public interface Keyword {

    /**
     * Returns the name of this keyword.
     *
     * @return the name of this keyword, never be {@code null}.
     */
    default String name() {
        return getType().name();
    }

    /**
     * Checks whether this keyword is recognized or not.
     *
     * @return {@code true} if the keyword is recognized, {@code false} otherwise.
     * @since 4.0
     */
    default boolean isRecognized() {
        return true;
    }

    /**
     * Returns the value of this keyword as an instance of {@code JsonValue}.
     *
     * @return the value of this keyword, cannot be {@code null}.
     */
    JsonValue getValueAsJson();

    /**
     * Returns the type of this keyword.
     *
     * @return the instance of {@link KeywordType}.
     * @since 4.0
     */
    KeywordType getType();

    /**
     * Combines this keyword with sibling keywords.
     *
     * @param siblings the sibling keywords owned by the same schema, never be
     *                 {@code null}.
     * @return the keyword after modification.
     */
    default Keyword withKeywords(Map<String, Keyword> siblings) {
        return this;
    }

    /**
     * Checks if this keyword can evaluate a JSON instance.
     *
     * @return {@code true} if this keyword can evaluate, {@code false} it cannot.
     * @since 4.0
     */
    default boolean canEvaluate() {
        return false;
    }

    /**
     * Creates an unrecognized keyword.
     *
     * @param name      the keyword name.
     * @param jsonValue the value given for the keyword.
     * @return newly created keyword.
     */
    static Keyword unrecognized(String name, JsonValue jsonValue) {
        return new UnrecognizedKeyword(name, jsonValue);
    }
}
