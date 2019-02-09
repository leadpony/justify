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

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * @author leadpony
 */
class SchemaFixture extends Fixture {

    private final String description;
    private final JsonValue schema;
    private final boolean validity;

    private SchemaFixture(String name, int index, String description, JsonValue schema, boolean valid) {
        super(name, index);
        this.description = description;
        this.schema = schema;
        this.validity = valid;
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

    static Stream<SchemaFixture> newStream(String name) {
        AtomicInteger counter = new AtomicInteger();
        return readJsonArray(name).stream()
                .map(JsonValue::asJsonObject)
                .map(object->new SchemaFixture(
                        name,
                        counter.getAndIncrement(),
                        object.getString("description"),
                        object.get("schema"),
                        object.getBoolean("valid")
                        ));
    }

    private static JsonArray readJsonArray(String name) {
        InputStream in = ValidationFixture.class.getResourceAsStream(name);
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray();
        }
    }
}
