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

package org.leadpony.justify.internal.provider;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.JsonBuilderFactory;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.internal.validator.ValidatingJsonParser;
import org.leadpony.justify.internal.validator.ValidatingJsonParserFactory;
import org.leadpony.justify.internal.validator.ValidatingJsonReaderFactory;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.core.JsonSchemaResolver;
import org.leadpony.justify.core.JsonvException;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.spi.JsonValidationProvider;
import org.leadpony.justify.internal.base.JsonProviderDecorator;
import org.leadpony.justify.internal.base.ProblemPrinter;
import org.leadpony.justify.internal.schema.BasicSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.io.BasicSchemaReader;
import org.leadpony.justify.internal.schema.io.ValidatingSchemaReader;

/**
 * Default implementation of {@link JsonValidationProvider}.
 * 
 * @author leadpony
 */
public class DefaultJsonValidationProvider 
        extends JsonValidationProvider implements JsonSchemaResolver {
    
    private JsonProvider jsonProvider;
    private JsonSchema metaschema;
    
    private static final String METASCHEMA_NAME = "metaschema-draft-07.json";
    
    private static final Map<String, ?> defaultConfig = Collections.emptyMap();
    
    public DefaultJsonValidationProvider() {
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in) {
        requireNonNull(in, "in");
        ValidatingJsonParser parser = (ValidatingJsonParser)createParser(
                in, getMetaschema(), problem->{});
        return createValidatingSchemaReader(parser);
    }
  
    @Override
    public JsonSchemaReader createSchemaReader(InputStream in, Charset charset) {
        requireNonNull(in, "in");
        requireNonNull(charset, "charset");
        ValidatingJsonParser parser = (ValidatingJsonParser)createParser(
                in, charset, getMetaschema(), problem->{});
        return createValidatingSchemaReader(parser);
    }

    @Override
    public JsonSchemaReader createSchemaReader(Reader reader) {
        requireNonNull(reader, "reader");
        ValidatingJsonParser parser = (ValidatingJsonParser)createParser(
                reader, getMetaschema(), problem->{});
        return createValidatingSchemaReader(parser);
    }
    
    @Override
    public JsonSchemaBuilderFactory createSchemaBuilderFactory() {
        return createBasicSchemaBuilderFactory();
    }

    @Override
    public ValidatingJsonParserFactory createParserFactory(Map<String, ?> config, JsonSchema schema, 
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier) {
        requireNonNull(schema, "schema");
        if (config == null) {
            config = defaultConfig;
        }
        if (handlerSupplier == null) {
            handlerSupplier = parser->null;
        }
        JsonParserFactory realFactory = getJsonProvider().createParserFactory(config);
        return new ValidatingJsonParserFactory(schema, realFactory, handlerSupplier, 
                createJsonBuilderFactory());
    }
    
    @Override
    public JsonParser createParser(InputStream in, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        requireNonNull(in, "in");
        requireNonNull(schema, "schema");
        return createParserFactory(defaultConfig, schema, parser->handler).createParser(in);
    }
    
    @Override
    public JsonParser createParser(InputStream in, Charset charset, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        requireNonNull(in, "in");
        requireNonNull(charset, "charset");
        requireNonNull(schema, "schema");
        return createParserFactory(defaultConfig, schema, parser->handler).createParser(in, charset);
    }

    @Override
    public JsonParser createParser(Reader reader, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        requireNonNull(reader, "reader");
        requireNonNull(schema, "schema");
        return createParserFactory(defaultConfig, schema, parser->handler).createParser(reader);
    }
    
    @Override
    public ValidatingJsonReaderFactory createReaderFactory(Map<String, ?> config, JsonSchema schema,
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier) {
        requireNonNull(schema, "schema");
        if (config == null) {
            config = defaultConfig;
        }
        return new ValidatingJsonReaderFactory(
                createParserFactory(config, schema, handlerSupplier), config);
    }

    @Override
    public JsonReader createReader(InputStream in, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        requireNonNull(in, "in");
        requireNonNull(schema, "schema");
        return createReaderFactory(defaultConfig, schema, parser->handler).createReader(in);
    }
    
    @Override
    public JsonReader createReader(InputStream in, Charset charset, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        requireNonNull(in, "in");
        requireNonNull(charset, "charset");
        requireNonNull(schema, "schema");
        return createReaderFactory(defaultConfig, schema, parser->handler).createReader(in, charset);
    }

    @Override
    public JsonReader createReader(Reader reader, JsonSchema schema, Consumer<? super List<Problem>> handler) {
        requireNonNull(reader, "reader");
        requireNonNull(schema, "schema");
        return createReaderFactory(defaultConfig, schema, parser->handler).createReader(reader);
    }
   
    @Override
    public JsonProvider createJsonProvider(JsonSchema schema, 
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier) {
        requireNonNull(schema, "schema");
        return new ValidatingJsonProvider(getJsonProvider(), schema, handlerSupplier);
    }
    
    @Override
    public Consumer<List<Problem>> createProblemPrinter(Consumer<String> lineConsumer) {
        requireNonNull(lineConsumer, "lineConsumer");
        return new ProblemPrinter(lineConsumer);
    }
    
    @Override
    public JsonSchema resolveSchema(URI id) {
        requireNonNull(id, "id");
        if (id.equals(metaschema.id())) {
            return metaschema;
        } else {
            return null;
        }
    }
    
    private JsonSchema loadMetaschema(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        JsonParser parser = getJsonProvider().createParser(in);
        try (JsonSchemaReader reader = new BasicSchemaReader(parser, createBasicSchemaBuilderFactory())) {
            return reader.read();
        } catch (Exception e) {
            throw new JsonvException("Failed to read metaschema.", e);
        }
    }
    
    @SuppressWarnings("resource")
    private JsonSchemaReader createValidatingSchemaReader(ValidatingJsonParser parser) {
        return new ValidatingSchemaReader(parser, createBasicSchemaBuilderFactory())
                .withSchemaResolver(this);
    }
    
    private JsonProvider getJsonProvider() {
        if (jsonProvider == null) {
            jsonProvider = JsonProvider.provider();
        }
        return jsonProvider;
    }
    
    private JsonSchema getMetaschema() {
        if (metaschema == null) {
            metaschema = loadMetaschema(METASCHEMA_NAME);
        }
        return metaschema;
    }
    
    private JsonBuilderFactory createJsonBuilderFactory() {
        return getJsonProvider().createBuilderFactory(null);
    }
    
    private BasicSchemaBuilderFactory createBasicSchemaBuilderFactory() {
        return new BasicSchemaBuilderFactory(createJsonBuilderFactory());
    }

    /**
     * {@link JsonProvider} with validation functionality.
     * 
     * @author leadpony
     */
    private class ValidatingJsonProvider extends JsonProviderDecorator {

        private final JsonSchema schema;
        private final Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier;
        
        private ValidatingJsonProvider(JsonProvider realProvier, JsonSchema schema, 
                Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier) {
            super(realProvier);
            this.schema = schema;
            this.handlerSupplier = handlerSupplier;
        }
 
        @Override
        public JsonParserFactory createParserFactory(Map<String, ?> config) {
            return DefaultJsonValidationProvider.this
                    .createParserFactory(config, schema, handlerSupplier);
        }

        @Override
        public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
            return DefaultJsonValidationProvider.this
                    .createReaderFactory(config, schema, handlerSupplier);
        }
    }
}
