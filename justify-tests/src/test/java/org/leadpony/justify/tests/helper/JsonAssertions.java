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

package org.leadpony.justify.tests.helper;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;

import org.assertj.core.api.AbstractAssert;

/**
 * Custom assertions for JSON Processing API.
 *
 * @author leadpony
 */
public final class JsonAssertions {

    public static JsonValueAssert assertThat(JsonValue actual) {
        return new JsonValueAssert(actual);
    }

    public static JsonLocationAssert assertThat(JsonLocation actual) {
        return new JsonLocationAssert(actual);
    }

    private JsonAssertions() {
    }

    /**
     * An assertion on JSON values.
     *
     * @author leadpony
     */
    public static class JsonValueAssert extends AbstractAssert<JsonValueAssert, JsonValue> {

        public JsonValueAssert(JsonValue actual) {
            super(actual, JsonValueAssert.class);
        }

        /**
         * Verifies that the actual JSON value is equal to the expected JSON value.
         *
         * @param expected the expected JSON value.
         * @return this assertion.
         */
        public JsonValueAssert isEqualTo(JsonValue expected) {
            isNotNull();
            if (!actual.equals(expected)) {
                failWithMessage("Expected location to be <%s> but was <%s>", expected, actual);
            }
            return this;
        }

        /**
         * Verifies that the actual JSON value is equal to the expected string value.
         *
         * @param expected the expected string value.
         * @return this assertion.
         */
        public JsonValueAssert isEqualTo(String expected) {
            return isEqualTo(jsonValueFromString(expected));
        }

        private static JsonValue jsonValueFromString(String string) {
            try (JsonReader reader = Json.createReader(new StringReader(string))) {
                return reader.readValue();
            }
        }
    }

    /**
     * An assertion on JSON locations.
     *
     * @author leadpony
     */
    public static final class JsonLocationAssert extends AbstractAssert<JsonLocationAssert, JsonLocation> {

        private JsonLocationAssert(JsonLocation actual) {
            super(actual, JsonLocationAssert.class);
        }

        public JsonLocationAssert isEqualTo(JsonLocation expected) {
            isNotNull();
            if (actual.getLineNumber() != expected.getLineNumber()
                    || actual.getColumnNumber() != expected.getColumnNumber()
                    || actual.getStreamOffset() != expected.getStreamOffset()) {
                failWithMessage("Expected location to be <%s> but was <%s>", expected, actual);
            }
            return this;
        }
    }
}
