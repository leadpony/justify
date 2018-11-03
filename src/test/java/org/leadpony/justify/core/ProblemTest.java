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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.json.JsonReader;
import javax.json.stream.JsonLocation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author leadpony
 */
public class ProblemTest {

    private static final Logger log = Logger.getLogger(ProblemTest.class.getName());
    
    private static final String RESOURCE_NAME = "problem.tml";
  
    private static final Jsonv jsonv = Jsonv.newInstance();
    private static final ProblemHandler printer = jsonv.createProblemPrinter(log::info);
    
    public static Stream<ProblemFixture> fixtureProvider() {
        return ProblemFixture.newStream(RESOURCE_NAME);
    }
    
    @ParameterizedTest
    @MethodSource("fixtureProvider")
    public void testProblem(ProblemFixture fixture) {
        JsonSchema schema = readSchema(fixture.schema());
        List<Problem> problems = new ArrayList<>();
        JsonReader reader = jsonv.createReader(new StringReader(fixture.data()), schema, problems::addAll);
        reader.readValue();
        assertThat(problems).hasSameSizeAs(fixture.problems());
        Iterator<Problem> it = problems.iterator();
        Iterator<ProblemSpec> it2 = fixture.problems().iterator();
        while (it.hasNext() && it2.hasNext()) {
            Problem actual = it.next();
            ProblemSpec expected = it2.next();
            JsonLocation loc = actual.getLocation();
            assertThat(loc.getLineNumber()).isEqualTo(expected.lineNumber());
            assertThat(loc.getColumnNumber()).isEqualTo(expected.columnNumber());
            assertThat(actual.getKeyword()).isEqualTo(expected.keyword());
        }
        printProblems(problems, fixture);
    }
    
    private JsonSchema readSchema(String schema) {
        JsonSchemaReader reader = jsonv.createSchemaReader(new StringReader(schema));
        return reader.read();
    }
    
    private void printProblems(List<Problem> problems, ProblemFixture fixture) {
        if (!problems.isEmpty()) {
            log.info(fixture.displayName());
            printer.handleProblems(problems);
        }
    }
}
