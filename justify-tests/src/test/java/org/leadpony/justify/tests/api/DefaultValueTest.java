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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ValidationConfig;
import org.leadpony.justify.tests.helper.ApiTest;
import org.leadpony.justify.tests.helper.ProblemPrinter;

/**
 * A test class for filling with default values.
 *
 * @author leadpony
 */
@ApiTest
public class DefaultValueTest {

    private static JsonValidationService service;
    private static ProblemPrinter printer;

    /**
     * A fixture for this test.
     *
     * @author leadpony
     */
    private static class Fixture {

        final JsonValue schema;
        final JsonValue data;
        final JsonValue result;
        final boolean valid;
        final String description;

        final List<Error> errors;

        Fixture(JsonValue schema, JsonValue data, JsonValue result, boolean valid, List<Error> errors,
                String description) {
            this.schema = schema;
            this.data = data;
            this.result = result;
            this.valid = valid;
            this.errors = errors;
            this.description = description;
        }

        public String toString() {
            return description;
        }
    }

    /**
     * A error found by the validation.
     *
     * @author leadpony
     */
    private static class Error {

        final String pointer;
        final int event;

        Error(String pointer, int event) {
            this.pointer = pointer;
            this.event = event;
        }
    }

    private static final String[] FILES = {
            "default-properties.json",
            "default-properties-invalid.json",
            "default-items.json",
            "default-items-invalid.json"
    };

    public static Stream<Fixture> fixtures() {
        return Stream.of(FILES).flatMap(DefaultValueTest::readFixtures);
    }

    private static Stream<Fixture> readFixtures(String name) {
        InputStream in = DefaultValueTest.class.getResourceAsStream(name);
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray().stream()
                    .map(JsonValue::asJsonObject)
                    .flatMap(schema -> {
                        return schema.getJsonArray("tests").stream()
                                .map(JsonValue::asJsonObject)
                                .map(test -> createFixture(schema, test));
                    });
        }
    }

    private static Fixture createFixture(JsonObject schema, JsonObject test) {
        return new Fixture(
                schema.get("schema"),
                test.get("data"),
                test.get("result"),
                test.getBoolean("valid", true),
                createErrors(test),
                test.getString("description"));
    }

    private static List<Error> createErrors(JsonObject test) {
        if (test.containsKey("errors")) {
            return test.getJsonArray("errors").stream()
                    .map(JsonValue::asJsonObject)
                    .map(e -> new Error(
                            e.getString("pointer"),
                            e.getInt("event")))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    public void readValueShouldFillWithDefaultValues(Fixture fixture) {
        JsonSchema schema = readSchema(fixture.schema);
        List<Problem> problems = new ArrayList<>();
        JsonValue actual = null;
        try (JsonReader reader = createJsonReader(fixture.data, schema, problems::addAll)) {
            actual = reader.readValue();
        }

        printer.print(problems);

        assertThat(actual).isEqualTo(fixture.result);

        if (fixture.valid) {
            assertThat(problems).isEmpty();
        } else {
            assertThat(problems).isNotEmpty();
            for (int i = 0; i < problems.size(); i++) {
                Problem problem = problems.get(i);
                Error error = fixture.errors.get(i);
                assertThat(problem.getPointer()).isEqualTo(error.pointer);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    public void nextShouldGenerateAdditionalEvents(Fixture fixture) {
        JsonSchema schema = readSchema(fixture.schema);
        List<Problem> problems = new ArrayList<>();
        List<Event> expected = getExpectedEvents(fixture.result);
        List<Event> actual = new ArrayList<>();
        List<Integer> iterations = new ArrayList<>();

        try (JsonParser parser = createJsonParser(fixture.data, schema, problems::addAll)) {
            while (parser.hasNext()) {
                int size = problems.size();
                actual.add(parser.next());
                if (problems.size() > size) {
                    iterations.add(actual.size());
                }
            }
        }

        printer.print(problems);

        assertThat(actual).containsExactlyElementsOf(expected);

        if (fixture.valid) {
            assertThat(problems).isEmpty();
        } else {
            assertThat(problems).isNotEmpty();
            for (int i = 0; i < problems.size(); i++) {
                Problem problem = problems.get(i);
                Error error = fixture.errors.get(i);
                assertThat(problem.getPointer()).isEqualTo(error.pointer);
                assertThat(iterations.get(i)).isEqualTo(error.event);
            }
        }
    }

    private JsonSchema readSchema(JsonValue value) {
        StringReader reader = new StringReader(value.toString());
        return service.readSchema(reader);
    }

    private JsonParser createJsonParser(JsonValue value, JsonSchema schema, ProblemHandler handler) {
        ValidationConfig config = service.createValidationConfig()
                .withSchema(schema)
                .withProblemHandler(handler)
                .withDefaultValues(true);
        return service.createParserFactory(config.getAsMap())
                .createParser(new StringReader(value.toString()));
    }

    private JsonReader createJsonReader(JsonValue value, JsonSchema schema, ProblemHandler handler) {
        ValidationConfig config = service.createValidationConfig()
                .withSchema(schema)
                .withProblemHandler(handler)
                .withDefaultValues(true);
        return service.createReaderFactory(config.getAsMap())
                .createReader(new StringReader(value.toString()));
    }

    private List<Event> getExpectedEvents(JsonValue value) {
        List<Event> events = new ArrayList<>();
        StringReader reader = new StringReader(value.toString());
        try (JsonParser parser = Json.createParser(reader)) {
            while (parser.hasNext()) {
                events.add(parser.next());
            }
        }
        return events;
    }
}
