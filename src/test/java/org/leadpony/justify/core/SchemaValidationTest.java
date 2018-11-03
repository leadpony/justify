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

package org.leadpony.justify.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
        };
    
    private static final Jsonv jsonv = Jsonv.newInstance();
    private static final ProblemHandler printer = jsonv.createProblemPrinter(log::info);
    
    public static Stream<SchemaFixture> provideFixtures() {
        return Stream.of(TESTS).flatMap(SchemaFixture::newStream);
    }
    
    @ParameterizedTest
    @MethodSource("provideFixtures")
    public void testInvalidSchema(SchemaFixture fixture) {
        String value = fixture.schema().toString();
        JsonSchemaReader reader = jsonv.createSchemaReader(new StringReader(value));
        try {
            reader.read();
            assertThat(true).isEqualTo(fixture.getSchemaValidity());
        } catch (JsonValidatingException e) {
            assertThat(false).isEqualTo(fixture.getSchemaValidity());
            printProblems(fixture, e.getProblems());
        }
    }
    
    private void printProblems(SchemaFixture fixture, List<Problem> problems) {
        if (!problems.isEmpty()) {
            log.info(fixture.displayName());
            printer.handleProblems(problems);
        }
    }
}
