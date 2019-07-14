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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.json.JsonReader;
import javax.json.stream.JsonLocation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A test class for testing problem locations.
 *
 * @author leadpony
 */
public class ProblemLocationTest extends BaseTest {

    private static final String[] FILES = {
            "problem/additionalItems.txt",
            "problem/additionalItems-false.txt",
            "problem/additionalProperties.txt",
            "problem/additionalProperties-false.txt",
            "problem/format.txt",
            "problem/items.txt",
            "problem/items-false.txt",
            "problem/items-in-array.txt",
            "problem/items-in-object.txt",
            "problem/maximum.txt",
            "problem/maxItems.txt",
            "problem/maxProperties.txt",
            "problem/minimum.txt",
            "problem/minItems.txt",
            "problem/minProperties.txt",
            "problem/patternProperties.txt",
            "problem/patternProperties-false.txt",
            "problem/properties.txt",
            "problem/properties-false.txt",
            "problem/properties-in-array.txt",
            "problem/properties-in-object.txt",
            "problem/propertyNames.txt",
            "problem/required.txt",
            "problem/required-in-array.txt",
            "problem/required-in-object.txt",
            "problem/type.txt",
            "problem/uniqueItems.txt",
            "problem/uniqueItems-in-object.txt"
    };

    public static Stream<ProblemLocationFixture> fixtureProvider() {
        return Stream.of(FILES).map(ProblemLocationFixture::readFrom);
    }

    @ParameterizedTest
    @MethodSource("fixtureProvider")
    public void testProblem(ProblemLocationFixture fixture) {
        JsonSchema schema = readSchema(fixture.schema());
        List<Problem> problems = new ArrayList<>();
        JsonReader reader = SERVICE.createReader(new StringReader(fixture.instance()), schema, problems::addAll);
        reader.readValue();
        assertThat(problems).hasSameSizeAs(fixture.problems());
        Iterator<Problem> it = problems.iterator();
        Iterator<ProblemLocationFixture.Problem> it2 = fixture.problems().iterator();
        while (it.hasNext() && it2.hasNext()) {
            Problem actual = it.next();
            ProblemLocationFixture.Problem expected = it2.next();
            JsonLocation loc = actual.getLocation();
            assertThat(loc.getLineNumber()).isEqualTo(expected.lineNumber());
            assertThat(loc.getColumnNumber()).isEqualTo(expected.columnNumber());
            assertThat(actual.getPointer()).isEqualTo(expected.pointer());
            assertThat(actual.getKeyword()).isEqualTo(expected.keyword());
        }
        printProblems(problems, fixture);
    }

    private JsonSchema readSchema(String schema) {
        JsonSchemaReader reader = SERVICE.createSchemaReader(new StringReader(schema));
        return reader.read();
    }

    private void printProblems(List<Problem> problems, ProblemLocationFixture fixture) {
        if (!problems.isEmpty()) {
            print(fixture.toString());
            print(problems);
        }
    }
}
