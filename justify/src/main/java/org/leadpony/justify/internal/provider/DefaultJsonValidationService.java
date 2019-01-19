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

package org.leadpony.justify.internal.provider;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaBuilderFactory;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonSchemaReaderFactoryBuilder;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ProblemHandlerFactory;
import org.leadpony.justify.internal.base.JsonProviderDecorator;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.ProblemPrinter;
import org.leadpony.justify.internal.keyword.assertion.content.ContentAttributeRegistry;
import org.leadpony.justify.internal.keyword.assertion.format.FormatAttributeRegistry;
import org.leadpony.justify.internal.schema.DefaultSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.io.Draft07SchemaReader;
import org.leadpony.justify.internal.schema.io.DefaultJsonSchemaReaderFactory;
import org.leadpony.justify.internal.validator.ValidatingJsonParserFactory;
import org.leadpony.justify.internal.validator.ValidatingJsonReaderFactory;

/**
 * Default implementation of {@link JsonValidationService}.
 *
 * @author leadpony
 */
class DefaultJsonValidationService implements JsonValidationService {

    private final JsonProvider jsonProvider;
    private final JsonBuilderFactory jsonBuilderFactory;
    private final FormatAttributeRegistry formatRegistry;
    private final ContentAttributeRegistry contentRegistry;
    private final JsonSchema metaschema;
    private final JsonSchemaReaderFactory defaultSchemaReaderFactory;

    private static final String METASCHEMA_NAME = "metaschema-draft-07.json";

    private static final Map<String, ?> DEFAULT_CONFIG = Collections.emptyMap();

    /**
     * Constructs this object.
     *
     * @param jsonProvider the JSON provider.
     * @throws JsonException if an error is encountered while reading the
     *                       metaschema.
     */
    DefaultJsonValidationService(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.jsonBuilderFactory = jsonProvider.createBuilderFactory(null);
        this.formatRegistry = createFormatAttributeRegistry();
        this.contentRegistry = createContentAttributeRegistry(jsonProvider);
        this.metaschema = loadMetaschema(METASCHEMA_NAME);
        this.defaultSchemaReaderFactory = createSchemaReaderFactoryBuilder().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSchemaReaderFactory createSchemaReaderFactory() {
        return defaultSchemaReaderFactory;
    }

    /**
     * {@inheritDoc}
     */
    public JsonSchemaReaderFactoryBuilder createSchemaReaderFactoryBuilder() {
        return DefaultJsonSchemaReaderFactory.builder(jsonProvider, formatRegistry, contentRegistry, metaschema);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSchemaReader createSchemaReader(InputStream in) {
        return createSchemaReaderFactory().createSchemaReader(in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSchemaReader createSchemaReader(InputStream in, Charset charset) {
        return createSchemaReaderFactory().createSchemaReader(in, charset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSchemaReader createSchemaReader(Reader reader) {
        return createSchemaReaderFactory().createSchemaReader(reader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSchemaReader createSchemaReader(Path path) {
        return createSchemaReaderFactory().createSchemaReader(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonSchemaBuilderFactory createSchemaBuilderFactory() {
        return createBasicSchemaBuilderFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatingJsonParserFactory createParserFactory(Map<String, ?> config, JsonSchema schema,
            ProblemHandlerFactory handlerFactory) {
        requireNonNull(schema, "schema");
        requireNonNull(handlerFactory, "handlerFactory");
        if (config == null) {
            config = DEFAULT_CONFIG;
        }
        JsonParserFactory realFactory = jsonProvider.createParserFactory(config);
        return new ValidatingJsonParserFactory(schema, realFactory, handlerFactory, jsonBuilderFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonParser createParser(InputStream in, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(in, "in");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        return createParserFactory(DEFAULT_CONFIG, schema, parser -> handler).createParser(in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonParser createParser(InputStream in, Charset charset, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(in, "in");
        requireNonNull(charset, "charset");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        return createParserFactory(DEFAULT_CONFIG, schema, parser -> handler).createParser(in, charset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonParser createParser(Reader reader, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(reader, "reader");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        return createParserFactory(DEFAULT_CONFIG, schema, parser -> handler).createParser(reader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonParser createParser(Path path, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(path, "path");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        try {
            InputStream in = Files.newInputStream(path);
            return createParserFactory(DEFAULT_CONFIG, schema, parser -> handler).createParser(in);
        } catch (NoSuchFileException e) {
            throw buildJsonException(e, "instance.problem.not.found", path);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatingJsonReaderFactory createReaderFactory(Map<String, ?> config, JsonSchema schema,
            ProblemHandlerFactory handlerFactory) {
        requireNonNull(schema, "schema");
        requireNonNull(handlerFactory, "handlerFactory");
        if (config == null) {
            config = DEFAULT_CONFIG;
        }
        return new ValidatingJsonReaderFactory(createParserFactory(config, schema, handlerFactory), config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReader createReader(InputStream in, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(in, "in");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        return createReaderFactory(DEFAULT_CONFIG, schema, parser -> handler).createReader(in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReader createReader(InputStream in, Charset charset, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(in, "in");
        requireNonNull(charset, "charset");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        return createReaderFactory(DEFAULT_CONFIG, schema, parser -> handler).createReader(in, charset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReader createReader(Reader reader, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(reader, "reader");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        return createReaderFactory(DEFAULT_CONFIG, schema, parser -> handler).createReader(reader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReader createReader(Path path, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(path, "path");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        try {
            InputStream in = Files.newInputStream(path);
            return createReaderFactory(DEFAULT_CONFIG, schema, parser -> handler).createReader(in);
        } catch (NoSuchFileException e) {
            throw buildJsonException(e, "instance.problem.not.found", path);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonProvider createJsonProvider(JsonSchema schema, ProblemHandlerFactory handlerFactory) {
        requireNonNull(schema, "schema");
        requireNonNull(handlerFactory, "handlerFactory");
        return new ValidatingJsonProvider(jsonProvider, schema, handlerFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProblemHandler createProblemPrinter(Consumer<String> lineConsumer, Locale locale) {
        requireNonNull(lineConsumer, "lineConsumer");
        requireNonNull(locale, "locale");
        return new ProblemPrinter(lineConsumer, locale);
    }

    protected FormatAttributeRegistry createFormatAttributeRegistry() {
        return new FormatAttributeRegistry().registerDefault().registerProvidedFormatAttributes();
    }

    protected ContentAttributeRegistry createContentAttributeRegistry(JsonProvider jsonProvider) {
        return new ContentAttributeRegistry(jsonProvider).registerProvidedEncodingSchemes().registerProvidedMimeTypes()
                .registerDefault();
    }

    /**
     * Loads metaschema from the resource on classpath.
     *
     * @param name the name of the resource.
     * @return the loaded schema.
     * @throws JsonException if an error is encountered while reading the
     *                       metaschema.
     */
    private JsonSchema loadMetaschema(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        JsonParser realParser = jsonProvider.createParser(in);
        try (JsonSchemaReader reader = new Draft07SchemaReader(realParser, createBasicSchemaBuilderFactory())) {
            return reader.read();
        }
    }

    private DefaultSchemaBuilderFactory createBasicSchemaBuilderFactory() {
        return new DefaultSchemaBuilderFactory(jsonBuilderFactory, formatRegistry, contentRegistry);
    }

    private static JsonException buildJsonException(NoSuchFileException e, String key, Path path) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("path", path);
        String message = Message.get(key).format(arguments);
        return new JsonException(message, e);
    }

    /**
     * {@link JsonProvider} with validation functionality.
     *
     * @author leadpony
     */
    private class ValidatingJsonProvider extends JsonProviderDecorator {

        private final JsonSchema schema;
        private final ProblemHandlerFactory handlerFactory;

        private ValidatingJsonProvider(JsonProvider realProvier, JsonSchema schema,
                ProblemHandlerFactory handlerFactory) {
            super(realProvier);
            this.schema = schema;
            this.handlerFactory = handlerFactory;
        }

        @Override
        public JsonParserFactory createParserFactory(Map<String, ?> config) {
            return DefaultJsonValidationService.this.createParserFactory(config, schema, handlerFactory);
        }

        @Override
        public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
            return DefaultJsonValidationService.this.createReaderFactory(config, schema, handlerFactory);
        }
    }
}
