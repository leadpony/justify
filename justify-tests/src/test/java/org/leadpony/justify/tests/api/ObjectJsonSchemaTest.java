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

import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.json.JsonValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.tests.helper.JsonResource;
import org.leadpony.justify.tests.helper.JsonSource;
import org.leadpony.justify.tests.helper.ValidationServiceType;

/**
 * A test type for testing {@link ObjectJsonSchema}.
 *
 * @author leadpony
 */
public class ObjectJsonSchemaTest {

    private static final JsonValidationService SERVICE = ValidationServiceType.DEFAULT.getService();

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
        return JsonResource.of("/org/leadpony/justify/tests/api/jsonschema-containskeyword.json")
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
    public static class KeySetTestCase {

        public JsonValue schema;
        public List<String> keys;

        int size() {
            return keys.size();
        }

        boolean isEmpty() {
            return keys.isEmpty();
        }
    }

    @ParameterizedTest
    @JsonSource("objectjsonschematest-keyset.json")
    public void keySetShouldReturnAllKeys(KeySetTestCase test) {
        ObjectJsonSchema schema = fromValue(test.schema);
        Set<String> actual = schema.keySet();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(test.keys);
    }

    @ParameterizedTest
    @JsonSource("objectjsonschematest-keyset.json")
    public void sizeShouldReturnSizeAsExpected(KeySetTestCase test) {
        ObjectJsonSchema schema = fromValue(test.schema);
        int actual = schema.size();
        assertThat(actual).isEqualTo(test.size());
    }

    @ParameterizedTest
    @JsonSource("objectjsonschematest-keyset.json")
    public void isEmptyShouldReturnBooleanAsExpected(KeySetTestCase test) {
        ObjectJsonSchema schema = fromValue(test.schema);
        boolean actual = schema.isEmpty();
        assertThat(actual).isEqualTo(test.isEmpty());
    }

    private static ObjectJsonSchema fromValue(JsonValue value) {
        return fromString(value.toString());
    }

    private static ObjectJsonSchema fromString(String string) {
        return SERVICE.readSchema(new StringReader(string)).asObjectJsonSchema();
    }
}
