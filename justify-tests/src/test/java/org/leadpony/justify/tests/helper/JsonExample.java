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

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

/**
 * @author leadpony
 */
public enum JsonExample {
    ARRAY("arrays.json", SchemaExample.ARRAY, true),
    FSTAB("fstab.json", SchemaExample.FSTAB, true),
    FSTAB_INVALID("fstab-invalid.json",  SchemaExample.FSTAB, false),
    GEOGRAPHICAL_LOCATION("geographical-location.json", SchemaExample.GEOGRAPHICAL_LOCATION, true),
    PERSON("person.json", SchemaExample.PERSON, true),
    PRODUCT("product.json", SchemaExample.PRODUCT, true),
    PRODUCT_INVALID("product-invalid.json", SchemaExample.PRODUCT, false);

    private static final String BASE_PACKAGE = SchemaExample.BASE_PACKAGE;
    private static final Path BASE_PATH = SchemaExample.BASE_PATH;

    private final String jsonName;
    private final String yamlName;
    private final SchemaExample schema;
    private final boolean valid;

    JsonExample(String jsonName, SchemaExample schema, boolean valid) {
        this.jsonName = jsonName;
        this.yamlName = jsonName.replaceAll("\\.json$", ".yaml");
        this.schema = schema;
        this.valid = valid;
    }

    public String getJsonName() {
        return jsonName;
    }

    public String getYamlName() {
        return yamlName;
    }

    public boolean isValid() {
        return valid;
    }

    public Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    public Path getJsonPath() {
        return BASE_PATH.resolve(getJsonName());
    }

    public Path getYamlPath() {
        return BASE_PATH.resolve(getYamlName());
    }

    public InputStream getJsonAsStream() {
        return getResourceAsStream(getJsonName());
    }

    public InputStream getYamlAsStream() {
        return getResourceAsStream(getYamlName());
    }

    public JsonValue getAsJson() {
        try (JsonReader reader = Json.createReader(getJsonAsStream())) {
            return reader.readValue();
        }
    }

    public InputStream getJsonSchemaAsStream() {
        return schema.getJsonAsStream();
    }

    public InputStream getYamlSchemaAsStream() {
        return schema.getYamlAsStream();
    }

    private InputStream getResourceAsStream(String name) {
        return getClass().getResourceAsStream(BASE_PACKAGE + name);
    }
}
