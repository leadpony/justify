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
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/**
 * @author leadpony
 */
public class SchemaValidationTest {

    private static final Logger log = Logger.getLogger(SchemaValidationTest.class.getName());

    private static final String[] TESTS = {
            "schema/schema.json",
            "schema/core/schema.json",
            "schema/core/id.json",
            "schema/core/ref.json",
            "schema/validation/type.json",
            "schema/validation/type.json",
            "schema/validation/enum.json",
            "schema/validation/const.json",
            "schema/validation/multipleOf.json",
            "schema/validation/maximum.json",
            "schema/validation/exclusiveMaximum.json",
            "schema/validation/minimum.json",
            "schema/validation/exclusiveMinimum.json",
            "schema/validation/maxLength.json",
            "schema/validation/minLength.json",
            "schema/validation/pattern.json",
            "schema/validation/items.json",
            "schema/validation/additionalItems.json",
            "schema/validation/maxItems.json",
            "schema/validation/minItems.json",
            "schema/validation/uniqueItems.json",
            "schema/validation/contains.json",
            "schema/validation/maxProperties.json",
            "schema/validation/minProperties.json",
            "schema/validation/required.json",
            "schema/validation/properties.json",
            "schema/validation/patternProperties.json",
            "schema/validation/additionalProperties.json",
            "schema/validation/dependencies.json",
            "schema/validation/propertyNames.json",
            "schema/validation/if.json",
            "schema/validation/then.json",
            "schema/validation/else.json",
            "schema/validation/allOf.json",
            "schema/validation/anyOf.json",
            "schema/validation/oneOf.json",
            "schema/validation/not.json",
            "schema/validation/definitions.json",
            "schema/validation/title.json",
            "schema/validation/description.json",
            "schema/validation/default.json",
            "schema/validation/optional/format.json",
            "schema/validation/optional/contentEncoding.json",
            "schema/validation/optional/contentMediaType.json",
        };

    private static final JsonValidationService service = JsonValidationService.newInstance();
    private static final ProblemHandler printer = service.createProblemPrinter(log::info);

    public static Stream<SchemaFixture> provideAllFixtures() {
        return Stream.of(TESTS).flatMap(SchemaFixture::newStream);
    }

    @ParameterizedTest
    @MethodSource("provideAllFixtures")
    public void testSchemaValidation(SchemaFixture fixture) {
        String value = fixture.schema().toString();
        try (JsonSchemaReader reader = service.createSchemaReader(new StringReader(value))) {
            reader.read();
            assertThat(true).isEqualTo(fixture.hasValidSchema());
        } catch (JsonValidatingException e) {
            assertThat(false).isEqualTo(fixture.hasValidSchema());
            printProblems(fixture, e.getProblems());
        }
    }

    public static Stream<SchemaFixture> provideValidFixtures() {
        return provideAllFixtures().filter(SchemaFixture::hasValidSchema);
    }

    @ParameterizedTest
    @MethodSource("provideValidFixtures")
    public void toJson_shouldProduceOriginalJson(SchemaFixture fixture) {
        String value = fixture.schema().toString();
        JsonSchema schema = null;
        try (JsonSchemaReader reader = service.createSchemaReader(new StringReader(value))) {
            schema = reader.read();
        }

        assertThat(schema.toJson()).isEqualTo(fixture.schema());
    }

    private void printProblems(SchemaFixture fixture, List<Problem> problems) {
        if (!problems.isEmpty()) {
            log.info(fixture.displayName());
            printer.handleProblems(problems);
        }
    }
}
