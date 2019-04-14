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
 *
 * <pre>
 * <code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * JsonSchemaReaderFactory factory = service.createSchemaReaderFactoryBuilder()
 *     .withStrictWithKeywords(true)
 *     .build();
 * </code>
 * </pre>
 *
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
     * encountered an unknown keyword. This value is {@code false} by default.
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
     * encountered an unknown format attribute. This value is {@code false} by
     * default.
     * </p>
     *
     * @param strict {@code true} if the schema reader is strict with formats,
     *               {@code false} otherwise.
     * @return this builder.
     */
    JsonSchemaReaderFactoryBuilder withStrictWithFormats(boolean strict);

    /**
     * Adds a resolver of external JSON schemas to this builder. Multiple resolvers
     * can be added to a builder.
     *
     * @param resolver the resolver of external JSON schemas.
     * @return this builder.
     * @throws NullPointerException if the specified {@code resolver} is
     *                              {@code null}.
     */
    JsonSchemaReaderFactoryBuilder withSchemaResolver(JsonSchemaResolver resolver);

    /**
     * Activates or deactivates the custom format attributes provided through
     * Service Provider Interface. By default, they are activated.
     *
     * @param active {@code true} to activate the custom format attributes,
     *               {@code false} to deactivate them.
     * @return this builder.
     */
    JsonSchemaReaderFactoryBuilder withCustomFormatAttributes(boolean active);

    /**
     * Specifies the version of the JSON Schema specification. By default the latest
     * version is assigned.
     *
     * @param version the version of the JSON Schema specification.
     * @return this buidler.
     * @throws NullPointerException if the specified {@code version} is
     *                              {@code null}.
     */
    JsonSchemaReaderFactoryBuilder withSpecVersion(SpecVersion version);

    /**
     * Specifies the schema reader validates the schema against the metaschema or
     * not. By default, the schema validation is enabled.
     *
     * @param enable {@code true} to enable the validation, {@code false} to disable
     *               the validation.
     * @return this builder.
     */
    JsonSchemaReaderFactoryBuilder withSchemaValidation(boolean enable);
}
