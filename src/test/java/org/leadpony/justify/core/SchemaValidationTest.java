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
            "schema/keyword/type.json",
            "schema/keyword/enum.json",
            "schema/keyword/const.json",
            "schema/keyword/multipleOf.json",
            "schema/keyword/maximum.json",
            "schema/keyword/exclusiveMaximum.json",
            "schema/keyword/minimum.json",
            "schema/keyword/exclusiveMinimum.json",
            "schema/keyword/maxLength.json",
            "schema/keyword/minLength.json",
            "schema/keyword/pattern.json",
            "schema/keyword/items.json",
            "schema/keyword/additionalItems.json",
            "schema/keyword/maxItems.json",
            "schema/keyword/minItems.json",
            "schema/keyword/uniqueItems.json",
            "schema/keyword/contains.json",
            "schema/keyword/maxProperties.json",
            "schema/keyword/minProperties.json",
            "schema/keyword/required.json",
            "schema/keyword/properties.json",
            "schema/keyword/patternProperties.json",
            "schema/keyword/additionalProperties.json",
            "schema/keyword/dependencies.json",
            "schema/keyword/propertyNames.json",
            "schema/keyword/if.json",
            "schema/keyword/then.json",
            "schema/keyword/else.json",
            "schema/keyword/allOf.json",
            "schema/keyword/anyOf.json",
            "schema/keyword/oneOf.json",
            "schema/keyword/not.json",
            "schema/keyword/definitions.json",
            "schema/keyword/title.json",
            "schema/keyword/description.json",
            "schema/keyword/default.json",
        };
    
    private static final Jsonv jsonv = Jsonv.newInstance();
    
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
            problems.forEach(p->p.printAll(log::info));
        }
    }
}
