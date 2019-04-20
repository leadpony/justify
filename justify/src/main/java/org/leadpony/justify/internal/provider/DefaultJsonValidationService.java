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
import org.leadpony.justify.api.ProblemPrinterBuilder;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.ValidationConfig;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.json.JsonProviderDecorator;
import org.leadpony.justify.internal.base.json.DefaultJsonReader;
import org.leadpony.justify.internal.base.json.DefaultJsonReaderFactory;
import org.leadpony.justify.internal.problem.DefaultProblemPrinterBuilder;
import org.leadpony.justify.internal.schema.io.SchemaSpecRegistry;
import org.leadpony.justify.internal.schema.io.DefaultJsonSchemaReaderFactory;
import org.leadpony.justify.internal.schema.io.DefaultSchemaBuilderFactory;
import org.leadpony.justify.internal.validator.DefaultValidationConfig;
import org.leadpony.justify.internal.validator.JsonValidator;
import org.leadpony.justify.internal.validator.JsonValidatorFactory;

/**
 * The default implementation of {@link JsonValidationService}.
 *
 * @author leadpony
 */
class DefaultJsonValidationService implements JsonValidationService {

    private final JsonProvider jsonProvider;
    private final JsonBuilderFactory jsonBuilderFactory;
    private final JsonParserFactory parserFactory;
    private final JsonSchemaReaderFactory defaultSchemaReaderFactory;
    private final SchemaSpecRegistry specRegistry;

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
        this.parserFactory = jsonProvider.createParserFactory(null);
        this.specRegistry = DefaultSchemaSpecRegistry.load(jsonProvider, jsonBuilderFactory);
        this.defaultSchemaReaderFactory = createSchemaReaderFactoryBuilder().build();
    }

    /* As a JsonValidationService */

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
        return DefaultJsonSchemaReaderFactory.builder(jsonProvider, specRegistry);
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
        return createDefaultSchemaBuilderFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationConfig createValidationConfig() {
        return new DefaultValidationConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        if (config == null) {
            config = Collections.emptyMap();
        }
        if (config.containsKey(ValidationConfig.SCHEMA)) {
            return new JsonValidatorFactory(jsonProvider, parserFactory, config);
        } else {
            return jsonProvider.createParserFactory(config);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config, JsonSchema schema,
            ProblemHandlerFactory handlerFactory) {
        requireNonNull(schema, "schema");
        requireNonNull(handlerFactory, "handlerFactory");
        return createParserFactory(
                createValidationConfig()
                    .withProperties(config)
                    .withSchema(schema)
                    .withProblemHandlerFactory(handlerFactory)
                    .getAsMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonParser createParser(InputStream in, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(in, "in");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        JsonParser parser = parserFactory.createParser(in);
        return createValidator(parser, schema, handler);
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
        JsonParser parser = parserFactory.createParser(in, charset);
        return createValidator(parser, schema, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonParser createParser(Reader reader, JsonSchema schema, ProblemHandler handler) {
        requireNonNull(reader, "reader");
        requireNonNull(schema, "schema");
        requireNonNull(handler, "handler");
        JsonParser parser = parserFactory.createParser(reader);
        return createValidator(parser, schema, handler);
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
            return createParser(in, schema, handler);
        } catch (NoSuchFileException e) {
            throw buildJsonException(e, Message.INSTANCE_PROBLEM_NOT_FOUND, path);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
        if (config == null) {
            config = Collections.emptyMap();
        }
        if (config.containsKey(ValidationConfig.SCHEMA)) {
            JsonParserFactory parserFactory = createParserFactory(config);
            return new DefaultJsonReaderFactory(parserFactory);
        } else {
            return jsonProvider.createReaderFactory(config);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config, JsonSchema schema,
            ProblemHandlerFactory handlerFactory) {
        requireNonNull(schema, "schema");
        requireNonNull(handlerFactory, "handlerFactory");
        return createReaderFactory(
                createValidationConfig()
                    .withProperties(config)
                    .withSchema(schema)
                    .withProblemHandlerFactory(handlerFactory)
                    .getAsMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReader createReader(InputStream in, JsonSchema schema, ProblemHandler handler) {
        JsonParser parser = createParser(in, schema, handler);
        return createReader(parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReader createReader(InputStream in, Charset charset, JsonSchema schema, ProblemHandler handler) {
        JsonParser parser = createParser(in, charset, schema, handler);
        return createReader(parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReader createReader(Reader reader, JsonSchema schema, ProblemHandler handler) {
        JsonParser parser = createParser(reader, schema, handler);
        return createReader(parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonReader createReader(Path path, JsonSchema schema, ProblemHandler handler) {
        JsonParser parser = createParser(path, schema, handler);
        return createReader(parser);
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
    public ProblemHandler createProblemPrinter(Consumer<String> lineConsumer) {
        return createProblemPrinter(lineConsumer, Locale.getDefault());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProblemHandler createProblemPrinter(Consumer<String> lineConsumer, Locale locale) {
        requireNonNull(lineConsumer, "lineConsumer");
        requireNonNull(locale, "locale");
        return createProblemPrinterBuilder(lineConsumer).withLocale(locale).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProblemPrinterBuilder createProblemPrinterBuilder(Consumer<String> lineConsumer) {
        requireNonNull(lineConsumer, "lineConsumer");
        return new DefaultProblemPrinterBuilder(lineConsumer);
    }

    private DefaultSchemaBuilderFactory createDefaultSchemaBuilderFactory() {
        return new DefaultSchemaBuilderFactory(
                jsonBuilderFactory,
                specRegistry.getSpec(SpecVersion.DRAFT_07, true));
    }

    private static JsonException buildJsonException(NoSuchFileException e, Message message, Path path) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("path", path);
        String formatted = message.format(arguments);
        return new JsonException(formatted, e);
    }

    /**
     * Creates an instance of JSON validator.
     *
     * @param parser  the real parser.
     * @param schema  the schema.
     * @param handler the handler of found problems.
     * @return newly created validator.
     */
    @SuppressWarnings("resource")
    private JsonParser createValidator(JsonParser parser, JsonSchema schema, ProblemHandler handler) {
        return new JsonValidator(parser, schema, jsonProvider)
                .withHandler(handler);
    }

    private JsonReader createReader(JsonParser parser) {
        return new DefaultJsonReader(parser);
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
