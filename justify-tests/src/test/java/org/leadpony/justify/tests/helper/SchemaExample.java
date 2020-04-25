/*
 * Copyright 2020 the Justify authors.
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

/**
 * JSON schema examples provided by json-schema.org.
 *
 * @author leadpony
 */
public enum SchemaExample {
    ARRAY("arrays.schema.json"),
    FSTAB("fstab.schema.json"),
    GEOGRAPHICAL_LOCATION("geographical-location.schema.json"),
    PERSON("person.schema.json"),
    PRODUCT("product.schema.json");

    static final String BASE_PACKAGE = "/org/json_schema/examples/draft7/";
    static final Path BASE_PATH = Paths.get(
            "target", "test-classes", "org", "json_schema", "examples", "draft7");

    private final String name;

    SchemaExample(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    public Path getPath() {
        return BASE_PATH.resolve(getName());
    }

    public InputStream getAsStream() {
        return getClass().getResourceAsStream(BASE_PACKAGE + getName());
    }

    public JsonValue getAsJson() {
        try (JsonReader reader = Json.createReader(getAsStream())) {
            return reader.readValue();
        }
    }
}
