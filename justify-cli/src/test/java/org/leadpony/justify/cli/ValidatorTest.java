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
package org.leadpony.justify.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class of {@link Validator}.
 *
 * @author leadpony
 */
public class ValidatorTest {

    public static Stream<Arguments> fixtures() {
        return Stream.of(
            // Validates JSON schemas
            Arguments.of(args("arrays.schema.json"), 0),
            Arguments.of(args("person.schema.json"), 0),
            Arguments.of(args("person-invalid.schema.json"), 1),
            Arguments.of(args("person-lax.schema.json"), 0),
            Arguments.of(args("-strict", "person-lax.schema.json"), 1),
            Arguments.of(args("person-corrupt.schema.json"), 1),
            Arguments.of(args("nonexistent.schema.json"), 1),
            // Validates JSON instances
            Arguments.of(args("arrays.schema.json", "arrays.json"), 0),
            Arguments.of(args("arrays.schema.json", "arrays-invalid.json"), 1),
            Arguments.of(args("geographical-location.schema.json", "geographical-location.json"), 0),
            Arguments.of(args("geographical-location.schema.json", "geographical-location-invalid.json"), 1),
            Arguments.of(args("person.schema.json", "person.json"), 0),
            Arguments.of(args("person.schema.json", "person-invalid.json"), 1),
            Arguments.of(args("person.schema.json", "person-corrupt.json"), 1),
            Arguments.of(args("person.schema.json", "nonexistent.json"), 1),
            Arguments.of(args("nonexistent.schema.json", "person.json"), 1),
            Arguments.of(args("person.schema.json", "person.json", "person-invalid.json"), 1),
            // Without arguments
            Arguments.of(args(), 0),
            Arguments.of(args("-h"), 0),
            Arguments.of(args("-unknown"), 1)
            );
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    public void test(String[] args, int expectedCode) {
        Validator validator = new Validator();
        int exitCode = validator.run(args);
        assertThat(exitCode).isEqualTo(expectedCode);
    }

    private static final Path baseDir = Paths.get("target", "test-classes");

    private static String[] args(String... strings) {
        for (int i = 0; i < strings.length; i++) {
            String item = strings[i];
            if (!item.startsWith("-")) {
                strings[i] = baseDir.resolve(item).toString();
            }
        }
        return strings;
    }
}
