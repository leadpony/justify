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
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.Loggers;

/**
 * @author leadpony
 */
public class InvalidSchemaTest {

    private static final Logger log = Loggers.getLogger(InvalidSchemaTest.class);
    
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
        };
    
    public static Stream<Arguments> fixtureProvider() {
        return Stream.of(TESTS)
                .flatMap(SchemaFixture::newStream)
                .map(Fixture::toArguments);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtureProvider")
    public void testInvalidSchema(String displayName, SchemaFixture fixture) {
        String value = fixture.schema().toString();
        JsonSchemaReader reader = Jsonv.createSchemaReader(new StringReader(value));
        try {
            reader.read();
            assertThat(fixture.getSchemaValidity()).isTrue();
        } catch (JsonValidatingException e) {
            assertThat(fixture.getSchemaValidity()).isFalse();
            printProblems(fixture, e.getProblems());
        }
    }
    
    @Test
    public void testBlankSchema() {
        String schemaJson = "";
        JsonSchemaReader reader = Jsonv.createSchemaReader(new StringReader(schemaJson));
        Throwable thrown = catchThrowable(()->reader.read());
        assertThat(thrown).isInstanceOf(JsonValidatingException.class);
        JsonValidatingException e = (JsonValidatingException)thrown;
        List<Problem> problems = e.getProblems();
        assertThat(problems).hasSize(1);
        Jsonv.createProblemPrinter(log::info).accept(problems);
    }

    private void printProblems(SchemaFixture fixture, List<Problem> problems) {
        if (!problems.isEmpty()) {
            log.info(fixture.displayName());
            Jsonv.createProblemPrinter(log::info).accept(problems);
        }
    }
}
