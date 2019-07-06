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

import javax.json.JsonValue;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A factory of keywords.
 *
 * @author leadpony
 */
public interface KeywordFactory {

    /**
     * Creates a keyword.
     *
     * @param name the name of the keyword to create never be {@code null}.
     * @param value the value of the keyword, never be {@code null}.
     * @param context the creation context never be {@code null}.
     * @return newly created keyword, or {@code null}.
     */
    SchemaKeyword createKeyword(String name, JsonValue value, CreationContext context);

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
         * @throws IllegalArgumentException if the specified {@code value} is not a JSON schema.
         */
        JsonSchema asJsonSchema(JsonValue value);

        /**
         * Returns the format attribute of the specified name.
         *
         * @param name the name of the format attribute.
         * @return the format attribute if found, or {@code null} if not found.
         */
        FormatAttribute getFormateAttribute(String name);

        /**
         * Returns the encoding scheme of the specified name.
         *
         * @param name the name of the encoding scheme.
         * @return the encoding scheme.
         */
        ContentEncodingScheme getEncodingScheme(String name);

        /**
         * Returns the MIME type of the specified value.
         *
         * @param value the value of the MIME type.
         * @return the MIME type.
         */
        ContentMimeType getMimeType(String value);
    }
}
