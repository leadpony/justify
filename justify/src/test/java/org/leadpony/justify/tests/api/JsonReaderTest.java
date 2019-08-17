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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/**
 * A test class for testing validations using {@link JsonReader} .
 *
 * @author leadpony
 */
public class JsonReaderTest extends BaseTest {

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

    private static JsonReader newReader(String instance) {
        return Json.createReader(new StringReader(instance));
    }

    private static JsonReader newReader(String instance, String schema, ProblemHandler handler) {
        JsonSchema s = SERVICE.readSchema(new StringReader(schema));
        return SERVICE.createReader(new StringReader(instance), s, handler);
    }

    @Test
    public void readShouldReadArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";

        JsonReader reader = newReader(instance);
        JsonStructure expected = reader.read();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(instance, schema, problems::addAll);
        JsonStructure actual = sut.read();
        sut.close();

        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void readShouldReadObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        JsonReader reader = newReader(instance);
        JsonStructure expected = reader.read();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(instance, schema, problems::addAll);
        JsonStructure actual = sut.read();
        sut.close();

        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void readArrayShouldReadArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";

        JsonReader reader = newReader(instance);
        JsonArray expected = reader.readArray();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(instance, schema, problems::addAll);
        JsonArray actual = sut.readArray();
        sut.close();

        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void readObjectShouldReadObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        JsonReader reader = newReader(instance);
        JsonObject expected = reader.readObject();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(instance, schema, problems::addAll);
        JsonObject actual = sut.readObject();
        sut.close();

        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    public static Stream<Arguments> argumentsForReadValue() {
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
    @MethodSource("argumentsForReadValue")
    public void readValueShouldReadValue(String schema, String data, boolean valid) {
        JsonReader reader = newReader(data);
        JsonValue expected = reader.readValue();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(data, schema, problems::addAll);
        JsonValue actual = sut.readValue();
        sut.close();

        assertThat(actual).isEqualTo(expected);
        assertThat(problems.isEmpty()).isEqualTo(valid);

        print(problems);
    }

    @ParameterizedTest
    @MethodSource("argumentsForReadValue")
    public void readValueShouldThrowExceptionIfInvalid(String schema, String data, boolean valid) {
        JsonReader reader = newReader(data);
        JsonValue expected = reader.readValue();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(data, schema, ProblemHandler.throwing());
        JsonValue actual = null;
        try {
            actual = sut.readValue();
        } catch (JsonValidatingException e) {
            problems.addAll(e.getProblems());
        }
        sut.close();

        if (actual != null) {
            assertThat(actual).isEqualTo(expected);
            assertThat(problems).isEmpty();
        } else {
            assertThat(problems).isNotEmpty();
        }

        print(problems);
    }
}
