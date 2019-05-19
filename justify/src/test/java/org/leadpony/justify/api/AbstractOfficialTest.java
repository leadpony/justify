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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.leadpony.justify.internal.annotation.Spec;

/**
 * A test type for official test suite.
 *
 * @author leadpony
 */
public abstract class AbstractOfficialTest {

    /**
     * A test fixture for official test suite.
     *
     * @author leadpony
     */
    public static class Fixture {

        private final String name;
        private final int index;
        private final JsonValue schema;
        private final JsonValue data;
        private final String description;
        private final boolean result;

        Fixture(String name,
                int index,
                JsonValue schema,
                JsonValue data,
                String description,
                boolean result) {
            this.name = name;
            this.index = index;
            this.schema = schema;
            this.data = data;
            this.description = description;
            this.result = result;
        }

        JsonValue getSchema() {
            return schema;
        }

        JsonValue getData() {
            return data;
        }

        boolean getResult() {
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            int beginIndex = name.lastIndexOf('/') + 1;
            int endIndex = name.lastIndexOf('.');
            builder.append(name.substring(beginIndex, endIndex))
                   .append("[").append(index).append("]")
                   .append(" ").append(description);
            return builder.toString();
        }
    }

    static final Logger log = Logger.getLogger(AbstractOfficialTest.class.getName());

    static final JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(null);
    static final JsonValidationService service = JsonValidationServices.get();
    static final ProblemHandler printer = service.createProblemPrinter(log::info);

    static final Path TEST_SUITE_HOME = Paths.get("..", "JSON-Schema-Test-Suite");
    static final Path TESTS_PATH = TEST_SUITE_HOME.resolve("tests");

    @SuppressWarnings("serial")
    private static final Map<SpecVersion, String> DRAFT_PATHS = new EnumMap<SpecVersion, String>(SpecVersion.class) {{
        put(SpecVersion.DRAFT_04, "draft4");
        put(SpecVersion.DRAFT_06, "draft6");
        put(SpecVersion.DRAFT_07, "draft7");
    }};

    private static SpecVersion specVersion;
    private static Path basePath;
    private static JsonSchemaReaderFactory schemaReaderFactory;

    private static JsonValue lastValue;
    private static JsonSchema lastSchema;

    @BeforeAll
    public static void setUpOnce(TestInfo testInfo) {
        Class<?> testClass = testInfo.getTestClass().get();
        Spec spec = testClass.getAnnotation(Spec.class);
        specVersion = spec.value()[0];
        basePath = TESTS_PATH.resolve(DRAFT_PATHS.get(specVersion));
        schemaReaderFactory = service.createSchemaReaderFactoryBuilder()
                .withDefaultSpecVersion(specVersion)
                .withSchemaResolver(new LocalSchemaResolver())
                .withSchemaValidation(false)
                .build();
    }

    public static Stream<Fixture> generateFixtures(String... files) {
        return Stream.of(files).flatMap(AbstractOfficialTest::readFixtures);
    }

    public void test(Fixture fixture) {
        JsonSchema schema = getSchema(fixture.getSchema());
        JsonValue data = fixture.getData();

        List<Problem> problems = new ArrayList<>();

        JsonParser parser = createValidator(data, schema, problems::addAll);
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();

        assertThat(problems.isEmpty()).isEqualTo(fixture.getResult());
        checkProblems(problems);
        printProblems(fixture, problems);
    }

    public void testNegated(Fixture fixture) {
        JsonSchema schema = getNegatedSchema(fixture.getSchema());
        JsonValue data = fixture.getData();

        List<Problem> problems = new ArrayList<>();

        JsonParser parser = createValidator(data, schema, problems::addAll);
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();

        assertThat(problems.isEmpty()).isEqualTo(!fixture.getResult());
        checkProblems(problems);
        printProblems(fixture, problems);
    }

    private JsonSchema getSchema(JsonValue value) {
        if (value == lastValue) {
            return lastSchema;
        }
        JsonSchema schema = readSchema(value);
        lastValue = value;
        lastSchema = schema;
        return schema;
     }

    private JsonSchema getNegatedSchema(JsonValue value) {
        JsonSchema schema = getSchema(value);
        return service.createSchemaBuilderFactory()
                .createBuilder()
                .withNot(schema)
                .build();
    }

    private JsonSchema readSchema(JsonValue value) {
        StringReader reader = new StringReader(value.toString());
        try (JsonSchemaReader schemaReader = createSchemaReader(reader)) {
            return schemaReader.read();
        }
    }

    private JsonSchemaReader createSchemaReader(Reader reader) {
        return schemaReaderFactory.createSchemaReader(reader);
    }

    private JsonParser createValidator(JsonValue data, JsonSchema schema, ProblemHandler handler) {
        StringReader reader = new StringReader(data.toString());
        return service.createParser(reader, schema, handler);
    }

    private static Stream<Fixture> readFixtures(String name) {
        Function<JsonObject, Stream<Fixture>> mapper = new Function<JsonObject, Stream<Fixture>>() {
            int index;
            @Override
            public Stream<Fixture> apply(JsonObject schema) {
                return schema.getJsonArray("tests").stream()
                        .map(JsonValue::asJsonObject)
                        .map(test->new Fixture(
                                name,
                                index++,
                                schema.getValue("/schema"),
                                test.get("data"),
                                test.getString("description"),
                                test.getBoolean("valid")
                                ));
            }
        };
        return readJsonArray(name).stream()
                .map(JsonValue::asJsonObject)
                .flatMap(mapper);
    }

    private static JsonArray readJsonArray(String name) {
        name = resolveResource(name);
        try (JsonReader reader = Json.createReader(openResource(name))) {
            return reader.readArray();
        }
    }

    private static String resolveResource(String name) {
        if (name.startsWith("/")) {
            return name;
        } else {
            return basePath.resolve(name).toString();
        }
    }

    private static void checkProblems(List<Problem> problems) {
        for (Problem problem : problems) {
            assertThat(problem.getSchema()).isNotNull();
            if (problem.hasBranches()) {
                int index = 0;
                while (index < problem.countBranches()) {
                    checkProblems(problem.getBranch(index++));
                }
            } else {
                assertThat(problem.getLocation()).isNotNull();
                assertThat(problem.getPointer()).isNotNull();
            }
        }
    }

    private void printProblems(Fixture fixture, List<Problem> problems) {
        if (problems.isEmpty() || !log.isLoggable(Level.INFO)) {
            return;
        }
        StringBuilder builder = new StringBuilder("- ");
        builder.append(fixture.toString());
        log.info(builder.toString());
        printer.handleProblems(problems);
        log.info("");
    }

    private static InputStream openResource(String name) {
        if (name.startsWith("/")) {
            return AbstractOfficialTest.class.getResourceAsStream(name);
        } else {
            try {
                return new FileInputStream(name);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
    }

    private static class LocalSchemaResolver implements JsonSchemaResolver {

        private static final Path REMOTE_PATH = TEST_SUITE_HOME.resolve("remotes");

        @Override
        public JsonSchema resolveSchema(URI id) {
            String rootless = id.getPath().substring(1);
            Path path = REMOTE_PATH.resolve(rootless);
            try {
                try (JsonSchemaReader reader = service.createSchemaReader(
                        Files.newInputStream(path))) {
                    return reader.read();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
