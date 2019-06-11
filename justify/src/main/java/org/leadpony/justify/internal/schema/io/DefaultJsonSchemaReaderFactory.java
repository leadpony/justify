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

import javax.json.JsonException;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonSchemaReaderFactoryBuilder;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.ResettableInputStream;
import org.leadpony.justify.internal.base.ResettableReader;
import org.leadpony.justify.internal.base.json.DefaultPointerAwareJsonParser;
import org.leadpony.justify.internal.base.json.JsonService;
import org.leadpony.justify.internal.base.json.PointerAwareJsonParser;
import org.leadpony.justify.internal.validator.JsonValidator;

/**
 * The default implementation of {@link JsonSchemaResolver}.
 *
 * @author leadpony
 */
public class DefaultJsonSchemaReaderFactory implements JsonSchemaReaderFactory {

    private final JsonService jsonService;
    protected final JsonParserFactory jsonParserFactory;

    private final SchemaSpecRegistry specRegistry;
    protected final SpecVersion defaultVersion;
    private final Map<String, Object> config;

    public static JsonSchemaReaderFactoryBuilder builder(
            JsonService jsonService,
            SchemaSpecRegistry specRegistry) {
        return new Builder(jsonService, specRegistry);
    }

    protected DefaultJsonSchemaReaderFactory(Builder builder) {
        this.jsonService = builder.jsonService;
        this.jsonParserFactory = this.jsonService.getJsonParserFactory();
        this.specRegistry = builder.specRegistry;
        this.config = builder.getAsMap();
        this.defaultVersion = (SpecVersion) this.config.get(JsonSchemaReader.DEFAULT_SPEC_VERSION);
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

    /**
     * Returns the instance of {@link SchemaSpec} for the specified version.
     *
     * @param version the version of the specification.
     * @return the instance of {@link SchemaSpec}
     */
    protected SchemaSpec getSpec(SpecVersion version) {
        return specRegistry.getSpec(
                version,
                testOption(JsonSchemaReader.CUSTOM_FORMATS));
    }

    private PointerAwareJsonParser createParser(JsonParser realParser, SchemaSpec spec) {
        if (testOption(JsonSchemaReader.SCHEMA_VALIDATION)) {
            return new JsonValidator(realParser, spec.getMetaschema(), jsonService.getJsonProvider());
        } else {
            return new DefaultPointerAwareJsonParser(realParser, jsonService.getJsonProvider());
        }
    }

    /**
     * Creates a schema reader for the specified version of specification.
     *
     * @param realParser the real JSON parser.
     * @param spec the specification.
     * @return newly created schema reader.
     */
    protected JsonSchemaReader createSpecificSchemaReader(JsonParser realParser, SchemaSpec spec) {
        PointerAwareJsonParser parser = createParser(realParser, spec);
        return new GenericSchemaReader(parser, jsonService, spec, config);
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
    private static final class DetectableJsonSchemaReaderFactory extends DefaultJsonSchemaReaderFactory {

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
    @SuppressWarnings("serial")
    private static final class Builder extends HashMap<String, Object> implements JsonSchemaReaderFactoryBuilder {

        private final JsonService jsonService;
        private final SchemaSpecRegistry specRegistry;
        private boolean alreadyBuilt = false;

        private List<JsonSchemaResolver> resolvers = new ArrayList<>();

        private Builder(JsonService jsonService, SchemaSpecRegistry specRegistry) {
            this.jsonService = jsonService;
            this.specRegistry = specRegistry;
            defaultize();
        }

        Map<String, Object> getAsMap() {
            return Collections.unmodifiableMap(this);
        }

        /* As a JsonSchemaReaderFactoryBuilder */

        @Override
        public JsonSchemaReaderFactory build() {
            checkState();
            try {
                if (get(JsonSchemaReader.SPEC_VERSION_DETECTION) == Boolean.TRUE) {
                    return new DetectableJsonSchemaReaderFactory(this);
                } else {
                    return new DefaultJsonSchemaReaderFactory(this);
                }
            } finally {
                alreadyBuilt = true;
            }
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
        public JsonSchemaReaderFactoryBuilder withCustomFormatAttributes(boolean enabled) {
            checkState();
            put(JsonSchemaReader.CUSTOM_FORMATS, enabled);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withDefaultSpecVersion(SpecVersion version) {
            checkState();
            requireNonNull(version, "version");
            put(JsonSchemaReader.DEFAULT_SPEC_VERSION, version);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withSchemaValidation(boolean enabled) {
            checkState();
            put(JsonSchemaReader.SCHEMA_VALIDATION, enabled);
            return this;
        }

        @Override
        public JsonSchemaReaderFactoryBuilder withSpecVersionDetection(boolean enabled) {
            checkState();
            put(JsonSchemaReader.SPEC_VERSION_DETECTION, enabled);
            return this;
        }

        private void checkState() {
            if (alreadyBuilt) {
                throw new IllegalStateException("already built.");
            }
        }

        /**
         * Defaultizes this builder.
         */
        private void defaultize() {
            put(JsonSchemaReader.CUSTOM_FORMATS, true);
            put(JsonSchemaReader.RESOLVERS, resolvers);
            put(JsonSchemaReader.DEFAULT_SPEC_VERSION, SpecVersion.current());
            put(JsonSchemaReader.SCHEMA_VALIDATION, true);
            put(JsonSchemaReader.SPEC_VERSION_DETECTION, true);
            withSchemaResolver(specRegistry.getMetaschemaCatalog());
        }
    }
}
