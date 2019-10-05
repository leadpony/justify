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

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * A test fixture class for {@link SchemaValidationTest}.
 *
 * @author leadpony
 */
final class SchemaFixture extends Fixture {

    private final String description;
    private final JsonValue schema;
    private final boolean validity;
    private final List<Error> errors;

    /**
     * A error found by the validation.
     *
     * @author leadpony
     */
    static class Error {
        private final String pointer;

        Error(String pointer) {
            this.pointer = pointer;
        }

        String pointer() {
            return pointer;
        }
    }

    private SchemaFixture(String name, int index, String description, JsonValue schema, boolean valid,
            List<Error> errors) {
        super(name, index);
        this.description = description;
        this.schema = schema;
        this.validity = valid;
        this.errors = errors;
    }

    @Override
    public String description() {
        return description;
    }

    JsonValue schema() {
        return schema;
    }

    boolean hasValidSchema() {
        return validity;
    }

    List<Error> errors() {
        return errors;
    }

    static Stream<SchemaFixture> newStream(String name) {
        AtomicInteger counter = new AtomicInteger();
        return readJsonArray(name).stream()
                .map(JsonValue::asJsonObject)
                .map(object -> toFixture(object, name, counter.getAndIncrement()));
    }

    private static SchemaFixture toFixture(JsonObject object, String name, int index) {
        List<Error> errors = Collections.emptyList();
        JsonArray array = object.getJsonArray("errors");
        if (array != null) {
            errors = array.stream()
                    .map(JsonValue::asJsonObject)
                    .map(SchemaFixture::toError)
                    .collect(Collectors.toList());
        }
        return new SchemaFixture(
                name,
                index,
                object.getString("description"),
                object.get("schema"),
                object.getBoolean("valid"),
                errors);
    }

    private static Error toError(JsonObject object) {
        return new Error(object.getString("pointer"));
    }

    private static JsonArray readJsonArray(String name) {
        InputStream in = SchemaFixture.class.getResourceAsStream(name);
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray();
        }
    }
}
