/*
 * Copyright 2018, 2020 the Justify authors.
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
package org.leadpony.justify.tests.extra.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
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
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.tests.helper.Loggable;
import org.leadpony.justify.tests.helper.ValidationServiceType;

/**
 * A test type for official test suite.
 *
 * @author leadpony
 */
public abstract class AbstractOfficialTest implements Loggable {

    private static final JsonValidationService SERVICE = ValidationServiceType.DEFAULT.getService();

    /**
     * A test case for official test suite.
     *
     * @author leadpony
     */
    public static class TestCase {

        private final String name;
        private final int index;
        private final JsonValue schema;
        private final JsonValue data;
        private final String description;
        private final boolean result;

        TestCase(String name,
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

        /**
         * Returns the JSON schema as a JSON value.
         *
         * @return the JSON schema.
         */
        JsonValue getSchema() {
            return schema;
        }

        /**
         * Returns the JSON instance as a JSON value.
         *
         * @return the JSON instane.
         */
        JsonValue getData() {
            return data;
        }

        /**
         * Returns whether the instance is valid or not.
         *
         * @return {@code true} if the instance is valid, {@code false} otherwise.
         */
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

    static final JsonBuilderFactory JSON_BUILDER_FACTORY = Json.createBuilderFactory(null);

    static final Path TEST_SUITE_HOME = Paths.get("..", "JSON-Schema-Test-Suite");
    static final Path TESTS_PATH = TEST_SUITE_HOME.resolve("tests");

    @SuppressWarnings("serial")
    private static final Map<SpecVersion, String> DRAFT_PATHS = new EnumMap<SpecVersion, String>(SpecVersion.class) {
        {
            put(SpecVersion.DRAFT_04, "draft4");
            put(SpecVersion.DRAFT_06, "draft6");
            put(SpecVersion.DRAFT_07, "draft7");
        }
    };

    private static SpecVersion specVersion;
    private static Path basePath;
    private static JsonSchemaReaderFactory schemaReaderFactory;

    private static JsonValue lastValue;
    private static JsonSchema lastSchema;

    @BeforeAll
    public static void setUpOnce(TestInfo testInfo) {
        Class<?> testClass = testInfo.getTestClass().get();
        Spec spec = testClass.getAnnotation(Spec.class);
        specVersion = spec.value();
        basePath = TESTS_PATH.resolve(DRAFT_PATHS.get(specVersion));
        schemaReaderFactory = SERVICE.createSchemaReaderFactoryBuilder()
                .withDefaultSpecVersion(specVersion)
                .withSchemaResolver(new LocalSchemaResolver())
                .withSchemaValidation(true)
                .build();
    }

    public static Stream<TestCase> generateFixtures(String... files) {
        return Stream.of(files).flatMap(AbstractOfficialTest::readTestCases);
    }

    /**
     * Tests with a test case.
     *
     * @param test
     */
    public void test(TestCase test) {
        JsonSchema schema = getSchema(test.getSchema());
        JsonValue data = test.getData();

        List<Problem> problems = new ArrayList<>();

        JsonParser parser = createValidator(data, schema, problems::addAll);
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();

        assertThat(problems.isEmpty()).isEqualTo(test.getResult());
        checkProblems(problems);
        printProblems(test, problems);
    }

    /**
     * Tests with a negated test case.
     *
     * @param test
     */
    public void testNegated(TestCase test) {
        JsonSchema schema = getNegatedSchema(test.getSchema());
        JsonValue data = test.getData();

        List<Problem> problems = new ArrayList<>();

        JsonParser parser = createValidator(data, schema, problems::addAll);
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();

        assertThat(problems.isEmpty()).isEqualTo(!test.getResult());
        checkProblems(problems);
        printProblems(test, problems);
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
        return SERVICE.createSchemaBuilderFactory()
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
        return SERVICE.createParser(reader, schema, handler);
    }

    private static Stream<TestCase> readTestCases(String name) {
        Function<JsonObject, Stream<TestCase>> mapper = new Function<JsonObject, Stream<TestCase>>() {
            private int index;

            @Override
            public Stream<TestCase> apply(JsonObject schema) {
                return schema.getJsonArray("tests").stream()
                        .map(JsonValue::asJsonObject)
                        .map(test -> new TestCase(
                                name,
                                index++,
                                schema.getValue("/schema"),
                                test.get("data"),
                                test.getString("description"),
                                test.getBoolean("valid")));
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

    private void printProblems(TestCase test, List<Problem> problems) {
        if (problems.isEmpty() || !LOG.isLoggable(Level.INFO)) {
            return;
        }
        StringBuilder builder = new StringBuilder("- ");
        builder.append(test.toString());

        LOG.info(builder.toString());
        printProblems(problems);
        LOG.info("");
    }

    private static InputStream openResource(String name) {
        if (name.startsWith("/")) {
            return AbstractOfficialTest.class.getResourceAsStream(name);
        } else {
            try {
                return new FileInputStream(name);
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(name, e);
            }
        }
    }

    /**
     * A schema resolver which resolves schemas for the local filesystem.
     *
     * @author leadpony
     */
    private static class LocalSchemaResolver implements JsonSchemaResolver {

        private static final Path REMOTE_PATH = TEST_SUITE_HOME.resolve("remotes");

        @Override
        public JsonSchema resolveSchema(URI id) {
            String rootless = id.getPath().substring(1);
            Path path = REMOTE_PATH.resolve(rootless);
            try {
                try (JsonSchemaReader reader = SERVICE.createSchemaReader(
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
