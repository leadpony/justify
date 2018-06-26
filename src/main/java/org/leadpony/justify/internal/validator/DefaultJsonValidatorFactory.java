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

package org.leadpony.justify.internal.validator;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonValidatorFactory;
import org.leadpony.justify.core.ProblemHandler;
import org.leadpony.justify.internal.base.JsonProviderDecorator;

/**
 * Default implementation of {@link JsonValidatorFactory}.
 * 
 * @author leadpony
 */
public class DefaultJsonValidatorFactory implements JsonValidatorFactory {
    
    private final JsonProvider jsonProvider;
    
    private static final Map<String, ?> defaultConfig = Collections.emptyMap();
    
    public DefaultJsonValidatorFactory(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    @Override
    public ValidatingJsonParserFactory createParserFactory(Map<String, ?> config, JsonSchema schema, 
            Function<JsonParser, ProblemHandler> handlerSupplier) {
        Objects.requireNonNull(schema, "schema must not be null.");
        if (config == null) {
            config = defaultConfig;
        }
        if (handlerSupplier == null) {
            handlerSupplier = parser->null;
        }
        JsonParserFactory realFactory = jsonProvider.createParserFactory(config);
        return new ValidatingJsonParserFactory(schema, realFactory, handlerSupplier, this.jsonProvider);
    }
    
    @Override
    public JsonParser createParser(InputStream in, JsonSchema schema, ProblemHandler handler) {
        Objects.requireNonNull(in, "in must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        return createParserFactory(defaultConfig, schema, parser->handler).createParser(in);
    }
    
    @Override
    public JsonParser createParser(InputStream in, Charset charset, JsonSchema schema, ProblemHandler handler) {
        Objects.requireNonNull(in, "in must not be null.");
        Objects.requireNonNull(charset, "charset must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        return createParserFactory(defaultConfig, schema, parser->handler).createParser(in, charset);
    }

    @Override
    public JsonParser createParser(Reader reader, JsonSchema schema, ProblemHandler handler) {
        Objects.requireNonNull(reader, "reader must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        return createParserFactory(defaultConfig, schema, parser->handler).createParser(reader);
    }
    
    @Override
    public ValidatingJsonReaderFactory createReaderFactory(Map<String, ?> config, JsonSchema schema,
            Function<JsonParser, ProblemHandler> handlerSupplier) {
        Objects.requireNonNull(schema, "schema must not be null.");
        if (config == null) {
            config = defaultConfig;
        }
        return new ValidatingJsonReaderFactory(
                createParserFactory(config, schema, handlerSupplier), config);
    }

    @Override
    public JsonReader createReader(InputStream in, JsonSchema schema, ProblemHandler handler) {
        Objects.requireNonNull(in, "in must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        return createReaderFactory(defaultConfig, schema, parser->handler).createReader(in);
    }
    
    @Override
    public JsonReader createReader(InputStream in, Charset charset, JsonSchema schema, ProblemHandler handler) {
        Objects.requireNonNull(in, "in must not be null.");
        Objects.requireNonNull(charset, "charset must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        return createReaderFactory(defaultConfig, schema, parser->handler).createReader(in, charset);
    }

    @Override
    public JsonReader createReader(Reader reader, JsonSchema schema, ProblemHandler handler) {
        Objects.requireNonNull(reader, "reader must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        return createReaderFactory(defaultConfig, schema, parser->handler).createReader(reader);
    }
    
    @Override
    public JsonProvider createJsonProvider(JsonSchema schema, 
            Function<JsonParser, ProblemHandler> handlerSupplier) {
        Objects.requireNonNull(schema, "schema must not be null.");
        return new ValidatingJsonProvider(jsonProvider, schema, handlerSupplier);
    }
    
    /**
     * {@link JsonProvider} with validation functionality.
     * 
     * @author leadpony
     */
    private class ValidatingJsonProvider extends JsonProviderDecorator {

        private final JsonSchema schema;
        private final Function<JsonParser, ProblemHandler> handlerSupplier;
        
        private ValidatingJsonProvider(JsonProvider realProvier, JsonSchema schema, 
                Function<JsonParser, ProblemHandler> handlerSupplier) {
            super(realProvier);
            this.schema = schema;
            this.handlerSupplier = handlerSupplier;
        }
 
        @Override
        public JsonParserFactory createParserFactory(Map<String, ?> config) {
            return DefaultJsonValidatorFactory.this.createParserFactory(config, schema, handlerSupplier);
        }

        @Override
        public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
            return DefaultJsonValidatorFactory.this.createReaderFactory(config, schema, handlerSupplier);
        }
    }
}
