/*
 * Copyright 2018 the Justify authors.
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

package org.leadpony.justify.core;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.core.spi.JsonValidationProvider;

/**
 * This is the facade class for creating JSON validation objects.
 * 
 * <p>
 * The following example shows how to read a JSON schema from a string:
 * </p>
 * <pre><code>
 * StringReader reader = new StringReader("{\"type\": \"integer\"}");
 * JsonSchema schema = Jsonv.readSchema(reader);
 * </code></pre>
 * 
 * <p>
 * Alternatively, a JSON schema can be built programmatically 
 * with {@link JsonSchemaBuilder}.
 * </p>
 * <pre><code>
 * JsonSchemaBuilderFactory factory = Jsonv.createSchemaBuilder();
 * JsonSchemaBuilder builder = factory.createBuilder();
 * JsonSchema schema = builder.withType(InstanceType.INTEGER).build();
 * </code></pre>
 * 
 * <p>
 * All the methods in this class are safe for use by multiple concurrent
 * threads.
 * </p>
 * 
 * @author leadpony
 * 
 * @see <a href="https://javaee.github.io/jsonp/">Java API for JSON Processing (JSON-P)</a>
 * @see <a href="http://json-b.net/">Java API for JSON Binding (JSON-B)</a>
 */
public final class Jsonv {

    private Jsonv() {
    }

    /**
     * Creates a JSON schema reader from a byte stream. 
     * The character encoding of the stream is determined as described in RFC 7159.
     * 
     * @param in the byte stream from which a JSON schema is to be read.
     * @return newly created instance of JSON schema reader.
     * @throws NullPointerException if the specified {@code in} was {@code null}.
     * @see JsonSchemaReader
     */
    public static JsonSchemaReader createSchemaReader(InputStream in) {
        return JsonValidationProvider.provider().createSchemaReader(in);
    }
    
    /**
     * Creates a JSON schema reader from a byte stream. 
     * The bytes of the stream are decoded to characters using the specified charset.
     * 
     * @param in the byte stream from which a JSON schema is to be read.
     * @param charset the character set.
     * @return newly created instance of JSON schema reader.
     * @throws NullPointerException if the specified {@code in} or {@code charset} was {@code null}.
     * @see JsonSchemaReader
     */
    public static JsonSchemaReader createSchemaReader(InputStream in, Charset charset) {
        return JsonValidationProvider.provider().createSchemaReader(in, charset);
    }

    /**
     * Creates a JSON schema reader from a reader. 
     * 
     * @param reader the reader from which a JSON schema is to be read.
     * @return newly created instance of JSON schema reader.
     * @throws NullPointerException if the specified {@code reader} was {@code null}.
     * @see JsonSchemaReader
     */
    public static JsonSchemaReader createSchemaReader(Reader reader) {
        return JsonValidationProvider.provider().createSchemaReader(reader);
    }

    /**
     * Reads a JSON schema reader from a byte stream. 
     * The character encoding of the stream is determined as described in RFC 7159.
     * 
     * @param in the byte stream from which a JSON schema is to be read.
     * @return the JSON schema.
     * @throws NullPointerException if the specified {@code in} was {@code null}.
     * @throws JsonException if an I/O error occurs while reading.
     * @throws JsonValidatingException if the reader found problems during validation of the schema.
     */
    public static JsonSchema readSchema(InputStream in) {
        try (JsonSchemaReader schemaReader = createSchemaReader(in)) {
            return schemaReader.read();
        }
    }

    /**
     * Reads a JSON schema reader from a byte stream. 
     * The bytes of the stream are decoded to characters using the specified charset.
     * 
     * @param in the byte stream from which a JSON schema is to be read.
     * @param charset the character set.
     * @return the JSON schema.
     * @throws NullPointerException if the specified {@code in} or {@code charset} was {@code null}.
     * @throws JsonException if an I/O error occurs while reading.
     * @throws JsonValidatingException if the reader found problems during validation of the schema.
     */
    public static JsonSchema readSchema(InputStream in, Charset charset) {
        try (JsonSchemaReader schemaReader = createSchemaReader(in, charset)) {
            return schemaReader.read();
        }
    }

    /**
     * Reads a JSON schema reader from a reader. 
     * 
     * @param reader the reader from which a JSON schema is to be read.
     * @return the JSON schema.
     * @throws NullPointerException if the specified {@code reader} was {@code null}.
     * @throws JsonException if an I/O error occurs while reading.
     * @throws JsonValidatingException if the reader found problems during validation of the schema.
     */
    public static JsonSchema readSchema(Reader reader) {
        try (JsonSchemaReader schemaReader = createSchemaReader(reader)) {
            return schemaReader.read();
        }
    }

    /**
     * Creates a new instance of factory for producing JSON schema builders.
     * 
     * @return the newly created instance of JSON schema builder factory.
     * @see JsonSchemaBuilderFactory
     */
    public static JsonSchemaBuilderFactory createSchemaBuilder() {
        return JsonValidationProvider.provider().createSchemaBuilderFactory();
    }

    /**
     * Creates a parser factory for creating {@link JsonParser} instances.
     * Parsers created by the factory validate JSON documents while parsing.
     * 
     * <p> 
     * The factory is configured with the specified map of configuration properties.
     * Provider implementations should ignore any unsupported configuration properties 
     * specified in the map.
     * </p> 
     *
     * @param config the map of provider-specific properties to configure the JSON parsers. 
     *        The map may be empty or {@code null}.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handlerSupplier the function to supply problem handlers, can be {@code null}.
     * @throws NullPointerException if the specified parameter was {@code null}.
     * @return newly created instance of {@link JsonParserFactory}, which is conformant to JSON-P.
     */
    public static JsonParserFactory createParserFactory(Map<String,?> config, JsonSchema schema, 
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier) {
        return JsonValidationProvider.provider()
                .createParserFactory(config, schema, handlerSupplier);
    }
    
    /**
     * Creates a JSON parser from the specified byte stream,
     * which validates JSON document while parsing.
     * The character encoding of the stream is determined as specified in RFC 7159.
     * 
     * @param in the byte stream from which JSON is to be read.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     *                If {@code null} was specified, the parser throws {@link JsonValidatingException}
     *                when finding problems in the validation.
     * @return newly created instance of {@link JsonParser}, which is conformant to JSON-P. 
     * @throws NullPointerException if the specified parameter was {@code null}.
     * @throws JsonException if encoding cannot be determined or I/O error occurred. 
     */
    public static JsonParser createParser(InputStream in, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        return JsonValidationProvider.provider()
                .createParser(in, schema, handler);
    }
    
    /**
     * Creates a JSON parser from the specified byte stream, 
     * which validates JSON document while parsing.
     * The bytes of the stream are decoded to characters using the specified charset.
     * 
     * @param in the byte stream from which JSON is to be read.
     * @param charset the character set.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     *                If {@code null} was specified, the parser throws {@link JsonValidatingException}
     *                when finding problems in the validation.
     * @return newly created instance of {@link JsonParser}, which is conformant to JSON-P. 
     * @throws NullPointerException if the specified parameter was {@code null}.
     * @throws JsonException if encoding cannot be determined or I/O error occurred. 
     */
    public static JsonParser createParser(InputStream in, Charset charset, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        return JsonValidationProvider.provider()
                .createParser(in, charset, schema, handler);
    }

    /**
     * Creates a JSON parser from the specified character stream,
     * which validates JSON document while parsing.
     * 
     * @param reader I/O reader from which JSON is to be read.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     *                If {@code null} was specified, the parser throws {@link JsonValidatingException}
     *                when finding problems in the validation.
     * @return newly created instance of {@link JsonParser}, which is conformant to JSON-P. 
     * @throws NullPointerException if the specified parameter was {@code null}.
     */
    public static JsonParser createParser(Reader reader, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        return JsonValidationProvider.provider()
                .createParser(reader, schema, handler);
    }
    
    /**
     * Creates a reader factory for creating {@link JsonReader} instances. 
     * Readers created by the factory validate JSON documents while reading.
     * <p> 
     * The factory is configured with the specified map of configuration properties. 
     * Provider implementations should ignore any unsupported configuration properties 
     * specified in the map.
     * </p> 
     * 
     * @param config the map of provider specific properties to configure the JSON readers. 
                     The map may be empty or {@code null}.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handlerSupplier the function to supply problem handlers, can be {@code null}.
     * @throws NullPointerException if the specified parameter was {@code null}.
     * @return newly created instance of {@link JsonReaderFactory}, which is conformant to JSON-P.
     */
    public static JsonReaderFactory createReaderFactory(Map<String, ?> config, JsonSchema schema,
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier) {
        return JsonValidationProvider.provider()
                .createReaderFactory(config, schema, handlerSupplier);
    }
    
    /**
     * Creates a JSON reader from a byte stream, 
     * which validates JSON document while reading.
     * The character encoding of the stream is determined as described in RFC 7159.
     * 
     * @param in a byte stream from which JSON is to be read.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     *                If {@code null} was specified, the reader throws {@link JsonValidatingException}
     *                when finding problems in the validation.
     * @return newly created instance of {@link JsonReader}, which is conformant to JSON-P.
     * @throws NullPointerException if the specified parameter was {@code null}.
     */
    public static JsonReader createReader(InputStream in, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        return JsonValidationProvider.provider()
                .createReader(in, schema, handler);
    }
    
    /**
     * Creates a JSON reader from a byte stream, 
     * which validates JSON document while reading.
     * The bytes of the stream are decoded to characters using the specified charset. 
     *
     * @param in a byte stream from which JSON is to be read.
     * @param charset the character set.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     *                If {@code null} was specified, the reader throws {@link JsonValidatingException}
     *                when finding problems in the validation.
     * @return newly created instance of {@link JsonReader}, which is conformant to JSON-P.
     * @throws NullPointerException if the specified parameter was {@code null}.
     */
    public static JsonReader createReader(InputStream in, Charset charset, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        return JsonValidationProvider.provider()
                .createReader(in, charset, schema, handler);
    }

    /**
     * Creates a JSON reader from a character stream,
     * which validates JSON document while reading.
     * 
     * @param reader a reader from which JSON is to be read.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     *                If {@code null} was specified, the reader throws {@link JsonValidatingException}
     *                when finding problems in the validation.
     * @return newly created instance of {@link JsonReader}, which is conformant to JSON-P.
     * @throws NullPointerException if the specified parameter was {@code null}.
     */
    public static JsonReader createReader(Reader reader, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        return JsonValidationProvider.provider()
                .createReader(reader, schema, handler);
    }

    /**
     * Creates a JSON provider for validating JSON documents while parsing and reading.
     * This method is intended to be used with Java API for JSON Binding (JSON-B).
     * 
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handlerSupplier the function to supply problem handlers, can be {@code null}.
     * @throws NullPointerException if the specified parameter was {@code null}.
     * @return newly created instance of {@link JsonProvider}, which is conformant to JSON-P.
     */
    public static JsonProvider createJsonProvider(JsonSchema schema,
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier) {
        return JsonValidationProvider.provider()
                .createJsonProvider(schema, handlerSupplier);
    }

    /**
     * Creates a problem handler which will store problems to the specified collection.
     * 
     * @param collection the collection to which problems will be stored.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if the specified {@code collection} was {@code null}.
     */
    public static Consumer<List<Problem>> createProblemCollector(Collection<Problem> collection) {
        Objects.requireNonNull(collection, "collection must not be null.");
        return problems->collection.addAll(problems);
    }
    
    /**
     * Creates a problem handler which will print problems 
     * with the aid of the specified line consumer.
     * 
     * @param lineConsumer the object which will output the line to somewhere.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if the specified {@code lineConsumer} was {@code null}.
     */
    public static Consumer<List<Problem>> createProblemPrinter(Consumer<String> lineConsumer) {
        return JsonValidationProvider.provider().createProblemPrinter(lineConsumer);
    }
}
