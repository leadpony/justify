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

package org.leadpony.justify.api;

import static org.assertj.core.api.Assertions.*;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.test.helper.JsonResources;

/**
 * A test class for tesing the {@link JsonSchema} implementation.
 *
 * @author leadpony
 */
public class JsonSchemaTest {

    private static final JsonValidationService service = JsonValidationServices.get();

    @Test
    public void empty_shouldReturnEmptySchema() {
        JsonSchema schema = JsonSchema.EMPTY;

        assertThat(schema).hasToString("{}");
    }

    @Test
    public void valueOf_shouldReturnTrueBooleanSchema() {
        JsonSchema schema = JsonSchema.TRUE;

        assertThat(schema).hasToString("true");
    }

    @Test
    public void valueOf_shouldReturnFalseBooleanSchema() {
        JsonSchema schema = JsonSchema.FALSE;

        assertThat(schema).hasToString("false");
    }

    @Test
    public void defaultValue_shouldReturnValueIfExists() {
        String json = "{ \"default\": 42 }";
        JsonSchema schema = fromString(json);
        JsonValue actual = schema.defaultValue();

        assertThat(actual.getValueType()).isEqualTo(JsonValue.ValueType.NUMBER);
        JsonNumber number = (JsonNumber)actual;
        assertThat(number.intValue()).isEqualTo(42);
    }

    @Test
    public void defaultValue_shouldReturnNullIfNotExists() {
        String json = "{ \"type\": \"string\" }";
        JsonSchema schema = fromString(json);
        JsonValue actual = schema.defaultValue();

        assertThat(actual).isNull();
    }

    static class ContainsKeywordTestCase {

        final JsonValue schema;
        final String keyword;
        final boolean result;

        ContainsKeywordTestCase(JsonValue schema, String keyword, boolean result) {
            this.schema = schema;
            this.keyword = keyword;
            this.result = result;
        }

        @Override
        public String toString() {
            return keyword;
        }
    }

    public static Stream<ContainsKeywordTestCase> containsKeywordShouldReturnBooleanAsExpected() {
        return JsonResources.getJsonObjectStream("/org/leadpony/justify/api/contains-keyword.json")
                .flatMap(object->{
                    JsonValue schema = object.get("schema");
                    return object.getJsonArray("tests").stream()
                        .map(JsonValue::asJsonObject)
                        .map(test->new ContainsKeywordTestCase(
                                schema,
                                test.getString("keyword"),
                                test.getBoolean("result")
                                ));
                });
    }

    @ParameterizedTest
    @MethodSource
    public void containsKeywordShouldReturnBooleanAsExpected(ContainsKeywordTestCase test) {
        JsonSchema schema = fromString(test.schema.toString());
        boolean actual = schema.containsKeyword(test.keyword);
        assertThat(actual).isEqualTo(test.result);
    }

    static class GetKeywordValueTestCase {

        final JsonValue schema;
        final String keyword;
        final JsonValue value;

        GetKeywordValueTestCase(JsonValue schema, String keyword, JsonValue value) {
            this.schema = schema;
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public String toString() {
            return schema.toString();
        }
    }

    public static Stream<GetKeywordValueTestCase> getKeywordValueShouldReturnValueAsExpected() {
        return JsonResources.getJsonObjectStream("/org/leadpony/justify/api/keyword-value.json")
                .flatMap(object->{
                    JsonValue schema = object.get("schema");
                    return object.getJsonArray("tests").stream()
                        .map(JsonValue::asJsonObject)
                        .map(test->new GetKeywordValueTestCase(
                                schema,
                                test.getString("keyword"),
                                test.get("value")
                                ));
                });
    }

    @ParameterizedTest
    @MethodSource
    public void getKeywordValueShouldReturnValueAsExpected(GetKeywordValueTestCase test) {
        JsonSchema schema = fromString(test.schema.toString());
        JsonValue actual = schema.getKeywordValue(test.keyword);
        assertThat(actual).isEqualTo(test.value);
    }

    static class GetKeywordDefaultValueTestCase {

        final JsonValue schema;
        final String keyword;
        final JsonValue defaultValue;
        final JsonValue value;

        GetKeywordDefaultValueTestCase(JsonValue schema, String keyword, JsonValue defaultValue, JsonValue value) {
            this.schema = schema;
            this.keyword = keyword;
            this.defaultValue = defaultValue;
            this.value = value;
        }

        @Override
        public String toString() {
            return schema.toString();
        }
    }

    public static Stream<GetKeywordDefaultValueTestCase> getKeywordValueShouldReturnDefaultValueAsExpected() {
        return JsonResources.getJsonObjectStream("/org/leadpony/justify/api/keyword-default-value.json")
                .flatMap(object->{
                    JsonValue schema = object.get("schema");
                    return object.getJsonArray("tests").stream()
                        .map(JsonValue::asJsonObject)
                        .map(test->new GetKeywordDefaultValueTestCase(
                                schema,
                                test.getString("keyword"),
                                test.get("defaultValue"),
                                test.get("value")
                                ));
                });
    }

    @ParameterizedTest
    @MethodSource
    public void getKeywordValueShouldReturnDefaultValueAsExpected(GetKeywordDefaultValueTestCase test) {
        JsonSchema schema = fromString(test.schema.toString());
        JsonValue actual = schema.getKeywordValue(test.keyword, test.defaultValue);
        assertThat(actual).isEqualTo(test.value);
    }

    private static final String SUBSCHEMAS_JSON = "/org/leadpony/justify/api/subschemas.json";

    private static Stream<Arguments> subschemaFixtures(String keyName) {
        InputStream in = JsonSchemaTest.class.getResourceAsStream(SUBSCHEMAS_JSON);
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray().stream()
                .map(JsonValue::asJsonObject)
                .map(object->{
                    String schema = object.get("schema").toString();
                    List<String> subschemas = object.get(keyName).asJsonArray()
                           .stream()
                           .map(v->((JsonString)v).getString())
                           .collect(Collectors.toList());
                    return Arguments.of(schema, subschemas);
               });
        }
    }

    public static Stream<Arguments> subschemasFixtures() {
        return subschemaFixtures("subschemas");
    }

    @ParameterizedTest
    @MethodSource("subschemasFixtures")
    public void getSubschemas_shouldReturnSubschemas(String json, List<String> jsonPointers) {
        JsonSchema schema = fromString(json);
        List<JsonSchema> expected = jsonPointers.stream()
                .map(schema::getSubschemaAt)
                .collect(Collectors.toList());

        Stream<JsonSchema> actual = schema.getSubschemas();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    public static Stream<Arguments> inPlaceSubschemasFixtures() {
        return subschemaFixtures("inPlaceSubschemas");
    }

    @ParameterizedTest
    @MethodSource("inPlaceSubschemasFixtures")
    public void getInPlaceSubschemas_shouldReturnSubschemas(String json, List<String> jsonPointers) {
        JsonSchema schema = fromString(json);
        List<JsonSchema> expected = jsonPointers.stream()
                .map(schema::getSubschemaAt)
                .collect(Collectors.toList());

        Stream<JsonSchema> actual = schema.getInPlaceSubschemas();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    private static JsonSchema fromString(String string) {
        return service.readSchema(new StringReader(string));
    }
}
