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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.validator.JsonValidator;

/**
 * The default implementation of {@link JsonSchemaResolver}.
 *
 * @author leadpony
 */
public class DefaultJsonSchemaReaderFactory implements JsonSchemaReaderFactory {

    private final JsonProvider jsonProvider;
    private final JsonParserFactory jsonParserFactory;
    private final JsonBuilderFactory jsonBuilderFactory;

    private final SchemaSpecRegistry specRegistry;
    private final JsonSchema metaschema;
    private final Map<String, Object> config;

    public static JsonSchemaReaderFactoryBuilder builder(
            JsonProvider jsonProvider,
            SchemaSpecRegistry specRegistry,
            JsonSchema metaschema) {
        return new Builder(jsonProvider, specRegistry, metaschema);
    }

    private DefaultJsonSchemaReaderFactory(Builder builder) {
        this.jsonProvider = builder.jsonProvider;
        this.jsonParserFactory = jsonProvider.createParserFactory(null);
        this.jsonBuilderFactory = jsonProvider.createBuilderFactory(null);
        this.specRegistry = builder.specRegistry;
        this.metaschema = builder.metaschema;
        this.config = builder.getAsMap();
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in) {
        requireNonNull(in, "in");
        JsonParser realParser = jsonParserFactory.createParser(in);
        JsonValidator parser = createParser(realParser);
        return createSchemaReader(parser);
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in, Charset charset) {
        requireNonNull(in, "in");
        requireNonNull(charset, "charset");
        JsonParser realParser = jsonParserFactory.createParser(in, charset);
        JsonValidator parser = createParser(realParser);
        return createSchemaReader(parser);
    }

    @Override
    public JsonSchemaReader createSchemaReader(Reader reader) {
        requireNonNull(reader, "reader");
        JsonParser realParser = jsonParserFactory.createParser(reader);
        JsonValidator parser = createParser(realParser);
        return createSchemaReader(parser);
    }

    @Override
    public JsonSchemaReader createSchemaReader(Path path) {
        requireNonNull(path, "path");
        try {
            InputStream in = Files.newInputStream(path);
            return createSchemaReader(in);
        } catch (NoSuchFileException e) {
            throw newJsonException(e, Message.SCHEMA_PROBLEM_NOT_FOUND, path);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    private JsonValidator createParser(JsonParser realParser) {
        return new JsonValidator(realParser, metaschema, jsonProvider);
    }

    private DefaultSchemaBuilderFactory createSchemaBuilderFactory(SpecVersion version) {
        return new DefaultSchemaBuilderFactory(
                jsonBuilderFactory,
                specRegistry.getSpec(version, usesCustomFormats()));
    }

    private JsonSchemaReader createSchemaReader(JsonValidator parser) {
        return createGenericSchemReader(parser);
    }

    @SuppressWarnings("unused")
    private JsonSchemaReader createBuildingSchemReader(JsonValidator parser) {
        DefaultSchemaBuilderFactory schemaBuilder = createSchemaBuilderFactory(SpecVersion.latest());
        return new Draft07SchemaReader(parser, schemaBuilder, config);
    }

    private JsonSchemaReader createGenericSchemReader(JsonValidator parser) {
        SchemaSpec spec = specRegistry.getSpec(SpecVersion.latest(), usesCustomFormats());
        return new GenericSchemaReader(
                parser, jsonBuilderFactory, spec, config);
    }

    private static JsonException newJsonException(NoSuchFileException e, Message message, Path path) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("path", path);
        String formatted = message.format(arguments);
        return new JsonException(formatted, e);
    }

    private boolean usesCustomFormats() {
        return config.get(JsonSchemaReader.CUSTOM_FORMATS) == Boolean.TRUE;
    }

    @SuppressWarnings("serial")
    private static class Builder extends HashMap<String, Object>
        implements JsonSchemaReaderFactoryBuilder {

        private final JsonProvider jsonProvider;
        private final SchemaSpecRegistry specRegistry;
        private final JsonSchema metaschema;
        private boolean alreadyBuilt = false;

        private List<JsonSchemaResolver> resolvers = new ArrayList<>();

        private Builder(
                JsonProvider jsonProvider,
                SchemaSpecRegistry specRegistry,
                JsonSchema metaschema) {
            this.jsonProvider = jsonProvider;
            this.specRegistry = specRegistry;
            this.metaschema = metaschema;
            put(JsonSchemaReader.CUSTOM_FORMATS, true);
            put(JsonSchemaReader.RESOLVERS, resolvers);
        }

        Map<String, Object> getAsMap() {
            return Collections.unmodifiableMap(this);
        }

        /* As a JsonSchemaReaderFactoryBuilder */

        @Override
        public JsonSchemaReaderFactory build() {
            checkState();
            alreadyBuilt = true;
            return new DefaultJsonSchemaReaderFactory(this);
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withStrictWithKeywords(boolean strict) {
            checkState();
            put(JsonSchemaReader.STRICT_KEYWORDS, strict);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withStrictWithFormats(boolean strict) {
            checkState();
            put(JsonSchemaReader.STRICT_FORMATS, strict);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withSchemaResolver(JsonSchemaResolver resolver) {
            checkState();
            requireNonNull(resolver, "resolver");
            resolvers.add(resolver);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withCustomFormatAttributes(boolean active) {
            checkState();
            put(JsonSchemaReader.CUSTOM_FORMATS, active);
            return this;
        }

        private void checkState() {
            if (alreadyBuilt) {
                throw new IllegalStateException("already built.");
            }
        }
    }
}
