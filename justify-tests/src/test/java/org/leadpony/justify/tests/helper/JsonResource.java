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

import java.io.InputStream;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

/**
 * A JSON resource.
 *
 * @author leadpony
 */
public final class JsonResource {

    private final String name;

    public static JsonResource of(String name) {
        return new JsonResource(name);
    }

    private JsonResource(String name) {
        this.name = name;
    }

    /**
     * Generates a stream of JSON objects from this resource.
     *
     * @return a stream of JSON objects.
     */
    public Stream<JsonObject> asObjectStream() {
        InputStream in = getClass().getResourceAsStream(name);
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray().stream().map(JsonValue::asJsonObject);
        }
    }
}
