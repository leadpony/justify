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
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.tests.helper.ApiTest;
import org.leadpony.justify.tests.helper.JsonAssertions;
import org.leadpony.justify.tests.helper.ProblemPrinter;

/**
 * A test class for testing validations using {@link JsonParser} .
 *
 * @author leadpony
 */
@ApiTest
public class JsonParserTest {

    private static Logger log;
    private static JsonValidationService service;
    private static ProblemPrinter printer;

    private static final String PERSON_SCHEMA = "{"
            + "\"type\":\"object\","
            + "\"properties\":{"
            + "\"name\": {\"type\":\"string\"},"
            + "\"age\": {\"type\":\"integer\", \"minimum\":0}"
            + "},"
            + "\"required\":[\"name\"]"
            + "}";

    private static final String INTEGER_ARRAY_SCHEMA = "{"
            + "\"type\":\"array\","
            + "\"items\":{\"type\":\"integer\"}"
            + "}";

    private static JsonParser newParser(String instance) {
        return Json.createParser(new StringReader(instance));
    }

    private static JsonParser newParser(String instance, String schema, ProblemHandler handler) {
        JsonSchema s = service.readSchema(new StringReader(schema));
        return service.createParser(new StringReader(instance), s, handler);
    }

    @Test
    public void hasNextShouldReturnTrueAtFirst() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        JsonParser sut = newParser(instance, schema, ProblemHandler.throwing());
        boolean actual = sut.hasNext();
        sut.close();

        assertThat(actual).isTrue();
    }

    @Test
    public void hasNextShouldReturnFalseAtLast() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        JsonParser sut = newParser(instance, schema, ProblemHandler.throwing());
        sut.next();
        boolean actual = sut.hasNext();
        sut.close();

        assertThat(actual).isFalse();
    }

    @Test
    public void nextShouldReturnAllEvents() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        JsonParser parser = newParser(instance);
        List<JsonParser.Event> expected = new ArrayList<>();
        while (parser.hasNext()) {
            expected.add(parser.next());
        }
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        List<JsonParser.Event> actual = new ArrayList<>();
        while (sut.hasNext()) {
            actual.add(sut.next());
        }
        sut.close();

        assertThat(actual).containsExactlyElementsOf(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void nextShouldReturnAllEventsEventIfInvalid() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": \"young\"}";

        JsonParser parser = newParser(instance);
        List<JsonParser.Event> expected = new ArrayList<>();
        while (parser.hasNext()) {
            expected.add(parser.next());
        }
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        List<JsonParser.Event> actual = new ArrayList<>();
        while (sut.hasNext()) {
            actual.add(sut.next());
        }
        sut.close();

        assertThat(actual).containsExactlyElementsOf(expected);
        assertThat(problems).isNotEmpty();
    }

    @Test
    public void nextShouldThrowExceptionIfInvalid() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":46, \"age\": \"John Smith\"}";

        JsonParser parser = newParser(instance);
        List<JsonParser.Event> expected = new ArrayList<>();
        while (parser.hasNext()) {
            expected.add(parser.next());
        }
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, ProblemHandler.throwing());
        List<JsonParser.Event> actual = new ArrayList<>();
        while (sut.hasNext()) {
            try {
                JsonParser.Event event = sut.next();
                actual.add(event);
            } catch (JsonValidatingException e) {
                problems.addAll(e.getProblems());
                break;
            }
        }
        sut.close();

        assertThat(problems).isNotEmpty();
    }

    @Test
    public void nextShouldThrowExceptionIfDoesNotHaveNext() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Throwable thrown = catchThrowable(() -> sut.next());
        sut.close();

        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getLocationShouldReturnLocation() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";

        JsonParser parser = newParser(instance);
        parser.next();
        JsonLocation expected = parser.getLocation();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        JsonLocation actual = sut.getLocation();
        sut.close();

        JsonAssertions.assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getStringShouldReturnString() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        String actual = sut.getString();
        sut.close();

        assertThat(actual).isEqualTo("foo");
        assertThat(problems).isEmpty();
    }

    @Test
    public void getIntShouldRturnInteger() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        int actual = sut.getInt();
        sut.close();

        assertThat(actual).isEqualTo(42);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getLongShouldReturnLong() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "9223372036854775807";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        long actual = sut.getLong();
        sut.close();

        assertThat(actual).isEqualTo(9223372036854775807L);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getBigDecimalShouldReturnBigDecimal() {
        String schema = "{\"type\":\"number\"}";
        String instance = "12.34";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        BigDecimal actual = sut.getBigDecimal();
        sut.close();

        assertThat(actual).isEqualTo(new BigDecimal("12.34"));
        assertThat(problems).isEmpty();
    }

    @Test
    public void getArrayShouldReturnArray() {
        String schema = INTEGER_ARRAY_SCHEMA;
        String instance = "[1,2,3]";

        JsonParser parser = newParser(instance);
        parser.next();
        JsonArray expected = parser.getArray();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        JsonArray actual = sut.getArray();
        sut.close();

        JsonAssertions.assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getArrayShouldThrowExceptionIfNotArray() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Throwable thrown = catchThrowable(() -> sut.getArray());
        sut.close();

        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertThat(problems).isEmpty();

        log.info(thrown.getMessage());
    }

    @Test
    public void getArrayShouldThrowExceptionIfNotClosed() {
        String schema = INTEGER_ARRAY_SCHEMA;
        String instance = "[1,2,3";

        JsonParser parser = newParser(instance);
        parser.next();
        Throwable expected = catchThrowable(() -> parser.getArray());
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Throwable actual = catchThrowable(() -> sut.getArray());
        sut.close();

        assertThat(actual).hasSameClassAs(expected);
        assertThat(actual.getMessage()).isNotEmpty();
    }

    @Test
    public void getObjectShouldReturnObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        JsonParser parser = newParser(instance);
        parser.next();
        JsonObject expected = parser.getObject();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        JsonObject actual = sut.getObject();
        sut.close();

        JsonAssertions.assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getObjectShouldThrowExceptionIfNotObject() {
        String schema = INTEGER_ARRAY_SCHEMA;
        String instance = "[1,2,3]";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Throwable thrown = catchThrowable(() -> sut.getObject());
        sut.close();

        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertThat(problems).isEmpty();

        log.info(thrown.getMessage());
    }

    @Test
    public void getObjectShouldThrowExceptionIfNotClosed() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46";

        JsonParser parser = newParser(instance);
        parser.next();
        Throwable expected = catchThrowable(() -> parser.getObject());
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Throwable actual = catchThrowable(() -> sut.getObject());
        sut.close();

        assertThat(actual).hasSameClassAs(expected);
        assertThat(actual.getMessage()).isNotEmpty();
    }

    @Test
    public void skipArrayShouldSkipArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        sut.skipArray();

        assertThat(sut.hasNext()).isFalse();
        assertThat(problems).isEmpty();

        sut.close();
    }

    @Test
    public void skipArrayShouldSkipArrayNotClosed() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        try {
            sut.skipArray();
        } catch (JsonParsingException e) {
        }

        assertThat(problems).isEmpty();

        sut.close();
    }

    @Test
    public void skipObjectShouldSkipObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        sut.skipObject();

        assertThat(sut.hasNext()).isFalse();
        assertThat(problems).isEmpty();

        sut.close();
    }

    @Test
    public void skipObjectShouldSkipObjectNotClosed() {
        String schema = "{\"type\":\"object\"}";
        String instance = "{";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        try {
            sut.skipObject();
        } catch (JsonParsingException e) {
        }

        assertThat(problems).isEmpty();

        sut.close();
    }

    @Test
    public void getArrayStreamShouldReturnStream() {
        String schema = "{ \"type\":\"array\" }";
        String instance = "[ true, false, null ]";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Stream<JsonValue> actual = sut.getArrayStream();

        assertThat(actual).containsExactly(JsonValue.TRUE, JsonValue.FALSE, JsonValue.NULL);
        assertThat(problems).isEmpty();
        sut.close();
    }

    @Test
    public void getArrayStreamShouldReturnStreamReporingProblem() {
        String schema = "{ \"minItems\": 4 }";
        // invalid
        String instance = "[ true, false, null ]";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Stream<JsonValue> actual = sut.getArrayStream();

        assertThat(actual).containsExactly(JsonValue.TRUE, JsonValue.FALSE, JsonValue.NULL);
        assertThat(problems).isNotEmpty();
        sut.close();
    }

    /**
     * This test will fail when using YAML parser.
     */
    @Test
    public void getArrayStreamShouldReturnStreamThrowingException() {
        String schema = "true";
        // ill-formed as a JSON document
        String instance = "[ \"key\" : 123 ]";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Stream<JsonValue> stream = sut.getArrayStream();
        Throwable thrown = catchThrowable(() -> {
            stream.forEach(entry -> {
            });
        });
        sut.close();

        assertThat(thrown).isInstanceOf(JsonParsingException.class);
    }

    @Test
    public void getArrayStreamShouldThrowIllegalStateException() {
        String schema = "true";
        String instance = "[ 1, 2, 3 ]";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        sut.next();
        Throwable thrown = catchThrowable(() -> {
            sut.getArrayStream();
        });
        sut.close();

        assertThat(thrown).isInstanceOf(IllegalStateException.class);

        log.info(thrown.getMessage());
    }

    @Test
    public void getObjectStreamShouldReturnStream() {
        String schema = "{ \"type\": \"object\" }";
        String instance = "{ \"key\": 123 }";

        JsonParser parser = newParser(instance);
        parser.next();
        Stream<Map.Entry<String, JsonValue>> expected = parser.getObjectStream();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Stream<Map.Entry<String, JsonValue>> actual = sut.getObjectStream();

        assertThat(actual).containsExactlyElementsOf(
                expected.collect(Collectors.toList()));
        assertThat(problems).isEmpty();

        parser.close();
        sut.close();
    }

    @Test
    public void getObjectStreamShouldReturnStreamReportingProblems() {
        String schema = "{ \"required\": [\"bar\"] }";
        // invalid
        String instance = "{ \"foo\" : 123 }";

        JsonParser parser = newParser(instance);
        parser.next();
        Stream<Map.Entry<String, JsonValue>> expected = parser.getObjectStream();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Stream<Map.Entry<String, JsonValue>> actual = sut.getObjectStream();

        assertThat(actual).containsExactlyElementsOf(
                expected.collect(Collectors.toList()));
        assertThat(problems).isNotEmpty();

        parser.close();
        sut.close();
    }

    /**
     * This test will fail when using YAML parser.
     */
    @Test
    public void getObjectStreamShouldReturnStreamThrowingException() {
        String schema = "true";
        // ill-formed as a JSON document
        String instance = "{ \"key\", 123 }";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Stream<Map.Entry<String, JsonValue>> stream = sut.getObjectStream();
        Throwable thrown = catchThrowable(() -> {
            stream.forEach(entry -> {
            });
        });
        sut.close();

        assertThat(thrown).isInstanceOf(JsonParsingException.class);
    }

    @Test
    public void getObjectStreamShouldThrowIllegalStateException() {
        String schema = "true";
        String instance = "{ \"key\": 123 }";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        sut.next();
        Throwable thrown = catchThrowable(() -> {
            sut.getObjectStream();
        });
        sut.close();

        assertThat(thrown).isInstanceOf(IllegalStateException.class);

        log.info(thrown.getMessage());
    }

    @Test
    public void getValueStreamShouldReturnValueStream() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        Stream<JsonValue> actual = sut.getValueStream();

        assertThat(actual).containsExactly(Json.createValue(42));
        assertThat(problems).isEmpty();
        sut.close();
    }

    @Test
    public void getValueStreamShouldThrowIllegalStateException() {
        String schema = "true";
        String instance = "{ \"key\": 123 }";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        Throwable thrown = catchThrowable(() -> {
            sut.getValueStream();
        });
        sut.close();

        assertThat(thrown).isInstanceOf(IllegalStateException.class);

        log.info(thrown.getMessage());
    }

    public static Stream<Arguments> argumentsForGetValue() {
        return Stream.of(
                Arguments.of("{\"type\":\"boolean\"}", "true", true),
                Arguments.of("{\"type\":\"string\"}", "true", false),
                Arguments.of("{\"type\":\"boolean\"}", "false", true),
                Arguments.of("{\"type\":\"string\"}", "false", false),
                Arguments.of("{\"type\":\"null\"}", "null", true),
                Arguments.of("{\"type\":\"string\"}", "null", false),
                Arguments.of("{\"type\":\"string\"}", "\"foo\"", true),
                Arguments.of("{\"type\":\"integer\"}", "\"foo\"", false),
                Arguments.of("{\"type\":\"integer\"}", "42", true),
                Arguments.of("{\"type\":\"integer\"}", "9223372036854775807", true),
                Arguments.of("{\"type\":\"string\"}", "42", false),
                Arguments.of("{\"type\":\"number\"}", "3.14", true),
                Arguments.of("{\"type\":\"string\"}", "3.14", false),
                Arguments.of(INTEGER_ARRAY_SCHEMA, "[1,2,3]", true),
                Arguments.of(INTEGER_ARRAY_SCHEMA, "[\"foo\",\"bar\"]", false),
                Arguments.of(PERSON_SCHEMA, "{\"name\":\"John Smith\", \"age\": 46}", true),
                Arguments.of(PERSON_SCHEMA, "{\"name\":\"John Smith\", \"age\": \"46\"}", false));
    }

    @ParameterizedTest
    @MethodSource("argumentsForGetValue")
    public void getValueShouldReturnsValue(String schema, String data, boolean valid) {
        JsonParser parser = newParser(data);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(data, schema, problems::addAll);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();

        assertThat(actual).isEqualTo(expected);
        assertThat(problems.isEmpty()).isEqualTo(valid);
        printer.print(problems);
    }

    @Test
    public void getValueShouldThrowIllegalStateException() {
        String schema = "true";
        String instance = "{}";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::addAll);
        sut.next();
        sut.next();
        Throwable thrown = catchThrowable(() -> sut.getValue());
        sut.close();

        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertThat(problems).isEmpty();

        log.info(thrown.getMessage());
    }
}
