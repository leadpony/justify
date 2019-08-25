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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.tests.helper.ApiTest;
import org.leadpony.justify.tests.helper.ProblemPrinter;

/**
 * A test class for testing the examples provided by json-schema.org.
 *
 * @author leadpony
 */
@ApiTest
public class KnownExampleTest /* extends BaseTest */ {

    private static JsonValidationService service;
    private static ProblemPrinter printer;

    private static final String BASE_PATH = "/org/json_schema/examples/draft7/";

    /**
     * Well known JSON schema examples.
     *
     * @author leadpony
     */
    public enum Example {
        ARRAY("arrays.schema.json", "arrays.json", true),
        FSTAB("fstab.schema.json", "fstab.json", true),
        FSTAB_INVALID("fstab.schema.json", "fstab-invalid.json", false),
        GEOGRAPHICAL_LOCATION("geographical-location.schema.json", "geographical-location.json", true),
        PERSON("person.schema.json", "person.json", true),
        PRODUCT("product.schema.json", "product.json", true),
        PRODUCT_INVALID("product.schema.json", "product-invalid.json", false);

        Example(String schema, String instance, boolean valid) {
            this.schema = schema;
            this.instance = instance;
            this.valid = valid;
        }

        final String schema;
        final String instance;
        final boolean valid;
    }

    @ParameterizedTest()
    @EnumSource(Example.class)
    public void testWithJsonParser(Example example) throws IOException {
        JsonSchema schema = readSchemaFromResource(example.schema);
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        try (JsonParser parser = service.createParser(getResourceAsStream(example.instance), schema, handler)) {
            while (parser.hasNext()) {
                parser.next();
            }
        }

        printer.print(problems);

        assertThat(problems.isEmpty()).isEqualTo(example.valid);
        assertThat(schema.toJson()).isEqualTo(readJsonFromResource(example.schema));
    }

    @ParameterizedTest()
    @EnumSource(Example.class)
    public void testWithJsonReader(Example example) throws IOException {
        JsonSchema schema = readSchemaFromResource(example.schema);
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        JsonValue value = null;
        try (JsonReader reader = service.createReader(getResourceAsStream(example.instance), schema, handler)) {
            value = reader.readValue();
        }

        printer.print(problems);

        assertThat(value).isNotNull();
        assertThat(problems.isEmpty()).isEqualTo(example.valid);
        assertThat(schema.toJson()).isEqualTo(readJsonFromResource(example.schema));
    }

    @ParameterizedTest()
    @EnumSource(Example.class)
    public void testWithJsonParserFromValue(Example example) throws IOException {
        JsonSchema schema = readSchemaFromResource(example.schema);
        JsonValue value = readJsonFromResource(example.instance);
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;

        JsonParserFactory factory = service.createParserFactory(null, schema, p -> handler);
        JsonParser parser = null;
        switch (value.getValueType()) {
        case ARRAY:
            parser = factory.createParser((JsonArray) value);
            break;
        case OBJECT:
            parser = factory.createParser((JsonObject) value);
            break;
        default:
            return;
        }
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();

        printer.print(problems);

        assertThat(problems.isEmpty()).isEqualTo(example.valid);
        assertThat(schema.toJson()).isEqualTo(readJsonFromResource(example.schema));
    }

    private JsonSchema readSchemaFromResource(String name) throws IOException {
        try (InputStream in = getResourceAsStream(name)) {
            return service.readSchema(in);
        }
    }

    private JsonValue readJsonFromResource(String name) throws IOException {
        try (JsonReader reader = Json.createReader(getResourceAsStream(name))) {
            return reader.readValue();
        }
    }

    private InputStream getResourceAsStream(String name) {
        return getClass().getResourceAsStream(BASE_PATH + name);
    }
}
