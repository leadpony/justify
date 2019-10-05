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
package org.leadpony.justify.tests.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.JsonException;

/**
 * A test class for testing the {@link JsonValidationService} implementation.
 *
 * @author leadpony
 */
public class JsonValidationServiceTest {

    private JsonValidationService service;

    @BeforeEach
    public void setUp() {
        service = JsonValidationService.newInstance();
    }

    @Test
    public void createSchemaReaderShouldThrowJsonExceptionIfPathDoesNotExist() {
        Throwable thrown = catchThrowable(() -> {
            Path path = Paths.get("nonexistent.schema.json");
            service.createSchemaReader(path);
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
            service.readSchema(path);
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
            service.createParser(path, JsonSchema.TRUE, problems -> {
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
            service.createReader(path, JsonSchema.TRUE, problems -> {
            });
        });

        assertThat(thrown)
                .isInstanceOf(JsonException.class)
                .hasCauseInstanceOf(IOException.class)
                .hasMessageContaining("nonexistent.json");
    }

    @Test
    public void getJsonProviderShouldReturnValidJsonProvider() {
        assertThat(service.getJsonProvider()).isNotNull();
    }
}
