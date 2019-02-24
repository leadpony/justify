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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The class providing test cases for "validate" command.
 *
 * @author leadpony
 */
public class ValidateTest {

    /**
     * Provides test fixgtures.
     *
     * @return the stream of test fixtures.
     */
    public static Stream<Fixture> fixtures() {
        return Stream.of(
                // Invalid arguments
                Fixture.of(Status.FAILED, "-unknown"),
                Fixture.of(Status.FAILED, "person.schema.json"),
                Fixture.of(Status.FAILED, "-r"),
                Fixture.of(Status.FAILED, "-r", "-s", "empty.schema.json"),

                // Validates a schema
                Fixture.of(Status.VALID, "-s", "arrays.schema.json"),
                Fixture.of(Status.VALID, "-s", "person.schema.json"),
                Fixture.of(Status.INVALID, "-s", "person-invalid.schema.json"),
                Fixture.of(Status.INVALID, "-s", "person-malformed.schema.json"),
                Fixture.of(Status.FAILED, "-s", "nonexistent.schema.json"),
                Fixture.of(Status.VALID, "-s", "person-lax.schema.json"),
                Fixture.of(Status.INVALID, "-strict", "-s", "person-lax.schema.json"),

                // Validates a schema with referenced schemas.
                Fixture.of(Status.INVALID, "-s", "product.schema.json"),
                Fixture.of(Status.VALID, "-s", "product.schema.json", "-r", "geographical-location.schema.json"),
                Fixture.of(Status.FAILED, "-s", "product.schema.json", "-r", "nonexistent.json"),
                Fixture.of(Status.INVALID, "-s", "product.schema.json", "-r", "malformed.json"),
                Fixture.of(Status.VALID, "-s", "empty.schema.json", "-r", "empty.schema.json"),
                Fixture.of(Status.VALID, "-s", "empty.schema.json", "-r", "true.schema.json"),
                Fixture.of(Status.VALID, "-s", "empty.schema.json", "-r", "false.schema.json"),
                Fixture.of(Status.VALID, "-s", "empty.schema.json", "-r", "invalid-id.schema.json"),
                Fixture.of(Status.VALID, "-s", "company.schema.json", "-r", "person.schema.json", "geographical-location.schema.json"),
                Fixture.of(Status.VALID, "-s", "company.schema.json", "-catalog", "catalog.json"),
                Fixture.of(Status.FAILED, "-s", "company.schema.json", "-catalog", "nonexistent.json"),
                Fixture.of(Status.FAILED, "-s", "company.schema.json", "-catalog", "malformed.json"),
                Fixture.of(Status.FAILED, "-s", "company.schema.json", "-catalog", "true.schema.json"),
                Fixture.of(Status.FAILED, "-s", "company.schema.json", "-catalog", "catalog-invalid.json"),

                // Validates an instance against a schema.
                Fixture.of(Status.VALID, "-s", "arrays.schema.json", "-i", "arrays.json"),
                Fixture.of(Status.INVALID, "-s", "arrays.schema.json", "-i", "arrays-invalid.json"),
                Fixture.of(Status.VALID, "-s", "geographical-location.schema.json", "-i", "geographical-location.json"),
                Fixture.of(Status.INVALID, "-s", "geographical-location.schema.json", "-i", "geographical-location-invalid.json"),
                Fixture.of(Status.VALID, "-s", "person.schema.json", "-i", "person.json"),
                Fixture.of(Status.INVALID, "-s", "person.schema.json", "-i", "person-invalid.json"),
                Fixture.of(Status.INVALID, "-s", "person.schema.json", "-i", "person-malformed.json"),
                Fixture.of(Status.FAILED, "-s", "person.schema.json", "-i", "nonexistent.json"),
                Fixture.of(Status.FAILED, "-s", "nonexistent.schema.json", "-i", "person.json"),
                Fixture.of(Status.INVALID, "-s", "person.schema.json", "-i", "person-invalid.json", "person.json"),

                // Validates an instance against a schema with referenced schemas.
                Fixture.of(Status.VALID, "-s", "product.schema.json", "-r", "geographical-location.schema.json", "-i", "product.json"),
                Fixture.of(Status.INVALID, "-s", "product.schema.json", "-r", "geographical-location.schema.json", "-i", "product-invalid.json"),
                Fixture.of(Status.INVALID, "-s", "product.schema.json", "-r", "geographical-location.schema.json", "-i", "product-invalid.json", "product.json"),
                Fixture.of(Status.VALID, "-s", "company.schema.json", "-catalog", "catalog.json", "-i", "company.json"),
                Fixture.of(Status.INVALID, "-s", "company.schema.json", "-catalog", "catalog.json", "-i", "company-invalid.json"),
                Fixture.of(Status.INVALID, "-s", "company.schema.json", "-catalog", "catalog.json", "-i", "company-invalid.json", "company.json")
                );
    }

    private static int index = 1;

    @ParameterizedTest
    @MethodSource("fixtures")
    public void test(Fixture fixture) {
        System.out.println("[" + index++ + "] " + fixture);
        Launcher launcher = new Launcher();
        Status actual = launcher.launch(fixture.args());
        System.out.println();
        assertThat(actual).isEqualTo(fixture.getExpectedStatus());
    }
}
