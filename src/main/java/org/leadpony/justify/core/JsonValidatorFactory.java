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
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.core.spi.JsonValidationServiceProvider;

/**
 * Factory for creating parsers and readers which will validate 
 * JSON documents based on the specified JSON schema.
 * <p>
 * Any instance of this type is safe for use by multiple concurrent threads.
 * For most use cases, only one instance of this class is required within the application. 
 * </p>
 */
public interface JsonValidatorFactory {

    /**
     * Creates a new instance of this class.
     * 
     * @return newly created instance of this class.
     */
    static JsonValidatorFactory newFactory() {
        return JsonValidationServiceProvider.provider().createValidatorFactory();
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
     * @param handlerSupplier the function which supplies problem handlers.
     * @throws NullPointerException if specified parameter was {@code null}.
     * @return newly created instance of {@link JsonParserFactory}.
     */
    JsonParserFactory createParserFactory(Map<String,?> config, JsonSchema schema, 
            Function<JsonParser, Consumer<Problem>> handlerSupplier);
    
    /**
     * Creates a JSON parser from the specified byte stream,
     * which validates JSON document while parsing.
     * The character encoding of the stream is determined as specified in RFC 7159.
     * 
     * @param in the byte stream from which JSON is to be read.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     * @return newly created instance of {@link JsonParser}. 
     * @throws NullPointerException if specified parameter was {@code null}.
     * @throws JsonException if encoding cannot be determined or I/O error occurred. 
     */
    JsonParser createParser(InputStream in, JsonSchema schema, Consumer<Problem> handler);
    
    /**
     * Creates a JSON parser from the specified byte stream, 
     * which validates JSON document while parsing.
     * The bytes of the stream are decoded to characters using the specified charset.
     * 
     * @param in the byte stream from which JSON is to be read.
     * @param charset the character set.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     * @return newly created instance of {@link JsonParser}. 
     * @throws NullPointerException if specified parameter was {@code null}.
     * @throws JsonException if encoding cannot be determined or I/O error occurred. 
     */
    JsonParser createParser(InputStream in, Charset charset, JsonSchema schema, Consumer<Problem> handler);

    /**
     * Creates a JSON parser from the specified character stream,
     * which validates JSON document while parsing.
     * 
     * @param reader I/O reader from which JSON is to be read.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     * @return newly created instance of {@link JsonParser}. 
     * @throws NullPointerException if specified parameter was {@code null}.
     */
    JsonParser createParser(Reader reader, JsonSchema schema, Consumer<Problem> handler);
    
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
     * @param handlerSupplier the function which supplies problem handlers.
     * @throws NullPointerException if specified parameter was {@code null}.
     * @return newly created instance of {@link JsonReaderFactory}.
     */
    JsonReaderFactory createReaderFactory(Map<String, ?> config, JsonSchema schema,
            Function<JsonParser, Consumer<Problem>> handlerSupplier);
    
    /**
     * Creates a JSON reader from a byte stream, 
     * which validates JSON document while reading.
     * The character encoding of the stream is determined as described in RFC 7159.
     * 
     * @param in a byte stream from which JSON is to be read.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     * @return newly created instance of {@link JsonReader}.
     * @throws NullPointerException if specified parameter was {@code null}.
     */
    JsonReader createReader(InputStream in, JsonSchema schema, Consumer<Problem> handler);
    
    /**
     * Creates a JSON reader from a byte stream, 
     * which validates JSON document while reading.
     * The bytes of the stream are decoded to characters using the specified charset. 
     *
     * @param in a byte stream from which JSON is to be read.
     * @param charset the character set.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     * @return newly created instance of {@link JsonReader}.
     * @throws NullPointerException if specified parameter was {@code null}.
     */
    JsonReader createReader(InputStream in, Charset charset, JsonSchema schema, Consumer<Problem> handler);

    /**
     * Creates a JSON reader from a character stream,
     * which validates JSON document while reading.
     * 
     * @param reader a reader from which JSON is to be read.
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handler the object handling problems found in the validation.
     * @return newly created instance of {@link JsonReader}.
     * @throws NullPointerException if specified parameter was {@code null}.
     */
    JsonReader createReader(Reader reader, JsonSchema schema, Consumer<Problem> handler);

    /**
     * Creates a JSON provider for validating JSON documents while parsing and reading.
     * 
     * @param schema the JSON schema to apply when validating JSON document.
     * @param handlerSupplier the function which supplies problem handlers.
     * @throws NullPointerException if specified parameter was {@code null}.
     * @return newly created instance of {@link JsonProvider}.
     */
    JsonProvider createJsonProvider(JsonSchema schema,
            Function<JsonParser, Consumer<Problem>> handlerSupplier);
}
