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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;

/**
 * A test class for testing the {@link JsonValidationService} implementation.
 *
 * @author leadpony
 */
public class JsonValidationServiceTest {

    private JsonValidationService sut;

    @BeforeEach
    public void setUp() {
        sut = JsonValidationService.newInstance();
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void readSchemaShouldReadSchemaFromInputStream(SchemaExample example)
            throws IOException {

        InputStream in = Files.newInputStream(example.getPath());
        JsonSchema schema = sut.readSchema(in);

        assertThat(schema.toJson()).isEqualTo(example.getAsJson());
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void readSchemaShouldReadSchemaFromInputStreamAndCharset(SchemaExample example)
            throws IOException {

        InputStream in = Files.newInputStream(example.getPath());
        JsonSchema schema = sut.readSchema(in, example.getCharset());

        assertThat(schema.toJson()).isEqualTo(example.getAsJson());
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void readSchemaShouldReadSchemaFromReader(SchemaExample example)
            throws IOException {

        Reader reader = Files.newBufferedReader(example.getPath());
        JsonSchema schema = sut.readSchema(reader);

        assertThat(schema.toJson()).isEqualTo(example.getAsJson());
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void readSchemaShouldReadSchemaFromPath(SchemaExample example)
            throws IOException {

        JsonSchema schema = sut.readSchema(example.getPath());

        assertThat(schema.toJson()).isEqualTo(example.getAsJson());
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void readSchemaShouldReadSchemaFromJsonParser(SchemaExample example)
            throws IOException {

        InputStream in = Files.newInputStream(example.getPath());
        JsonParser parser = Json.createParser(in);
        JsonSchema schema = sut.readSchema(parser);

        assertThat(schema.toJson()).isEqualTo(example.getAsJson());

        // The parser shoud be closed.
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        InputStream source = example.getAsStream();
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        InputStream source = example.getAsStream();
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        Reader source = Files.newBufferedReader(example.getPath());
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        Path source = example.getPath();
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        JsonParser source = Json.createParser(example.getAsStream());
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        InputStream source = example.getAsStream();
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        InputStream source = example.getAsStream();
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        Reader source = Files.newBufferedReader(example.getPath());
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        Path source = example.getPath();
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
        JsonSchema schema = sut.readSchema(example.getSchemaAsStream());

        JsonParser source = Json.createParser(example.getAsStream());
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

    private static void parseAll(JsonParser parser) {
        while (parser.hasNext()) {
            parser.next();
        }
    }

    @Nested
    public class JsonSchemaReaderFactoryTest implements BaseJsonSchemaReaderFactoryTest {

        @Override
        public JsonSchemaReaderFactory sut() {
            return sut;
        }
    }
}
