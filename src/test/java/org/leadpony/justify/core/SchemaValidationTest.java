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
import org.leadpony.justify.Loggers;

/**
 * @author leadpony
 */
public class SchemaValidationTest {

    private static final Logger log = Loggers.getLogger(SchemaValidationTest.class);
    
    private static final String[] TESTS = {
            "/unofficial/schema/schema.json",
            "/unofficial/schema/keyword/type.json",
            "/unofficial/schema/keyword/enum.json",
            "/unofficial/schema/keyword/const.json",
            "/unofficial/schema/keyword/multipleOf.json",
            "/unofficial/schema/keyword/maximum.json",
            "/unofficial/schema/keyword/exclusiveMaximum.json",
            "/unofficial/schema/keyword/minimum.json",
            "/unofficial/schema/keyword/exclusiveMinimum.json",
            "/unofficial/schema/keyword/maxLength.json",
            "/unofficial/schema/keyword/minLength.json",
            "/unofficial/schema/keyword/pattern.json",
            "/unofficial/schema/keyword/items.json",
            "/unofficial/schema/keyword/additionalItems.json",
            "/unofficial/schema/keyword/maxItems.json",
            "/unofficial/schema/keyword/minItems.json",
            "/unofficial/schema/keyword/uniqueItems.json",
            "/unofficial/schema/keyword/contains.json",
            "/unofficial/schema/keyword/maxProperties.json",
            "/unofficial/schema/keyword/minProperties.json",
            "/unofficial/schema/keyword/required.json",
            "/unofficial/schema/keyword/properties.json",
            "/unofficial/schema/keyword/patternProperties.json",
            "/unofficial/schema/keyword/additionalProperties.json",
            "/unofficial/schema/keyword/dependencies.json",
            "/unofficial/schema/keyword/propertyNames.json",
            "/unofficial/schema/keyword/if.json",
            "/unofficial/schema/keyword/then.json",
            "/unofficial/schema/keyword/else.json",
            "/unofficial/schema/keyword/allOf.json",
            "/unofficial/schema/keyword/anyOf.json",
            "/unofficial/schema/keyword/oneOf.json",
            "/unofficial/schema/keyword/not.json",
            "/unofficial/schema/keyword/definitions.json",
            "/unofficial/schema/keyword/title.json",
            "/unofficial/schema/keyword/description.json",
            "/unofficial/schema/keyword/default.json",
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
