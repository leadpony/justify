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
package org.leadpony.justify.tests.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.tests.helper.JsonExample;
import org.leadpony.justify.tests.helper.SchemaExample;
import org.leadpony.justify.tests.helper.ValidationServiceType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.JsonException;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParser;

/**
 * A test class for testing the {@link JsonValidationService} implementation.
 *
 * @author leadpony
 */
public class JsonValidationServiceTest {

    abstract static class AbstractTest extends JsonSchemaReaderFactoryTest.AbstractTest {

        protected JsonValidationService sut;

        protected AbstractTest(ValidationServiceType type) {
            super(type);
            this.sut = this.service;
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void readSchemaShouldReadSchemaFromInputStream(SchemaExample example)
                throws IOException {

            InputStream in = Files.newInputStream(getPathOf(example));
            JsonSchema schema = sut.readSchema(in);

            assertThat(schema.toJson()).isEqualTo(example.getAsJson());
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void readSchemaShouldReadSchemaFromInputStreamAndCharset(SchemaExample example)
                throws IOException {

            InputStream in = Files.newInputStream(getPathOf(example));
            JsonSchema schema = sut.readSchema(in, example.getCharset());

            assertThat(schema.toJson()).isEqualTo(example.getAsJson());
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void readSchemaShouldReadSchemaFromReader(SchemaExample example)
                throws IOException {

            Reader reader = Files.newBufferedReader(getPathOf(example));
            JsonSchema schema = sut.readSchema(reader);

            assertThat(schema.toJson()).isEqualTo(example.getAsJson());
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void readSchemaShouldReadSchemaFromPath(SchemaExample example)
                throws IOException {

            JsonSchema schema = sut.readSchema(getPathOf(example));

            assertThat(schema.toJson()).isEqualTo(example.getAsJson());
        }

        @ParameterizedTest()
        @EnumSource(SchemaExample.class)
        public void readSchemaShouldReadSchemaFromJsonParser(SchemaExample example)
                throws IOException {

            InputStream in = Files.newInputStream(getPathOf(example));
            JsonParser parser = getJsonProvider().createParser(in);
            JsonSchema schema = sut.readSchema(parser);

            assertThat(schema.toJson()).isEqualTo(example.getAsJson());

            // The parser should be closed.
            Throwable thrown = catchThrowable(() -> {
                parser.next();
            });
            assertThat(thrown).isInstanceOf(JsonException.class);
        }

        /* Tests for createParser() */

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createParserShouldCreateParserFromInputStream(JsonExample example) {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            InputStream source = getStreamFrom(example);
            try (JsonParser parser = sut.createParser(source, schema, handler)) {
                parseAll(parser);
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createParserShouldCreateParserFromInputStreamAndCharset(JsonExample example) {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            InputStream source = getStreamFrom(example);
            try (JsonParser parser = sut.createParser(source, example.getCharset(), schema, handler)) {
                parseAll(parser);
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createParserShouldCreateParserFromReader(JsonExample example) throws IOException {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            Reader source = Files.newBufferedReader(getPathOf(example));
            try (JsonParser parser = sut.createParser(source, schema, handler)) {
                parseAll(parser);
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createParserShouldCreateParserFromPath(JsonExample example) throws IOException {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            Path source = getPathOf(example);
            try (JsonParser parser = sut.createParser(source, schema, handler)) {
                parseAll(parser);
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createParserShouldCreateParserFromJsonParser(JsonExample example) throws IOException {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            JsonParser source = getJsonProvider().createParser(getStreamFrom(example));
            try (JsonParser parser = sut.createParser(source, schema, handler)) {
                parseAll(parser);
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        /* Test for createReader() */

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createReaderShouldCreateReaderFromInputStream(JsonExample example) {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            InputStream source = getStreamFrom(example);
            try (JsonReader reader = sut.createReader(source, schema, handler)) {
                reader.readValue();
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createReaderShouldCreateReaderFromInputStreamAndCharset(JsonExample example) {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            InputStream source = getStreamFrom(example);
            try (JsonReader reader = sut.createReader(source, example.getCharset(), schema, handler)) {
                reader.readValue();
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createReaderShouldCreateReaderFromReader(JsonExample example) throws IOException {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            Reader source = Files.newBufferedReader(getPathOf(example));
            try (JsonReader reader = sut.createReader(source, schema, handler)) {
                reader.readValue();
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createReaderShouldCreateReaderFromPath(JsonExample example) throws IOException {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            Path source = getPathOf(example);
            try (JsonReader reader = sut.createReader(source, schema, handler)) {
                reader.readValue();
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        @ParameterizedTest()
        @EnumSource(JsonExample.class)
        public void createReaderShouldCreateReaderFromJsonParser(JsonExample example) throws IOException {
            List<Problem> problems = new ArrayList<>();
            ProblemHandler handler = problems::addAll;
            JsonSchema schema = sut.readSchema(getSchemaStreamFrom(example));

            JsonParser source = getJsonProvider().createParser(getStreamFrom(example));
            try (JsonReader reader = sut.createReader(source, schema, handler)) {
                reader.readValue();
            }

            assertThat(problems.isEmpty()).isEqualTo(example.isValid());
        }

        /* */

        @Test
        public void createSchemaReaderShouldThrowJsonExceptionIfPathDoesNotExist() {
            Throwable thrown = catchThrowable(() -> {
                Path path = Paths.get("nonexistent.schema.json");
                sut.createSchemaReader(path);
            });

            assertThat(thrown)
                    .isInstanceOf(JsonException.class)
                    .hasCauseInstanceOf(IOException.class)
                    .hasMessageContaining("nonexistent.schema.json");
        }

        @Test
        public void readSchemaShouldThrowJsonExceptionIfPathDoesNotExist() {
            Throwable thrown = catchThrowable(() -> {
                Path path = Paths.get("nonexistent.schema.json");
                sut.readSchema(path);
            });

            assertThat(thrown)
                    .isInstanceOf(JsonException.class)
                    .hasCauseInstanceOf(IOException.class)
                    .hasMessageContaining("nonexistent.schema.json");
        }

        @Test
        public void createParserShouldThrowJsonExceptionIfPathDoesNotExist() {
            Throwable thrown = catchThrowable(() -> {
                Path path = Paths.get("nonexistent.json");
                sut.createParser(path, JsonSchema.TRUE, problems -> {
                });
            });

            assertThat(thrown)
                    .isInstanceOf(JsonException.class)
                    .hasCauseInstanceOf(IOException.class)
                    .hasMessageContaining("nonexistent.json");
        }

        @Test
        public void createReaderShouldThrowJsonExceptionIfPathDoesNotExist() {
            Throwable thrown = catchThrowable(() -> {
                Path path = Paths.get("nonexistent.json");
                sut.createReader(path, JsonSchema.TRUE, problems -> {
                });
            });

            assertThat(thrown)
                    .isInstanceOf(JsonException.class)
                    .hasCauseInstanceOf(IOException.class)
                    .hasMessageContaining("nonexistent.json");
        }

        @Test
        public void getJsonProviderShouldReturnValidJsonProvider() {
            assertThat(sut.getJsonProvider()).isNotNull();
        }

        @Override
        protected JsonSchemaReaderFactory createSchemaReaderFactory(JsonValidationService service) {
            return service;
        }

        protected abstract Path getPathOf(JsonExample example);

        protected abstract InputStream getStreamFrom(JsonExample example);

        protected abstract InputStream getSchemaStreamFrom(JsonExample example);

        private static void parseAll(JsonParser parser) {
            while (parser.hasNext()) {
                parser.next();
            }
        }
    }

    public static class JsonTest extends AbstractTest {

        public JsonTest() {
            super(ValidationServiceType.DEFAULT);
        }

        @Override
        protected Path getPathOf(SchemaExample example) {
            return example.getJsonPath();
        }

        @Override
        protected Path getPathOf(JsonExample example) {
            return example.getJsonPath();
        }

        @Override
        protected InputStream getStreamFrom(JsonExample example) {
            return example.getJsonAsStream();
        }

        @Override
        protected InputStream getSchemaStreamFrom(JsonExample example) {
            return example.getJsonSchemaAsStream();
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

        @Override
        protected Path getPathOf(JsonExample example) {
            return example.getYamlPath();
        }

        @Override
        protected InputStream getStreamFrom(JsonExample example) {
            return example.getYamlAsStream();
        }

        @Override
        protected InputStream getSchemaStreamFrom(JsonExample example) {
            return example.getYamlSchemaAsStream();
        }
    }
}
