/*
 * Copyright 2018 the Justify authors.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author leadpony
 */
class ProblemFixture extends Fixture {
    
    private final String schema;
    private final String data;
    private final String description;
    private final List<ProblemSpec> problems;
    
    /**
     * @param name
     * @param index
     */
    private ProblemFixture(String name, int index, String schema, String data, String description,
            List<ProblemSpec> problems) {
        super(name, index);
        this.schema = schema;
        this.data = data;
        this.description = description;
        this.problems = problems;
    }

    @Override
    String description() {
        return description;
    }
    
    String schema() {
        return schema;
    }

    String data() {
        return data;
    }
    
    List<ProblemSpec> problems() {
        return problems;
    }
    
    static Stream<ProblemFixture> newStream(String name) {
        InputStream in = ProblemFixture.class.getResourceAsStream(name);
        try (FixtureReader reader = new FixtureReader(name, in)) {
            return reader.read().stream();
        }
    }
    
    private static class FixtureReader implements AutoCloseable {
        
        private final String name;
        private final BufferedReader reader;
        private final List<ProblemFixture> fixtures = new ArrayList<>();
        private String line;
        
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
                throw new UncheckedIOException(e);
            }
        }

        List<ProblemFixture> read() {
            moveNext();
            while (readFixture()) {
            }
            return fixtures;
        }
        
        private boolean readFixture() {
            String line = currentLine();
            if (line == null || !line.startsWith("===")) {
                return false;
            }
            String description = line.substring(3).trim();
            moveNext();
            line = currentLine();
            if (line == null || line.startsWith("===")) {
                return false;
            }
            moveNext();
            String schema = readBlock();
            line = currentLine();
            if (line == null || line.startsWith("===")) {
                return false;
            }
            moveNext();
            String data = readBlock();
            line = currentLine();
            if (line == null || line.startsWith("===")) {
                return false;
            }
            moveNext();
            List<ProblemSpec> problems = readProblems();
            fixtures.add(new ProblemFixture(
                    name, fixtures.size(), schema, data, description, problems));
            return true;
        }
        
        private String readBlock() {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = currentLine()) != null) {
                if (line.startsWith("===") || line.startsWith("---")) {
                    break;
                }
                builder.append(line).append("\n");
                moveNext();
            }
            return builder.toString();
        }
        
        private List<ProblemSpec> readProblems() {
            List<ProblemSpec> problems = new ArrayList<>();
            String line;
            while ((line = currentLine()) != null) {
                if (line.startsWith("===") || line.startsWith("---")) {
                    break;
                }
                String[] tokens = line.split("\\s*,\\s*");
                problems.add(new ProblemSpec(
                        Long.parseLong(tokens[0]),
                        Long.parseLong(tokens[1]),
                        tokens[2]
                        ));
                moveNext();
            }
            return problems;
        }
        
        private String currentLine() {
            return this.line;
        }
        
        private void moveNext() {
            try {
                this.line = reader.readLine();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
