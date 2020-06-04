/*
 * Copyright 2018-2020 the Justify authors.
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
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.json.JsonException;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonSchemaReaderFactoryBuilder;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.KeywordValueSetLoader;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.ResettableInputStream;
import org.leadpony.justify.internal.base.ResettableReader;
import org.leadpony.justify.internal.base.json.JsonService;
import org.leadpony.justify.internal.schema.SchemaCatalog;
import org.leadpony.justify.internal.schema.SchemaSpec;

/**
 * The default implementation of {@link JsonSchemaResolver}.
 *
 * @author leadpony
 */
public class JsonSchemaReaderFactoryImpl implements JsonSchemaReaderFactory {

    private final JsonService jsonService;
    protected final JsonParserFactory jsonParserFactory;
    private final KeywordValueSetLoader keywordValuesLoader;

    private final SchemaCatalog catalog;
    protected final SpecVersion defaultVersion;
    private final JsonSchema metaschema;
    private final Map<String, Object> config;

    public static JsonSchemaReaderFactoryBuilder builder(
            JsonService jsonService,
            KeywordValueSetLoader keywordValuesLoader,
            SchemaCatalog catalog) {
        return new Builder(jsonService, keywordValuesLoader, catalog);
    }

    protected JsonSchemaReaderFactoryImpl(Builder builder) {
        this.jsonService = builder.jsonService;
        this.jsonParserFactory = this.jsonService.getJsonParserFactory();
        this.keywordValuesLoader = builder.keywordValuesLoader;
        this.catalog = builder.catalog;
        this.config = builder.getConfigAsMap();
        this.defaultVersion = (SpecVersion) this.config.get(JsonSchemaReader.DEFAULT_SPEC_VERSION);
        this.metaschema = (JsonSchema) this.config.get(JsonSchemaReader.METASCHEMA);
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in) {
        requireNonNull(in, "in");
        SchemaSpec spec = getSpec(defaultVersion);
        JsonParser realParser = jsonParserFactory.createParser(in);
        return createSpecificSchemaReader(realParser, spec);
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in, Charset charset) {
        requireNonNull(in, "in");
        requireNonNull(charset, "charset");
        SchemaSpec spec = getSpec(defaultVersion);
        JsonParser realParser = jsonParserFactory.createParser(in, charset);
        return createSpecificSchemaReader(realParser, spec);
    }

    @Override
    public JsonSchemaReader createSchemaReader(Reader reader) {
        requireNonNull(reader, "reader");
        SchemaSpec spec = getSpec(defaultVersion);
        JsonParser realParser = jsonParserFactory.createParser(reader);
        return createSpecificSchemaReader(realParser, spec);
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

    @Override
    public JsonSchemaReader createSchemaReader(JsonParser parser) {
        requireNonNull(parser, "parser");
        SchemaSpec spec = getSpec(defaultVersion);
        return createSpecificSchemaReader(parser, spec);
    }

    /**
     * Returns the instance of {@link SchemaSpec} for the specified version.
     *
     * @param version the version of the specification.
     * @return the instance of {@link SchemaSpec}
     */
    protected SchemaSpec getSpec(SpecVersion version) {
        return SchemaSpec.get(version);
    }

    private JsonSchema getMetaschema(SchemaSpec spec) {
        if (this.metaschema != null) {
            return metaschema;
        }
        return catalog.resolveSchema(spec.getVersion().id());
    }

    private Map<String, KeywordType> buildKeywordMap(SchemaSpec spec) {
        return spec.getVocabularies().stream()
            .flatMap(v -> v.getKeywordTypes(this.config, this.keywordValuesLoader).stream())
            .collect(Collectors.toMap(KeywordType::name, Function.identity()));
    }

    /**
     * Creates a schema reader for the specified version of specification.
     *
     * @param realParser the real JSON parser.
     * @param spec       the specification.
     * @return newly created schema reader.
     */
    protected JsonSchemaReader createSpecificSchemaReader(JsonParser realParser, SchemaSpec spec) {
        JsonSchema metaschema = null;
        if (testOption(JsonSchemaReader.SCHEMA_VALIDATION)) {
            metaschema = getMetaschema(spec);
        }
        return new JsonSchemaReaderImpl(realParser, jsonService, buildKeywordMap(spec), config, metaschema);
    }

    private static JsonException newJsonException(NoSuchFileException e, Message message, Path path) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("path", path);
        String formatted = message.format(arguments);
        return new JsonException(formatted, e);
    }

    private boolean testOption(String option) {
        return config.get(option) == Boolean.TRUE;
    }

    /**
     * A factory type for creating schema readers detecting the spec version.
     *
     * @author leadpony
     */
    private static final class DetectableJsonSchemaReaderFactory extends JsonSchemaReaderFactoryImpl {

        private DetectableJsonSchemaReaderFactory(Builder builder) {
            super(builder);
        }

        @Override
        public JsonSchemaReader createSchemaReader(InputStream in) {
            requireNonNull(in, "in");
            ResettableInputStream probeStream = new ResettableInputStream(in);
            JsonParser probeParser = jsonParserFactory.createParser(probeStream);
            return new AbstractProbeSchemaReader(probeParser, defaultVersion) {
                @Override
                protected JsonSchemaReader createSchemaReader(SpecVersion version) {
                    SchemaSpec spec = getSpec(version);
                    JsonParser realParser = jsonParserFactory.createParser(
                            probeStream.createResettedStream());
                    return createSpecificSchemaReader(realParser, spec);
                }
            };
        }

        @Override
        public JsonSchemaReader createSchemaReader(InputStream in, Charset charset) {
            requireNonNull(in, "in");
            requireNonNull(charset, "charset");
            ResettableInputStream probeStream = new ResettableInputStream(in);
            JsonParser probeParser = jsonParserFactory.createParser(probeStream, charset);
            return new AbstractProbeSchemaReader(probeParser, defaultVersion) {
                @Override
                protected JsonSchemaReader createSchemaReader(SpecVersion version) {
                    SchemaSpec spec = getSpec(version);
                    JsonParser realParser = jsonParserFactory.createParser(
                            probeStream.createResettedStream(), charset);
                    return createSpecificSchemaReader(realParser, spec);
                }
            };
        }

        @Override
        public JsonSchemaReader createSchemaReader(Reader reader) {
            requireNonNull(reader, "reader");
            ResettableReader probeReader = new ResettableReader(reader);
            JsonParser probeParser = jsonParserFactory.createParser(probeReader);
            return new AbstractProbeSchemaReader(probeParser, defaultVersion) {
                @Override
                protected JsonSchemaReader createSchemaReader(SpecVersion version) {
                    SchemaSpec spec = getSpec(version);
                    JsonParser realParser = jsonParserFactory.createParser(
                            probeReader.createResettedReader());
                    return createSpecificSchemaReader(realParser, spec);
                }
            };
        }
    }

    /**
     * A builder type for building instances of {@link JsonSchemaReaderFactory}.
     *
     * @author leadpony
     */
    private static final class Builder implements JsonSchemaReaderFactoryBuilder {

        private final JsonService jsonService;
        private final KeywordValueSetLoader keywordValuesLoader;
        private final SchemaCatalog catalog;

        private Map<String, Object> properties;

        private Builder(JsonService jsonService, KeywordValueSetLoader keywordValuesLoader,
                SchemaCatalog catalog) {
            this.jsonService = jsonService;
            this.keywordValuesLoader = keywordValuesLoader;
            this.catalog = catalog;
        }

        Map<String, Object> getConfigAsMap() {
            return Collections.unmodifiableMap(getProperties());
        }

        /* As a JsonSchemaReaderFactoryBuilder */

        @Override
        public JsonSchemaReaderFactory build() {
            JsonSchemaReaderFactory factory;
            Map<String, Object> props = getProperties();
            if (props.get(JsonSchemaReader.SPEC_VERSION_DETECTION) == Boolean.TRUE) {
                factory = new DetectableJsonSchemaReaderFactory(this);
            } else {
                factory = new JsonSchemaReaderFactoryImpl(this);
            }
            this.properties = null;
            return factory;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withStrictKeywords(boolean strict) {
            getProperties().put(JsonSchemaReader.STRICT_KEYWORDS, strict);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withStrictFormats(boolean strict) {
            getProperties().put(JsonSchemaReader.STRICT_FORMATS, strict);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withSchemaResolver(JsonSchemaResolver resolver) {
            requireNonNull(resolver, "resolver");
            @SuppressWarnings("unchecked")
            List<JsonSchemaResolver> resolvers = (List<JsonSchemaResolver>) getProperties()
                    .get(JsonSchemaReader.RESOLVERS);
            resolvers.add(resolver);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withCustomFormatAttributes(boolean enabled) {
            getProperties().put(JsonSchemaReader.CUSTOM_FORMATS, enabled);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withDefaultSpecVersion(SpecVersion version) {
            requireNonNull(version, "version");
            getProperties().put(JsonSchemaReader.DEFAULT_SPEC_VERSION, version);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withSchemaValidation(boolean enabled) {
            getProperties().put(JsonSchemaReader.SCHEMA_VALIDATION, enabled);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withSpecVersionDetection(boolean enabled) {
            getProperties().put(JsonSchemaReader.SPEC_VERSION_DETECTION, enabled);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withMetaschema(JsonSchema metaschema) {
            requireNonNull(metaschema, "metaschema");
            getProperties().put(JsonSchemaReader.METASCHEMA, metaschema);
            return this;
        }

        private Map<String, Object> getProperties() {
            if (this.properties == null) {
                this.properties = createDefaultProperties();
            }
            return this.properties;
        }

        /**
         * Defaultizes this builder.
         */
        private Map<String, Object> createDefaultProperties() {
            Map<String, Object> props = new HashMap<>();

            props.put(JsonSchemaReader.CUSTOM_FORMATS, true);
            props.put(JsonSchemaReader.DEFAULT_SPEC_VERSION, SpecVersion.current());
            props.put(JsonSchemaReader.SCHEMA_VALIDATION, true);
            props.put(JsonSchemaReader.SPEC_VERSION_DETECTION, true);

            List<JsonSchemaResolver> resolvers = new ArrayList<>();
            resolvers.add(this.catalog);
            props.put(JsonSchemaReader.RESOLVERS, resolvers);

            return props;
        }
    }
}
