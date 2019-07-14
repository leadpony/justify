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

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A test class for testing schema validations.
 *
 * @author leadpony
 */
public class SchemaValidationTest extends BaseTest {

    private static final String[] TESTS = {
            "schema/schema.json",
            "schema/core/schema.json",
            "schema/core/id.json",
            "schema/core/ref.json",

            "schema/validation/additionalItems.json",
            "schema/validation/additionalProperties.json",
            "schema/validation/allOf.json",
            "schema/validation/anyOf.json",
            "schema/validation/const.json",
            "schema/validation/contains.json",
            "schema/validation/default.json",
            "schema/validation/definitions.json",
            "schema/validation/dependencies.json",
            "schema/validation/description.json",
            "schema/validation/else.json",
            "schema/validation/enum.json",
            "schema/validation/exclusiveMaximum.json",
            "schema/validation/exclusiveMinimum.json",
            "schema/validation/if.json",
            "schema/validation/items.json",
            "schema/validation/maximum.json",
            "schema/validation/maxItems.json",
            "schema/validation/maxLength.json",
            "schema/validation/maxProperties.json",
            "schema/validation/minimum.json",
            "schema/validation/minItems.json",
            "schema/validation/minLength.json",
            "schema/validation/minProperties.json",
            "schema/validation/multipleOf.json",
            "schema/validation/not.json",
            "schema/validation/oneOf.json",
            "schema/validation/pattern.json",
            "schema/validation/patternProperties.json",
            "schema/validation/properties.json",
            "schema/validation/propertyNames.json",
            "schema/validation/required.json",
            "schema/validation/then.json",
            "schema/validation/title.json",
            "schema/validation/type.json",
            "schema/validation/uniqueItems.json",

            "schema/validation/optional/format.json",
            "schema/validation/optional/contentEncoding.json",
            "schema/validation/optional/contentMediaType.json",
        };

    public static Stream<SchemaFixture> provideAllFixtures() {
        return Stream.of(TESTS).flatMap(SchemaFixture::newStream);
    }

    @ParameterizedTest
    @MethodSource("provideAllFixtures")
    public void testSchemaValidation(SchemaFixture fixture) {
        String value = fixture.schema().toString();
        try (JsonSchemaReader reader = SERVICE.createSchemaReader(new StringReader(value))) {
            reader.read();
            assertThat(true).isEqualTo(fixture.hasValidSchema());
        } catch (JsonValidatingException e) {
            List<Problem> problems = e.getProblems();
            printProblems(fixture, problems);
            assertThat(false).isEqualTo(fixture.hasValidSchema());
            verifyProblemDetails(fixture, problems);
        }
    }

    private void verifyProblemDetails(SchemaFixture fixture, List<Problem> problems) {
        List<SchemaFixture.Error> errors = fixture.errors();
        if (errors.isEmpty()) {
            return;
        }
        assertThat(problems).hasSameSizeAs(errors);
        Iterator<SchemaFixture.Error> it = errors.iterator();
        for (Problem problem : problems) {
            SchemaFixture.Error error = it.next();
            assertThat(problem.getPointer()).isEqualTo(error.pointer());
        }
    }

    public static Stream<SchemaFixture> provideValidFixtures() {
        return provideAllFixtures().filter(SchemaFixture::hasValidSchema);
    }

    @ParameterizedTest
    @MethodSource("provideValidFixtures")
    public void toJsonShouldProduceOriginalJson(SchemaFixture fixture) {
        String value = fixture.schema().toString();
        JsonSchema schema = null;
        try (JsonSchemaReader reader = SERVICE.createSchemaReader(new StringReader(value))) {
            schema = reader.read();
        }

        assertThat(schema.toJson()).isEqualTo(fixture.schema());
    }

    private void printProblems(SchemaFixture fixture, List<Problem> problems) {
        if (!problems.isEmpty()) {
            print(fixture.displayName());
            print(problems);
        }
    }
}
