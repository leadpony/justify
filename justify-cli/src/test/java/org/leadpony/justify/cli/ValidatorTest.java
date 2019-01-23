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

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class of {@link Validator}.
 *
 * @author leadpony
 */
public class ValidatorTest {

    /**
     * Returns JSON schemas to test.
     * @return JSON schemas to test.
     */
    public static Stream<Arguments> schemas() {
        return Stream.of(
                Arguments.of("arrays.schema.json", 0),
                Arguments.of("geographical-location.schema.json", 0),
                Arguments.of("person.schema.json", 0),
                Arguments.of("person-invalid.schema.json", 4),
                Arguments.of("nonexistent.schema.json", 1)
                );
    }

    @ParameterizedTest(name="[{index}] {0}")
    @MethodSource("schemas")
    public void testSchemaValidation(String path, int expected) {
        Validator validator = new Validator();
        validator.validate(path(path));
        assertThat(validator.getNumberOfErrors()).isEqualTo(expected);
    }

    /**
     * Returns JSON instances to test.
     * @return JSON instances to test.
     */
    public static Stream<Arguments> instances() {
        return Stream.of(
                Arguments.of("arrays.schema.json", "arrays.json", 0),
                Arguments.of("arrays.schema.json", "arrays-invalid.json", 2),
                Arguments.of("geographical-location.schema.json", "geographical-location.json", 0),
                Arguments.of("geographical-location.schema.json", "geographical-location-invalid.json", 1),
                Arguments.of("person.schema.json", "person.json", 0),
                Arguments.of("person.schema.json", "person-invalid.json", 3),
                Arguments.of("person.schema.json", "nonexistent.json", 1),
                Arguments.of("nonexistent.schema.json", "person.json", 1)
                );
    }

    @ParameterizedTest(name="[{index}] {1}")
    @MethodSource("instances")
    public void testInstanceValidation(String schemaPath, String instancePath, int expected) {
        Validator validator = new Validator();
        validator.validate(path(schemaPath), path(instancePath));
        assertThat(validator.getNumberOfErrors()).isEqualTo(expected);
    }

    @Test
    public void testNoArguments() {
        Validator validator = new Validator();
        validator.validate();
        assertThat(validator.getNumberOfErrors()).isEqualTo(0);
    }

    private String path(String name) {
        return Paths.get("target", "test-classes", name).toString();
    }
}
