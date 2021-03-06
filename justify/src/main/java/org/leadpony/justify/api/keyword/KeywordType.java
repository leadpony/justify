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

import jakarta.json.JsonValue;

/**
 * A definition of a keyword.
 *
 * @author leadpony
 * @since 4.0
 */
public interface KeywordType {

    /**
     * Returns the name of the keyword.
     *
     * @return the name of the keyword, cannot be {@code null}.
     */
    String name();

    /**
     * Creates a keyword of this type.
     *
     * @param jsonValue the value of the keyword.
     * @param schemaParser the parser of subschemas.
     * @return the created keyword, cannot be {@code null}.
     * @throws InvalidKeywordException if the input JSON value is invalid.
     */
    Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser);
}
