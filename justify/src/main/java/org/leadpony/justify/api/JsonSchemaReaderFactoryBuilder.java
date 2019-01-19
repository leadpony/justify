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
package org.leadpony.justify.api;

/**
 * A builder interface for building a {@link JsonSchemaReaderFactory} instance.
 *
 * <p>
 * The following code sample shows how to build a JSON schema reader factory
 * using this interface.
 * </p>
 * <pre><code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * JsonSchemaReaderFactory factory = service.createSchemaReaderFactoryBuilder()
 *     .withStrictWithKeywords(true)
 *     .build();
 * </code></pre>

 * <p>
 * Each instance of this type is NOT safe for use by multiple concurrent
 * threads.
 * </p>
 *
 * @author leadpony
 */
public interface JsonSchemaReaderFactoryBuilder {

    /**
     * Builds a new instance of {@link JsonSchemaReaderFactory}.
     *
     * @return newly created instance of {@link JsonSchemaReaderFactory}, never be
     *         {@code null}.
     */
    JsonSchemaReaderFactory build();

    /**
     * Specifies whether the schema reader is strict with keywords or not.
     * <p>
     * If the reader is strict with keywords, it will report a problem when it
     * encountered an unknown keyword.
     * This value is {@code false} by default.
     * </p>
     *
     * @param strict {@code true} if the schema reader is strict with keywords,
     *               {@code false} otherwise.
     * @return this builder.
     */
    JsonSchemaReaderFactoryBuilder withStrictWithKeywords(boolean strict);

    /**
     * Specifies whether the schema reader is strict with formats or not.
     * <p>
     * If the reader is strict with formats, it will report a problem when it
     * encountered an unknown format attribute.
     * This value is {@code false} by default.
     * </p>
     *
     * @param strict {@code true} if the schema reader is strict with formats,
     *               {@code false} otherwise.
     * @return this builder.
     */
    JsonSchemaReaderFactoryBuilder withStrictWithFormats(boolean strict);
}
