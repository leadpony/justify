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

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.spi.JsonValidationProvider;

/**
 * The facade interface for creating JSON validation objects.
 *
 * <p>
 * The following example shows how to read a JSON schema from a string:
 * </p>
 *
 * <pre>
 * <code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * StringReader reader = new StringReader("{\"type\": \"integer\"}");
 * JsonSchema schema = service.readSchema(reader);
 * </code>
 * </pre>
 *
 * <p>
 * Alternatively, a JSON schema can be built programmatically with
 * {@link JsonSchemaBuilder}.
 * </p>
 *
 * <pre>
 * <code>
 * JsonSchemaBuilderFactory factory = service.createSchemaBuilderFactory();
 * JsonSchemaBuilder builder = factory.createBuilder();
 * JsonSchema schema = builder.withType(InstanceType.INTEGER).build();
 * </code>
 * </pre>
 *
 * <p>
 * All the methods in this class are safe for use by multiple concurrent
 * threads. For most use-cases, only one instance of JsonValidationService is
 * required within the application.
 * </p>
 *
 * @author leadpony
 *
 * @see <a href="https://javaee.github.io/jsonp/">Java API for JSON Processing
 *      (JSON-P)</a>
 * @see <a href="http://json-b.net/">Java API for JSON Binding (JSON-B)</a>
 */
public interface JsonValidationService extends JsonSchemaReaderFactory {

    /**
     * Creates a new instance of this type.
     *
     * @return newly created instance of this type, never be {@code null}.
     * @throws JsonException if an error is encountered while creating the instance.
     */
    static JsonValidationService newInstance() {
        return JsonValidationProvider.provider().createService();
    }

    /**
     * Creates a factory for creating JSON schema readers with default
     * configuration.
     *
     * @return newly created instance of JSON schema reader factory.
     */
    JsonSchemaReaderFactory createSchemaReaderFactory();

    /**
     * Creates a builder for building a JSON schema reader factory.
     *
     * @return newly created instance of JSON schema reader factory builder.
     */
    JsonSchemaReaderFactoryBuilder createSchemaReaderFactoryBuilder();

    /**
     * Reads a JSON schema from a byte stream. The character encoding of the stream
     * is determined as described in RFC 7159.
     *
     * @param in the byte stream from which a JSON schema is to be read. The
     *           specified stream will be closed automatically in this method.
     * @return the read JSON schema.
     * @throws NullPointerException    if the specified {@code in} is {@code null}.
     * @throws JsonException           if an I/O error occurs while reading.
     * @throws JsonValidatingException if the reader found problems during
     *                                 validation of the schema.
     */
    default JsonSchema readSchema(InputStream in) {
        try (JsonSchemaReader schemaReader = createSchemaReader(in)) {
            return schemaReader.read();
        }
    }

    /**
     * Reads a JSON schema from a byte stream encoded by the specified charset. The
     * bytes of the stream will be decoded to characters using the specified
     * charset.
     *
     * @param in      the byte stream from which a JSON schema is to be read. The
     *                specified stream will be closed automatically in this method.
     * @param charset the character set.
     * @return the read JSON schema.
     * @throws NullPointerException    if the specified {@code in} or
     *                                 {@code charset} is {@code null}.
     * @throws JsonException           if an I/O error occurs while reading.
     * @throws JsonValidatingException if the reader found problems during
     *                                 validation of the schema.
     */
    default JsonSchema readSchema(InputStream in, Charset charset) {
        try (JsonSchemaReader schemaReader = createSchemaReader(in, charset)) {
            return schemaReader.read();
        }
    }

    /**
     * Reads a JSON schema from a character stream.
     *
     * @param reader the character stream from which a JSON schema is to be read.
     *               The specified reader will be closed automatically in this
     *               method.
     * @return the read JSON schema.
     * @throws NullPointerException    if the specified {@code reader} is
     *                                 {@code null}.
     * @throws JsonException           if an I/O error occurs while reading.
     * @throws JsonValidatingException if the reader found problems during
     *                                 validation of the schema.
     */
    default JsonSchema readSchema(Reader reader) {
        try (JsonSchemaReader schemaReader = createSchemaReader(reader)) {
            return schemaReader.read();
        }
    }

    /**
     * Reads a JSON schema from a path.
     *
     * @param path the path from which a JSON schema is to be read.
     * @return the read JSON schema.
     * @throws NullPointerException    if the specified {@code path} is
     *                                 {@code null}.
     * @throws JsonException           if an I/O error occurs while reading.
     * @throws JsonValidatingException if the reader found problems during
     *                                 validation of the schema.
     */
    default JsonSchema readSchema(Path path) {
        try (JsonSchemaReader schemaReader = createSchemaReader(path)) {
            return schemaReader.read();
        }
    }

    /**
     * Creates a factory for creating JSON schema builders.
     *
     * @return the newly created instance of JSON schema builder factory.
     * @see JsonSchemaBuilderFactory
     */
    JsonSchemaBuilderFactory createSchemaBuilderFactory();

    /**
     * Creates a configuration for {@code JsonParser} or {@code JsonReader} with
     * validation functionality. The map generated from the configuration can be
     * passed to the methods {@link #createParserFactory(Map)} and
     * {@link #createReaderFactory(Map)}.
     *
     * @return newly created configuration, never be {@code null}.
     */
    ValidationConfig createValidationConfig();

    /**
     * Creates a parser factory for creating {@code JsonParser} instances. Parsers
     * created by the factory can validate JSON documents while parsing.
     *
     * <p>
     * The factory is configured with the specified map of configuration properties.
     * </p>
     * <p>
     * Recommended way to create the configuration properties is to generate it via
     * {@link ValidationConfig#getAsMap()}. An instance of {@link ValidationConfig}
     * can be created by calling {@link #createValidationConfig()}.
     * </p>
     *
     * @param config the map of provider-specific properties to configure the JSON
     *               parsers. The map may be empty or {@code null}.
     * @return newly created instance of {@code JsonParserFactory}, which is defined
     *         in the JSON Processing API.
     */
    JsonParserFactory createParserFactory(Map<String, ?> config);

    /**
     * Creates a parser factory for creating {@code JsonParser} instances. Parsers
     * created by the factory validate JSON documents while parsing.
     *
     * <p>
     * The factory is configured with the specified map of configuration properties.
     * </p>
     *
     * @param config         the map of provider-specific properties to configure
     *                       the JSON parsers. The map may be empty or {@code null}.
     * @param schema         the JSON schema to apply when validating JSON document.
     * @param handlerFactory the factory to supply problem handlers, cannot be
     *                       {@code null}.
     * @return newly created instance of {@code JsonParserFactory}, which is defined
     *         in the JSON Processing API.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     */
    JsonParserFactory createParserFactory(Map<String, ?> config, JsonSchema schema,
            ProblemHandlerFactory handlerFactory);

    /**
     * Creates a JSON parser from the specified byte stream, which validates the
     * JSON document while parsing. The character encoding of the stream is
     * determined as specified in RFC 7159.
     *
     * @param in      the byte stream from which JSON is to be read.
     * @param schema  the JSON schema to apply when validating JSON document.
     * @param handler the object which handles problems found during the validation,
     *                cannot be {@code null}.
     * @return newly created instance of {@code JsonParser}, which is defined in the
     *         JSON Processing API. It must be closed by the method caller after
     *         use.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     * @throws JsonException        if encoding cannot be determined or I/O error
     *                              occurred.
     */
    JsonParser createParser(InputStream in, JsonSchema schema, ProblemHandler handler);

    /**
     * Creates a JSON parser from the specified byte stream, which validates the
     * JSON document while parsing. The bytes of the stream are decoded to
     * characters using the specified charset.
     *
     * @param in      the byte stream from which JSON is to be read.
     * @param charset the character set.
     * @param schema  the JSON schema to apply when validating JSON document.
     * @param handler the object which handles problems found during the validation,
     *                cannot be {@code null}.
     * @return newly created instance of {@code JsonParser}, which is defined in the
     *         JSON Processing API. It must be closed by the method caller after
     *         use.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     * @throws JsonException        if encoding cannot be determined or I/O error
     *                              occurred.
     */
    JsonParser createParser(InputStream in, Charset charset, JsonSchema schema, ProblemHandler handler);

    /**
     * Creates a JSON parser from the specified character stream, which validates
     * the JSON document while parsing.
     *
     * @param reader  the character stream from which JSON is to be read.
     * @param schema  the JSON schema to apply when validating JSON document.
     * @param handler the object which handles problems found during the validation,
     *                cannot be {@code null}.
     * @return newly created instance of {@code JsonParser}, which is defined in the
     *         JSON Processing API. It must be closed by the method caller after
     *         use.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     */
    JsonParser createParser(Reader reader, JsonSchema schema, ProblemHandler handler);

    /**
     * Creates a JSON parser from the specified path, which validates the JSON
     * document while parsing.
     *
     * @param path    the path from which JSON is to be read.
     * @param schema  the JSON schema to apply when validating JSON document.
     * @param handler the object which handles problems found during the validation,
     *                cannot be {@code null}.
     * @return newly created instance of {@code JsonParser}, which is defined in the
     *         JSON Processing API. It must be closed by the method caller after
     *         use.
     * @throws JsonException        if an I/O error occurs while creating parser.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     */
    JsonParser createParser(Path path, JsonSchema schema, ProblemHandler handler);

    /**
     * Creates a reader factory for creating {@code JsonReader} instances. Readers
     * created by the factory can validate JSON documents while reading.
     * <p>
     * The factory is configured with the specified map of configuration properties.
     * </p>
     * <p>
     * Recommended way to create the configuration properties is to generate it via
     * {@link ValidationConfig#getAsMap()}. An instance of {@link ValidationConfig}
     * can be created by calling {@link #createValidationConfig()}.
     * </p>
     *
     * @param config the map of provider specific properties to configure the JSON
     *               readers. The map may be empty or {@code null}.
     * @return newly created instance of {@code JsonReaderFactory}, which is defined
     *         in the JSON Processing API.
     */
    JsonReaderFactory createReaderFactory(Map<String, ?> config);

    /**
     * Creates a reader factory for creating {@code JsonReader} instances. Readers
     * created by the factory validate JSON documents while reading.
     * <p>
     * The factory is configured with the specified map of configuration properties.
     * </p>
     *
     * @param config         the map of provider specific properties to configure
     *                       the JSON readers. The map may be empty or {@code null}.
     * @param schema         the JSON schema to apply when validating JSON document.
     * @param handlerFactory the factory to supply problem handlers, cannot be
     *                       {@code null}.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     * @return newly created instance of {@code JsonReaderFactory}, which is defined
     *         in the JSON Processing API.
     */
    JsonReaderFactory createReaderFactory(Map<String, ?> config, JsonSchema schema,
            ProblemHandlerFactory handlerFactory);

    /**
     * Creates a JSON reader from a byte stream, which validates the JSON document
     * while reading. The character encoding of the stream is determined as
     * described in RFC 7159.
     *
     * @param in      a byte stream from which JSON is to be read.
     * @param schema  the JSON schema to apply when validating JSON document.
     * @param handler the object which handles problems found during the validation,
     *                cannot be {@code null}.
     * @return newly created instance of {@code JsonReader}, which is defined in the
     *         JSON Processing API. It must be closed by the method caller after
     *         use.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     */
    JsonReader createReader(InputStream in, JsonSchema schema, ProblemHandler handler);

    /**
     * Creates a JSON reader from a byte stream, which validates the JSON document
     * while reading. The bytes of the stream are decoded to characters using the
     * specified charset.
     *
     * @param in      a byte stream from which JSON is to be read.
     * @param charset the character set.
     * @param schema  the JSON schema to apply when validating JSON document.
     * @param handler the object which handles problems found during the validation,
     *                cannot be {@code null}.
     * @return newly created instance of {@code JsonReader}, which is defined in the
     *         JSON Processing API. It must be closed by the method caller after
     *         use.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     */
    JsonReader createReader(InputStream in, Charset charset, JsonSchema schema, ProblemHandler handler);

    /**
     * Creates a JSON reader from a character stream, which validates the JSON
     * document while reading.
     *
     * @param reader  the character stream from which JSON is to be read.
     * @param schema  the JSON schema to apply when validating JSON document.
     * @param handler the object which handles problems found during the validation,
     *                cannot be {@code null}.
     * @return newly created instance of {@code JsonReader}, which is defined in the
     *         JSON Processing API. It must be closed by the method caller after
     *         use.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     */
    JsonReader createReader(Reader reader, JsonSchema schema, ProblemHandler handler);

    /**
     * Creates a JSON reader from a path, which validates the JSON document while
     * reading.
     *
     * @param path    the path from which JSON is to be read.
     * @param schema  the JSON schema to apply when validating JSON document.
     * @param handler the object which handles problems found during the validation,
     *                cannot be {@code null}.
     * @return newly created instance of {@code JsonReader}, which is defined in the
     *         JSON Processing API. It must be closed by the method caller after
     *         use.
     * @throws JsonException        if an I/O error occurs while creating reader.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     */
    JsonReader createReader(Path path, JsonSchema schema, ProblemHandler handler);

    /**
     * Creates a JSON provider for validating JSON documents while parsing and
     * reading. This method is intended to be used with Java API for JSON Binding
     * (JSON-B).
     *
     * @param schema         the JSON schema to apply when validating JSON document.
     * @param handlerFactory the factory to supply problem handlers, cannot be
     *                       {@code null}.
     * @throws NullPointerException if any of specified parameters is {@code null}.
     * @return newly created instance of {@code JsonProvider}, which is defined in
     *         the JSON Processing API.
     */
    JsonProvider createJsonProvider(JsonSchema schema, ProblemHandlerFactory handlerFactory);

    /**
     * Creates a problem handler which will print problems with the aid of the
     * specified line consumer.
     *
     * <p>
     * If a customized printer is needed,
     * {@link #createProblemPrinterBuilder(Consumer)} should be used instead of this
     * method.
     * </p>
     *
     * @param lineConsumer the object which will output the message lines to
     *                     somewhere.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if the specified {@code lineConsumer} is
     *                              {@code null}.
     */
    ProblemHandler createProblemPrinter(Consumer<String> lineConsumer);

    /**
     * Creates a problem handler which will print problems with the aid of the
     * specified line consumer. The messages will be localized for the specified
     * locale.
     *
     * <p>
     * If a customized printer is needed,
     * {@link #createProblemPrinterBuilder(Consumer)} should be used instead of this
     * method.
     * </p>
     *
     * @param lineConsumer the object which will output the message lines to
     *                     somewhere.
     * @param locale       the locale for which the problem messages will be
     *                     localized.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if the one of the specified parameters is
     *                              {@code null}.
     */
    ProblemHandler createProblemPrinter(Consumer<String> lineConsumer, Locale locale);

    /**
     * Creates a builder instance which can be used to build a customized problem
     * printer.
     *
     * @param lineConsumer the object which will output the message lines to
     *                     somewhere.
     * @return newly created instance of problem printer builder, which can be used
     *         to build a problem printer.
     * @throws NullPointerException if the specified {@code lineConsumer} is
     *                              {@code null}.
     */
    ProblemPrinterBuilder createProblemPrinterBuilder(Consumer<String> lineConsumer);

    /**
     * Returns the underlying JSON provider used by this service.
     *
     * @return the underlying JSON provider, never be {@code null}.
     */
    JsonProvider getJsonProvider();
}
