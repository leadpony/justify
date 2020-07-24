/*
 * Copyright 2018, 2020 the Justify authors.
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
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonSchemaVisitor;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.tests.helper.JsonResource;
import org.leadpony.justify.tests.helper.JsonSource;
import org.leadpony.justify.tests.helper.ValidationServiceType;

/**
 * A test class for tesing the {@link JsonSchema} implementation.
 *
 * @author leadpony
 */
public class JsonSchemaTest {

    private static final JsonValidationService SERVICE = ValidationServiceType.DEFAULT.getService();

    private static JsonSchemaReaderFactory schemaReaderFactory;

    @BeforeAll
    public static void setUpOnce() {
        schemaReaderFactory = SERVICE.createSchemaReaderFactoryBuilder()
                .withDefaultSpecVersion(SpecVersion.DRAFT_2019_09)
                .build();
    }

    @Test
    public void defaultValueShouldReturnValueIfExists() {
        String json = "{ \"default\": 42 }";
        JsonSchema schema = fromString(json);
        JsonValue actual = schema.defaultValue();

        assertThat(actual.getValueType()).isEqualTo(JsonValue.ValueType.NUMBER);
        JsonNumber number = (JsonNumber) actual;
        assertThat(number.intValue()).isEqualTo(42);
    }

    @Test
    public void defaultValueShouldReturnNullIfNotExists() {
        String json = "{ \"type\": \"string\" }";
        JsonSchema schema = fromString(json);
        JsonValue actual = schema.defaultValue();

        assertThat(actual).isNull();
    }

    /**
     * JSON Schema samples.
     *
     * @author leadpony
     */
    enum JsonSchemaExample {
        TRUE("true", ValueType.TRUE),
        FALSE("false", ValueType.FALSE),
        SIMPLE_OBJECT("{\"type\":\"string\"}", ValueType.OBJECT),
        EMPTY_OBJECT("{}", ValueType.OBJECT);

        final String schema;
        final ValueType valueType;

        JsonSchemaExample(String schema, ValueType valueType) {
            this.schema = schema;
            this.valueType = valueType;
        }

        boolean isBoolean() {
            return valueType == ValueType.TRUE || valueType == ValueType.FALSE;
        }
    }

    @ParameterizedTest
    @EnumSource(JsonSchemaExample.class)
    public void isBooleanShouldReturnBooleanAsExpected(JsonSchemaExample test) {
        JsonSchema schema = fromString(test.schema);
        boolean actual = schema.isBoolean();
        assertThat(actual).isEqualTo(test.isBoolean());
    }

    @ParameterizedTest
    @EnumSource(JsonSchemaExample.class)
    public void getJsonValueTypeShouldReturnTypeAsExpected(JsonSchemaExample test) {
        JsonSchema schema = fromString(test.schema);
        ValueType actual = schema.getJsonValueType();
        assertThat(actual).isEqualTo(test.valueType);
    }

    @ParameterizedTest
    @EnumSource(JsonSchemaExample.class)
    public void toStringShouldReturnStringAsExpected(JsonSchemaExample test) {
        JsonSchema schema = fromString(test.schema);
        String actual = schema.toString();
        assertThat(actual).isEqualTo(test.schema);
    }

    /**
     * A test case for {@link JsonSchema#containsKeyword(String).
     *
     * @author leadpony
     */
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
        return JsonResource.of("/org/leadpony/justify/tests/api/jsonschema-containskeyword.json")
                .asObjectStream()
                .flatMap(object -> {
                    JsonValue schema = object.get("schema");
                    return object.getJsonArray("tests").stream()
                            .map(JsonValue::asJsonObject)
                            .map(test -> new ContainsKeywordTestCase(
                                    schema,
                                    test.getString("keyword"),
                                    test.getBoolean("result")));
                });
    }

    @ParameterizedTest
    @MethodSource
    public void containsKeywordShouldReturnBooleanAsExpected(ContainsKeywordTestCase test) {
        JsonSchema schema = fromString(test.schema.toString());
        boolean actual = schema.containsKeyword(test.keyword);
        assertThat(actual).isEqualTo(test.result);
    }

    /**
     * A test case for {@link JsonSchema#getKeywordValue(String).
     *
     * @author leadpony
     */
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
        return JsonResource.of("/org/leadpony/justify/tests/api/jsonschema-getkeywordvalue.json")
                .asObjectStream()
                .flatMap(object -> {
                    JsonValue schema = object.get("schema");
                    return object.getJsonArray("tests").stream()
                            .map(JsonValue::asJsonObject)
                            .map(test -> new GetKeywordValueTestCase(
                                    schema,
                                    test.getString("keyword"),
                                    test.get("value")));
                });
    }

    @ParameterizedTest
    @MethodSource
    public void getKeywordValueShouldReturnValueAsExpected(GetKeywordValueTestCase test) {
        JsonSchema schema = fromValue(test.schema);
        JsonValue actual = schema.getKeywordValue(test.keyword);
        assertThat(actual).isEqualTo(test.value);
    }

    /**
     * A test case for {@link JsonSchema#getKeywordValue(String, JsonValue).
     *
     * @author leadpony
     */
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
        return JsonResource.of("/org/leadpony/justify/tests/api/jsonschema-getkeywordvalue-default.json")
                .asObjectStream()
                .flatMap(object -> {
                    JsonValue schema = object.get("schema");
                    return object.getJsonArray("tests").stream()
                            .map(JsonValue::asJsonObject)
                            .map(test -> new GetKeywordDefaultValueTestCase(
                                    schema,
                                    test.getString("keyword"),
                                    test.get("defaultValue"),
                                    test.get("value")));
                });
    }

    @ParameterizedTest
    @MethodSource
    public void getKeywordValueShouldReturnDefaultValueAsExpected(GetKeywordDefaultValueTestCase test) {
        JsonSchema schema = fromValue(test.schema);
        JsonValue actual = schema.getKeywordValue(test.keyword, test.defaultValue);
        assertThat(actual).isEqualTo(test.value);
    }

    private static final String SUBSCHEMAS_JSON = "/org/leadpony/justify/tests/api/subschemas.json";

    private static Stream<Arguments> subschemaTestCases(String keyName) {
        InputStream in = JsonSchemaTest.class.getResourceAsStream(SUBSCHEMAS_JSON);
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray().stream()
                    .map(JsonValue::asJsonObject)
                    .map(object -> {
                        String schema = object.get("schema").toString();
                        List<String> subschemas = object.get(keyName).asJsonArray()
                                .stream()
                                .map(v -> ((JsonString) v).getString())
                                .collect(Collectors.toList());
                        return Arguments.of(schema, subschemas);
                    });
        }
    }

    public static Stream<Arguments> subschemasTestCases() {
        return subschemaTestCases("subschemas");
    }

    @ParameterizedTest
    @MethodSource("subschemasTestCases")
    public void getSubschemasShouldReturnSubschemas(String json, List<String> jsonPointers) {
        JsonSchema schema = fromString(json);
        List<JsonSchema> expected = jsonPointers.stream()
                .map(schema::findSchema)
                .map(Optional::get)
                .collect(Collectors.toList());

        Stream<JsonSchema> actual = schema.getSubschemas();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    public static Stream<Arguments> inPlaceSubschemasTestCases() {
        return subschemaTestCases("inPlaceSubschemas");
    }

    @ParameterizedTest
    @MethodSource("inPlaceSubschemasTestCases")
    public void getInPlaceSubschemasShouldReturnSubschemas(String json, List<String> jsonPointers) {
        JsonSchema schema = fromString(json);
        List<JsonSchema> expected = jsonPointers.stream()
                .map(schema::findSchema)
                .map(Optional::get)
                .collect(Collectors.toList());

        Stream<JsonSchema> actual = schema.getInPlaceSubschemas();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @ParameterizedTest
    @JsonSource("/org/jsonschema/draft201909/schema-identification.json")
    public void collectSchemasShouldReturnExpectedSchemas(JsonObject test) {
        JsonSchema schema = fromValue(test.get("schema"));
        Map<String, JsonValue> expected = test.getJsonObject("schemas");

        Map<String, JsonSchema> collected = schema.collectSchemas();

        Map<String, JsonValue> actual = collected.entrySet().stream()
            .collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> e.getValue().toJson()));

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @JsonSource("/org/jsonschema/draft201909/schema-identification.json")
    public void collectIdentifiedSchemasShouldReturnExpectedSchemas(JsonObject test) {
        JsonSchema schema = fromValue(test.get("schema"));
        Map<String, JsonValue> expected = test.getJsonObject("identified");

        Map<URI, JsonSchema> collected = schema.collectIdentifiedSchemas(URI.create("https://example.com"));

        Map<String, JsonValue> actual = collected.entrySet().stream()
            .collect(Collectors.toMap(
                    e -> e.getKey().toString(),
                    e -> e.getValue().toJson()));

        assertThat(actual).isEqualTo(expected);
    }

    private static class CollectiingJsonSchemaVisitor implements JsonSchemaVisitor {

        final Map<String, JsonValue> schemas = new HashMap<>();

        @Override
        public Result visitSchema(JsonSchema schema, String pointer) {
            this.schemas.put(pointer, schema.toJson());
            return Result.CONTINUE;
        }
    }

    @ParameterizedTest
    @JsonSource("/org/jsonschema/draft201909/schema-identification.json")
    public void walkSchemaTreeShouldVisitAllSchemas(JsonObject object) {
        JsonSchema schema = fromValue(object.get("schema"));
        Map<String, JsonValue> expected = object.getJsonObject("schemas");

        CollectiingJsonSchemaVisitor visitor = new CollectiingJsonSchemaVisitor();
        schema.walkSchemaTree(visitor);

        assertThat(visitor.schemas).isEqualTo(expected);
    }

    private abstract static class SpecialJsonSchemaTest {

        protected final JsonSchema schema;

        protected SpecialJsonSchemaTest(JsonSchema schema) {
            this.schema = schema;
        }

        @Test
        public void getKeywordsAsMapShouldReturnEmptyMap() {
            assertThat(schema.getKeywordsAsMap()).isEmpty();
        }
    }

    public static class TrueJsonSchemaTest extends SpecialJsonSchemaTest {

        public TrueJsonSchemaTest() {
            super(JsonSchema.TRUE);
        }

        @Test
        public void isBooleanShouldReturnTrue() {
            assertThat(schema.isBoolean()).isTrue();
        }

        @Test
        public void getJsonValueTypeShouldReturnTrue() {
            assertThat(schema.getJsonValueType()).isEqualTo(ValueType.TRUE);
        }

        @Test
        public void toStringShouldReturnTrue() {
            assertThat(schema.toString()).isEqualTo("true");
        }
    }

    public static class FalseJsonSchemaTest extends SpecialJsonSchemaTest {

        public FalseJsonSchemaTest() {
            super(JsonSchema.FALSE);
        }

        @Test
        public void isBooleanShouldReturnTrue() {
            assertThat(schema.isBoolean()).isTrue();
        }

        @Test
        public void getJsonValueTypeShouldReturnFalse() {
            assertThat(schema.getJsonValueType()).isEqualTo(ValueType.FALSE);
        }

        @Test
        public void toStringShouldReturnFalse() {
            assertThat(schema.toString()).isEqualTo("false");
        }
    }

    public static class EmptyJsonSchemaTest extends SpecialJsonSchemaTest {

        public EmptyJsonSchemaTest() {
            super(JsonSchema.EMPTY);
        }

        @Test
        public void isBooleanShouldReturnFalse() {
            assertThat(schema.isBoolean()).isFalse();
        }

        @Test
        public void getJsonValueTypeShouldReturnObject() {
            assertThat(schema.getJsonValueType()).isEqualTo(ValueType.OBJECT);
        }

        @Test
        public void toStringShouldReturnEmptyObject() {
            assertThat(schema.toString()).isEqualTo("{}");
        }
    }

    /* helpers */

    private static JsonSchema fromValue(JsonValue value) {
        return fromString(value.toString());
    }

    private static JsonSchema fromString(String string) {
        StringReader source = new StringReader(string);
        try (JsonSchemaReader reader = schemaReaderFactory.createSchemaReader(source)) {
            return reader.read();
        }
    }
}
