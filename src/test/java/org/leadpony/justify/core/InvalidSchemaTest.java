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
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

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
            "/unofficial/invalid_schema/schema.json",
            "/unofficial/invalid_schema/keyword/type.json",
        };
    
    public static Stream<Arguments> fixtureProvider() {
        return Stream.of(TESTS)
                .flatMap(SchemaFixture::newStream)
                .map(fixture->Arguments.of(fixture.displayName(), fixture.description(), fixture));
    }
    
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("fixtureProvider")
    public void testInvalidSchema(String name, String description, SchemaFixture fixture) {
        String value = fixture.schema().toString();
        JsonSchemaReader reader = Jsonv.createSchemaReader(new StringReader(value));
        Throwable thrown = catchThrowable(()->reader.read());
        assertThat(thrown).isInstanceOf(JsonValidatingException.class);
        JsonValidatingException e = (JsonValidatingException)thrown;
        printProblems(fixture, e.getProblems());
    }

    private void printProblems(SchemaFixture fixture, List<Problem> problems) {
        if (problems.isEmpty()) {
            return;
        }
        log.info(fixture.displayName() + ": The schema has the following problem(s).");
        Jsonv.createProblemPrinter(log::info).accept(problems);
    }
}
