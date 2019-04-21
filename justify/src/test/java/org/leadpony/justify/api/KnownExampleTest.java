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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A test class for testing the examples provided by json-schema.org.
 *
 * @author leadpony
 */
public class KnownExampleTest {

    private static final Logger log = Logger.getLogger(KnownExampleTest.class.getName());
    private static final JsonValidationService service = JsonValidationServices.get();
    private static final ProblemHandler printer = service.createProblemPrinter(log::info);

    private static final String BASE_PATH = "/org/json_schema/examples/draft7/";

    public static Stream<Arguments> fixtures() {
        return Stream.of(
                Arguments.of("arrays.schema.json", "arrays.json", true),
                Arguments.of("fstab.schema.json", "fstab.json", true),
                Arguments.of("fstab.schema.json","fstab-invalid.json", false),
                Arguments.of("geographical-location.schema.json", "geographical-location.json", true),
                Arguments.of("person.schema.json", "person.json", true),
                Arguments.of("product.schema.json", "product.json", true),
                Arguments.of("product.schema.json", "product-invalid.json", false)
                );
    }

    @ParameterizedTest()
    @MethodSource("fixtures")
    public void testWithJsonParser(String schemaName, String instanceName, boolean valid) throws IOException {
        JsonSchema schema = readSchemaFromResource(schemaName);
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        try (JsonParser parser = service.createParser(getResourceAsStream(instanceName), schema, handler)) {
            while (parser.hasNext()) {
                parser.next();
            }
        }
        if (!problems.isEmpty()) {
            printer.handleProblems(problems);
        }
        assertThat(problems.isEmpty()).isEqualTo(valid);
        assertThat(schema.toJson()).isEqualTo(readJsonFromResource(schemaName));
    }

    @ParameterizedTest()
    @MethodSource("fixtures")
    public void testWithJsonReader(String schemaName, String instanceName, boolean valid) throws IOException {
        JsonSchema schema = readSchemaFromResource(schemaName);
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        JsonValue value = null;
        try (JsonReader reader = service.createReader(getResourceAsStream(instanceName), schema, handler)) {
            value = reader.readValue();
        }
        if (!problems.isEmpty()) {
            printer.handleProblems(problems);
        }
        assertThat(value).isNotNull();
        assertThat(problems.isEmpty()).isEqualTo(valid);
        assertThat(schema.toJson()).isEqualTo(readJsonFromResource(schemaName));
    }

    @ParameterizedTest()
    @MethodSource("fixtures")
    @Disabled
    public void testWithJsonParserFromValue(String schemaName, String instanceName, boolean valid) throws IOException {
        JsonSchema schema = readSchemaFromResource(schemaName);
        JsonValue value = readJsonFromResource(instanceName);
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;

        JsonParserFactory factory = service.createParserFactory(null, schema, p->handler);
        JsonParser parser = null;
        switch (value.getValueType()) {
        case ARRAY:
            parser = factory.createParser((JsonArray)value);
            break;
        case OBJECT:
            parser = factory.createParser((JsonObject)value);
            break;
        default:
            return;
        }
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();

        if (!problems.isEmpty()) {
            printer.handleProblems(problems);
        }
        assertThat(problems.isEmpty()).isEqualTo(valid);
        assertThat(schema.toJson()).isEqualTo(readJsonFromResource(schemaName));
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
