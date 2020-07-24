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
import java.util.stream.Stream;

import jakarta.json.JsonValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.tests.helper.JsonResource;
import org.leadpony.justify.tests.helper.ValidationServiceType;

/**
 * @author leadpony
 */
public class KeywordTest {

    private static final JsonValidationService SERVICE = ValidationServiceType.DEFAULT.getService();

    /**
     * @author leadpony
     */
    static class KeywordTestCase {

        final JsonValue schema;
        final String keyword;
        final JsonValue value;

        KeywordTestCase(JsonValue schema, String keyword, JsonValue value) {
            this.schema = schema;
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public String toString() {
            return schema.toString();
        }
    }

    public static Stream<KeywordTestCase> provideKeywordTestCases() {
        return JsonResource.of("/org/leadpony/justify/tests/api/jsonschema-getkeywordvalue.json")
                .asObjectStream()
                .flatMap(object -> {
                    JsonValue schema = object.get("schema");
                    return object.getJsonArray("tests").stream()
                            .map(JsonValue::asJsonObject)
                            .map(test -> new KeywordTestCase(
                                    schema,
                                    test.getString("keyword"),
                                    test.get("value")));
                });
    }

    @ParameterizedTest
    @MethodSource("provideKeywordTestCases")
    public void nameShouldReturnKeywordName(KeywordTestCase test) {
        JsonSchema schema = fromValue(test.schema);
        Keyword keyword = schema.getKeywordsAsMap().get(test.keyword);
        if (test.value != null) {
            String actual = keyword.name();
            assertThat(actual).isEqualTo(test.keyword);
        } else {
            assertThat(keyword).isNull();
        }
    }

    @ParameterizedTest
    @MethodSource("provideKeywordTestCases")
    public void getValueAsJsonShouldReturnExpectedValue(KeywordTestCase test) {
        JsonSchema schema = fromValue(test.schema);
        Keyword keyword = schema.getKeywordsAsMap().get(test.keyword);
        if (test.value != null) {
            JsonValue actual = keyword.getValueAsJson();
            assertThat(actual).isEqualTo(test.value);
        } else {
            assertThat(keyword).isNull();
        }
    }

    private static JsonSchema fromValue(JsonValue value) {
        String string = value.toString();
        return SERVICE.readSchema(new StringReader(string));
    }
}
