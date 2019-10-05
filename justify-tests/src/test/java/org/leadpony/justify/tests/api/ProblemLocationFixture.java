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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * A test fixture class for {@link ProblemLocationTest}.
 *
 * @author leadpony
 */
final class ProblemLocationFixture {

    private final String name;
    private final String schema;
    private final String instance;
    private final List<Problem> problems;

    private ProblemLocationFixture(String name, String schema, String data, List<Problem> problems) {
        this.name = name;
        this.schema = schema;
        this.instance = data;
        this.problems = problems;
    }

    /**
     * Returns the JSON schema for this fixture.
     *
     * @return the JSON schema.
     */
    String schema() {
        return schema;
    }

    /**
     * Returns the JSON instance for this fixture.
     *
     * @return the JSON instance.
     */
    String instance() {
        return instance;
    }

    /**
     * Returns the problems to be found.
     *
     * @return the problems to be found.
     */
    List<Problem> problems() {
        return problems;
    }

    /**
     * Returns the title of this fixture.
     *
     * @return the title of this fixture.
     */
    @Override
    public String toString() {
        int beginIndex = name.lastIndexOf('/') + 1;
        int endIndex = name.lastIndexOf('.');
        return name.substring(beginIndex, endIndex);
    }

    /**
     * Reads a fixture from the specified resource file.
     *
     * @param name the name of the resource file.
     * @return the read fixture.
     */
    static ProblemLocationFixture readFrom(String name) {
        InputStream in = ProblemLocationFixture.class.getResourceAsStream(name);
        try (FixtureReader reader = new FixtureReader(name, in)) {
            return reader.read();
        }
    }

    /**
     * An expected problem.
     *
     * @author leadpony
     */
    static class Problem {

        private final long line;
        private final long column;
        private final String pointer;
        private final String keyword;

        Problem(long line, long column, String pointer, String keyword) {
            this.line = line;
            this.column = column;
            this.pointer = pointer;
            this.keyword = keyword;
        }

        long lineNumber() {
            return line;
        }

        long columnNumber() {
            return column;
        }

        String pointer() {
            return pointer;
        }

        String keyword() {
            return keyword;
        }
    }

    /**
     * A reader type for reading a fixture.
     *
     * @author leadpony
     */
    private static class FixtureReader implements AutoCloseable {

        private final String name;
        private final BufferedReader reader;

        FixtureReader(String name, InputStream in) {
            this.name = name;
            this.reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
        }

        @Override
        public void close() {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        ProblemLocationFixture read() {
            try {
                String schema = readJsonAsString();
                String data = readJsonAsString();
                List<Problem> problems = readProblems();
                return new ProblemLocationFixture(name, schema, data, problems);
            } catch (IOException e) {
                return null;
            }
        }

        private String readJsonAsString() throws IOException {
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("---")) {
                    break;
                }
                builder.append(line).append('\n');
            }
            return builder.toString();
        }

        private List<Problem> readProblems() {
            try (JsonReader reader = Json.createReader(this.reader)) {
                return reader.readArray().stream()
                        .map(JsonValue::asJsonObject)
                        .map(FixtureReader::asProblem)
                        .collect(Collectors.toList());
            }
        }

        private static Problem asProblem(JsonObject object) {
            String pointer = object.getString("pointer");
            JsonArray location = object.getJsonArray("location");
            String keyword = object.getString("keyword", null);
            return new Problem(location.getInt(0), location.getInt(1), pointer, keyword);
        }
    }
}
