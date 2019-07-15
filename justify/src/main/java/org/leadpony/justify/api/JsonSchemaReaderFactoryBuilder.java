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
 *     .withStrictKeywords(true)
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
    JsonSchemaReaderFactoryBuilder withStrictKeywords(boolean strict);

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
    JsonSchemaReaderFactoryBuilder withStrictFormats(boolean strict);

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
     * Enables or disables the custom format attributes provided through Service
     * Provider Interface. By default, they are enabled.
     *
     * @param enabled {@code true} to enable the custom format attributes,
     *                {@code false} to disable them.
     * @return this builder.
     */
    JsonSchemaReaderFactoryBuilder withCustomFormatAttributes(boolean enabled);

    /**
     * Specifies the default version of the JSON Schema specification. By default
     * the current version is assigned.
     *
     * @param version the default version of the JSON Schema specification.
     * @return this buidler.
     * @throws NullPointerException if the specified {@code version} is
     *                              {@code null}.
     */
    JsonSchemaReaderFactoryBuilder withDefaultSpecVersion(SpecVersion version);

    /**
     * Specifies whether the schema reader validates the schema against the
     * metaschema or not. By default, the schema validation is enabled.
     *
     * @param enabled {@code true} to enable the validation, {@code false} to
     *                disable the validation.
     * @return this builder.
     */
    JsonSchemaReaderFactoryBuilder withSchemaValidation(boolean enabled);

    /**
     * Enables or disables the automatic detection of specification version. By
     * default this option is enabled.
     *
     * @param enabled {@code true} to enable the detection of specification version,
     *                {@code false} to disable it.
     * @return this builder.
     */
    JsonSchemaReaderFactoryBuilder withSpecVersionDetection(boolean enabled);

    /**
     * Specifies the metaschema to be used when validating the schema.
     *
     * <p>
     * By default, the official metaschema provided by JSON Schema specification
     * will be automatically used in accordance with the identifier specified by
     * {@code $schema} keyword in the JSON schema.
     * </p>
     *
     * @param metaschema the metaschema to be used for validation of the schema.
     * @return this buidler.
     * @throws NullPointerException if the specified {@code metaschema} is
     *                              {@code null}.
     */
    JsonSchemaReaderFactoryBuilder withMetaschema(JsonSchema metaschema);
}
