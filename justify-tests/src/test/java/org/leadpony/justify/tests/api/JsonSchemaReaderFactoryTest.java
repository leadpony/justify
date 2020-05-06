/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.tests.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.tests.helper.SchemaExample;
import org.leadpony.justify.tests.helper.ValidationServiceType;

import jakarta.json.JsonException;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;

/**
 * Test cases for {@link JsonSchemaReaderFactory}.
 *
 * @author leadpony
 */
public class JsonSchemaReaderFactoryTest {

    /**
     * A base test for instance of {@link JsonSchemaReaderFactory}.
     *
     * @author leadpony
     */
    abstract static class AbstractTest {

        private final JsonProvider jsonProvider;
        protected final JsonValidationService service;

        protected AbstractTest(ValidationServiceType type) {
            this.jsonProvider = type.getJsonProvider();
            this.service = type.getService();
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void createSchemaReaderShouldCreateReaderFromInputStream(SchemaExample example)
                throws IOException {

            JsonSchemaReaderFactory sut = createSchemaReaderFactory(service);
            InputStream in = Files.newInputStream(getPathOf(example));

            try (JsonSchemaReader schemaReader = sut.createSchemaReader(in)) {
                JsonSchema schema = schemaReader.read();
                assertThat(schema.toJson()).isEqualTo(example.getAsJson());
            }
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void createSchemaReaderShouldCreateReaderFromInputStreamAndCharset(SchemaExample example)
                throws IOException {

            JsonSchemaReaderFactory sut = createSchemaReaderFactory(service);
            InputStream in = Files.newInputStream(getPathOf(example));

            try (JsonSchemaReader schemaReader = sut.createSchemaReader(in, example.getCharset())) {
                JsonSchema schema = schemaReader.read();
                assertThat(schema.toJson()).isEqualTo(example.getAsJson());
            }
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void createSchemaReaderShouldCreateReaderFromReader(SchemaExample example)
                throws IOException {

            JsonSchemaReaderFactory sut = createSchemaReaderFactory(service);
            Reader reader = Files.newBufferedReader(getPathOf(example));

            try (JsonSchemaReader schemaReader = sut.createSchemaReader(reader)) {
                JsonSchema schema = schemaReader.read();
                assertThat(schema.toJson()).isEqualTo(example.getAsJson());
            }
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void createSchemaReaderShouldCreateReaderFromPath(SchemaExample example)
                throws IOException {

            JsonSchemaReaderFactory sut = createSchemaReaderFactory(service);
            Path path = getPathOf(example);

            try (JsonSchemaReader schemaReader = sut.createSchemaReader(path)) {
                JsonSchema schema = schemaReader.read();
                assertThat(schema.toJson()).isEqualTo(example.getAsJson());
            }
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void createSchemaReaderShouldCreateReaderFromJsonParser(SchemaExample example)
                throws IOException {

            JsonSchemaReaderFactory sut = createSchemaReaderFactory(service);
            InputStream in = Files.newInputStream(getPathOf(example));
            JsonParser parser = getJsonProvider().createParser(in);

            try (JsonSchemaReader schemaReader = sut.createSchemaReader(parser)) {
                JsonSchema schema = schemaReader.read();
                assertThat(schema.toJson()).isEqualTo(example.getAsJson());
            }

            // The parser should be closed.
            Throwable thrown = catchThrowable(() -> {
                parser.next();
            });
            assertThat(thrown).isInstanceOf(JsonException.class);
        }

        protected JsonProvider getJsonProvider() {
            return jsonProvider;
        }

        protected JsonSchemaReaderFactory createSchemaReaderFactory(JsonValidationService service) {
            return service.createSchemaReaderFactory();
        }

        protected abstract Path getPathOf(SchemaExample example);
    }

    public static class JsonTest extends AbstractTest {

        public JsonTest() {
            super(ValidationServiceType.DEFAULT);
        }

        @Override
        protected Path getPathOf(SchemaExample example) {
            return example.getJsonPath();
        }
    }

    public static class YamlTest extends AbstractTest {

        public YamlTest() {
            super(ValidationServiceType.YAML);
        }

        @Override
        protected Path getPathOf(SchemaExample example) {
            return example.getYamlPath();
        }
    }
}
