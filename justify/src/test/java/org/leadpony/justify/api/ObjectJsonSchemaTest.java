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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonString;
import javax.json.JsonValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.test.helper.JsonResource;

/**
 * A test type for testing {@link ObjectJsonSchema}.
 *
 * @author leadpony
 */
public class ObjectJsonSchemaTest extends BaseTest {

    /**
     * A test case for {@link ObjectJsonSchema#get(Object)}.
     *
     * @author leadpony
     */
    static class GetTestCase {

        final JsonValue schema;
        final String keyword;
        final boolean result;

        GetTestCase(JsonValue schema, String keyword, boolean result) {
            this.schema = schema;
            this.keyword = keyword;
            this.result = result;
        }
    }

    public static Stream<GetTestCase> getShouldReturnKeywordIfExists() {
        return JsonResource.of("/org/leadpony/justify/api/jsonschema-containskeyword.json")
                .asObjectStream()
                .flatMap(object -> {
                    JsonValue schema = object.get("schema");
                    return object.getJsonArray("tests").stream()
                            .map(JsonValue::asJsonObject)
                            .map(test -> new GetTestCase(
                                    schema,
                                    test.getString("keyword"),
                                    test.getBoolean("result")));
                });
    }

    @ParameterizedTest
    @MethodSource
    public void getShouldReturnKeywordIfExists(GetTestCase test) {
        ObjectJsonSchema schema = fromValue(test.schema);
        Keyword keyword = schema.get(test.keyword);
        if (test.result) {
            assertThat(keyword).isNotNull();
            assertThat(keyword.name()).isEqualTo(test.keyword);
        } else {
            assertThat(keyword).isNull();
        }
    }

    @ParameterizedTest
    @MethodSource("getShouldReturnKeywordIfExists")
    public void containsKeyShouldReturnBooleanAsExpected(GetTestCase test) {
        ObjectJsonSchema schema = fromValue(test.schema);
        boolean actual = schema.containsKey(test.keyword);
        assertThat(actual).isEqualTo(test.result);
    }

    /**
     * A test case for {@link ObjectJsonSchema#keySet()}.
     *
     * @author leadpony
     */
    static class KeySetTestCase {

        final JsonValue schema;
        final List<String> keys;
        final int size;
        final boolean empty;

        KeySetTestCase(JsonValue schema, List<String> keys) {
            this.schema = schema;
            this.keys = keys;
            this.size = keys.size();
            this.empty = keys.isEmpty();
        }
    }

    public static Stream<KeySetTestCase> keySetShouldReturnAllKeys() {
        return JsonResource.of("/org/leadpony/justify/api/objectjsonschema-keyset.json")
                .asObjectStream()
                .map(object -> new KeySetTestCase(
                        object.get("schema"),
                        object.getJsonArray("keys").stream()
                                .map(v -> (JsonString) v)
                                .map(JsonString::getString)
                                .collect(Collectors.toList())));
    }

    @ParameterizedTest
    @MethodSource
    public void keySetShouldReturnAllKeys(KeySetTestCase test) {
        ObjectJsonSchema schema = fromValue(test.schema);
        Set<String> actual = schema.keySet();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(test.keys);
    }

    @ParameterizedTest
    @MethodSource("keySetShouldReturnAllKeys")
    public void sizeShouldReturnSizeAsExpected(KeySetTestCase test) {
        ObjectJsonSchema schema = fromValue(test.schema);
        int actual = schema.size();
        assertThat(actual).isEqualTo(test.size);
    }

    @ParameterizedTest
    @MethodSource("keySetShouldReturnAllKeys")
    public void isEmptyShouldReturnBooleanAsExpected(KeySetTestCase test) {
        ObjectJsonSchema schema = fromValue(test.schema);
        boolean actual = schema.isEmpty();
        assertThat(actual).isEqualTo(test.empty);
    }

    private static ObjectJsonSchema fromValue(JsonValue value) {
        return fromString(value.toString());
    }

    private static ObjectJsonSchema fromString(String string) {
        return SERVICE.readSchema(new StringReader(string)).asObjectJsonSchema();
    }
}
