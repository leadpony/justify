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

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.tests.helper.ApiTest;
import org.leadpony.justify.tests.helper.SchemaExample;

/**
 * Test cases for {@link JsonSchemaReaderFactory}.
 *
 * @author leadpony
 */
@ApiTest
public class JsonSchemaReaderFactoryTest {

    private static JsonValidationService service;

    private JsonSchemaReaderFactory sut;

    @BeforeEach
    public void setUp() {
        sut = service.createSchemaReaderFactory();
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void createSchemaReaderShouldCreateReaderFromInputStream(SchemaExample example)
            throws IOException {

        InputStream in = Files.newInputStream(example.getSchemaPath());

        try (JsonSchemaReader schemaReader = sut.createSchemaReader(in)) {
            JsonSchema schema = schemaReader.read();
            assertThat(schema.toJson()).isEqualTo(example.getSchemaAsJson());
        }
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void createSchemaReaderShouldCreateReaderFromInputStreamAndCharset(SchemaExample example)
            throws IOException {

        InputStream in = Files.newInputStream(example.getSchemaPath());

        try (JsonSchemaReader schemaReader = sut.createSchemaReader(in, example.getCharset())) {
            JsonSchema schema = schemaReader.read();
            assertThat(schema.toJson()).isEqualTo(example.getSchemaAsJson());
        }
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void createSchemaReaderShouldCreateReaderFromReader(SchemaExample example)
            throws IOException {

        Reader reader = Files.newBufferedReader(example.getSchemaPath());

        try (JsonSchemaReader schemaReader = sut.createSchemaReader(reader)) {
            JsonSchema schema = schemaReader.read();
            assertThat(schema.toJson()).isEqualTo(example.getSchemaAsJson());
        }
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void createSchemaReaderShouldCreateReaderFromPath(SchemaExample example)
            throws IOException {

        Path path = example.getSchemaPath();

        try (JsonSchemaReader schemaReader = sut.createSchemaReader(path)) {
            JsonSchema schema = schemaReader.read();
            assertThat(schema.toJson()).isEqualTo(example.getSchemaAsJson());
        }
    }

    @ParameterizedTest()
    @EnumSource(SchemaExample.class)
    public void createSchemaReaderShouldCreateReaderFromJsonParser(SchemaExample example)
            throws IOException {

        InputStream in = Files.newInputStream(example.getSchemaPath());
        JsonParser parser = Json.createParser(in);

        try (JsonSchemaReader schemaReader = sut.createSchemaReader(parser)) {
            JsonSchema schema = schemaReader.read();
            assertThat(schema.toJson()).isEqualTo(example.getSchemaAsJson());
        }

        Throwable thrown = catchThrowable(() -> {
            parser.next();
        });
        assertThat(thrown).isInstanceOf(JsonException.class);
    }
}
