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
import javax.json.JsonReader;
import javax.json.JsonValue;

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

    public static Stream<Arguments> fixtures() {
        return Stream.of(
                Arguments.of("/org/json_schema/examples/arrays.json", true),
                Arguments.of("/org/json_schema/examples/fstab.json", true),
                Arguments.of("/org/json_schema/examples/fstab-invalid.json", false),
                Arguments.of("/org/json_schema/examples/geographical-location.json", true),
                Arguments.of("/org/json_schema/examples/person.json", true),
                Arguments.of("/org/json_schema/examples/product.json", true),
                Arguments.of("/org/json_schema/examples/product-invalid.json", false)
                );
    }

    @ParameterizedTest()
    @MethodSource("fixtures")
    public void testExample(String instanceName, boolean valid) throws IOException {
        String schemaName = getSchemaNameFor(instanceName);
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

    private static String getSchemaNameFor(String instanceName) {
        int lastSlash = instanceName.lastIndexOf('/');
        String filename = instanceName.substring(lastSlash + 1);
        int lastDot = filename.lastIndexOf('.');
        String simpleName = filename.substring(0, lastDot);
        if (simpleName.endsWith("-invalid")) {
            simpleName = simpleName.substring(0, simpleName.length() - 8);
        }
        String parentName = instanceName.substring(0, lastSlash + 1);
        return parentName + simpleName + ".schema.json";
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
        return getClass().getResourceAsStream(name);
    }
}
