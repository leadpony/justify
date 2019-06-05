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
package org.leadpony.justify.test.helper;

import java.io.InputStream;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.leadpony.justify.api.JsonSchemaTest;

/**
 * A utility class operating on JSON resources.
 *
 * @author leadpony
 */
public final class JsonResources {

    public static Stream<JsonObject> getJsonObjectStream(String name) {
        InputStream in = JsonSchemaTest.class.getResourceAsStream(name);
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray().stream().map(JsonValue::asJsonObject);
        }
    }

    private JsonResources() {
    }
}
