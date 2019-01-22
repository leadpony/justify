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
package org.leadpony.justify.internal.schema.io;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonSchemaReaderFactoryBuilder;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.assertion.content.ContentAttributeRegistry;
import org.leadpony.justify.internal.keyword.assertion.format.FormatAttributeRegistry;
import org.leadpony.justify.internal.schema.DefaultSchemaBuilderFactory;
import org.leadpony.justify.internal.validator.ValidatingJsonParser;

/**
 * The default implementation of {@link JsonSchemaResolver}.
 *
 * @author leadpony
 */
public class DefaultJsonSchemaReaderFactory implements JsonSchemaReaderFactory {

    private final JsonParserFactory jsonParserFactory;
    private final JsonBuilderFactory jsonBuilderFactory;

    private final FormatAttributeRegistry formatRegistry;
    private final ContentAttributeRegistry contentRegistry;
    private final JsonSchema metaschema;
    private final SchemaReaderConfiguration configuration;

    public static JsonSchemaReaderFactoryBuilder builder(JsonProvider jsonProvider,
            FormatAttributeRegistry formatRegistry, ContentAttributeRegistry contentRegistry, JsonSchema metaschema) {
        return new Builder(jsonProvider, formatRegistry, contentRegistry, metaschema);
    }

    private DefaultJsonSchemaReaderFactory(Builder builder) {
        JsonProvider jsonProvider = builder.jsonProvider;
        this.jsonParserFactory = jsonProvider.createParserFactory(null);
        this.jsonBuilderFactory = jsonProvider.createBuilderFactory(null);
        this.formatRegistry = builder.formatRegistry;
        this.contentRegistry = builder.contentRegistry;
        this.metaschema = builder.metaschema;
        this.configuration = builder;
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in) {
        requireNonNull(in, "in");
        JsonParser realParser = jsonParserFactory.createParser(in);
        ValidatingJsonParser parser = createParser(realParser);
        return createSchemaReaderWith(parser);
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in, Charset charset) {
        requireNonNull(in, "in");
        requireNonNull(charset, "charset");
        JsonParser realParser = jsonParserFactory.createParser(in, charset);
        ValidatingJsonParser parser = createParser(realParser);
        return createSchemaReaderWith(parser);
    }

    @Override
    public JsonSchemaReader createSchemaReader(Reader reader) {
        requireNonNull(reader, "reader");
        JsonParser realParser = jsonParserFactory.createParser(reader);
        ValidatingJsonParser parser = createParser(realParser);
        return createSchemaReaderWith(parser);
    }

    @Override
    public JsonSchemaReader createSchemaReader(Path path) {
        requireNonNull(path, "path");
        try {
            InputStream in = Files.newInputStream(path);
            return createSchemaReader(in);
        } catch (NoSuchFileException e) {
            throw buildJsonException(e, "schema.problem.not.found", path);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    private ValidatingJsonParser createParser(JsonParser realParser) {
        return new ValidatingJsonParser(realParser, metaschema, jsonBuilderFactory);
    }

    private DefaultSchemaBuilderFactory createSchemaBuilderFactory() {
        return new DefaultSchemaBuilderFactory(jsonBuilderFactory, formatRegistry, contentRegistry);
    }

    private JsonSchemaReader createSchemaReaderWith(ValidatingJsonParser parser) {
        DefaultSchemaBuilderFactory schemaBuilder = createSchemaBuilderFactory();
        return new Draft07SchemaReader(parser, schemaBuilder, configuration);
    }

    private static JsonException buildJsonException(NoSuchFileException e, String key, Path path) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("path", path);
        String message = Message.get(key).format(arguments);
        return new JsonException(message, e);
    }

    private static class Builder extends SchemaReaderConfiguration {

        private final JsonProvider jsonProvider;
        private final FormatAttributeRegistry formatRegistry;
        private final ContentAttributeRegistry contentRegistry;
        private final JsonSchema metaschema;
        private boolean alreadyBuilt = false;

        private Builder(JsonProvider jsonProvider, FormatAttributeRegistry formatRegistry,
                ContentAttributeRegistry contentRegistry, JsonSchema metaschema) {
            this.jsonProvider = jsonProvider;
            this.formatRegistry = formatRegistry;
            this.contentRegistry = contentRegistry;
            this.metaschema = metaschema;
        }

        @Override
        public JsonSchemaReaderFactory build() {
            checkState();
            alreadyBuilt = true;
            return new DefaultJsonSchemaReaderFactory(this);
        }

        @Override
        protected void checkState() {
            if (alreadyBuilt) {
                throw new IllegalStateException("alreay built.");
            }
        }
    }
}
