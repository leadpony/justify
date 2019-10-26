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

package org.leadpony.justify.tests.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.json.JsonReader;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.stream.JsonLocation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.tests.helper.ApiTest;
import org.leadpony.justify.tests.helper.ProblemPrinter;
import org.leadpony.justify.tests.helper.MultiJsonSource;

/**
 * A test class for testing problem locations.
 *
 * @author leadpony
 */
@ApiTest
public class ProblemLocationTest {

    private static Logger log;
    private static JsonValidationService service;
    private static ProblemPrinter printer;

    private static Jsonb jsonb;

    @BeforeAll
    public static void setUpOnce() {
        jsonb = JsonbBuilder.create();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MultiJsonSource({
        "problem/additionalItems.txt",
        "problem/additionalProperties.txt",
        "problem/allOf.txt",
        "problem/anyOf.txt",
        "problem/contains.txt",
        "problem/format.txt",
        "problem/items.txt",
        "problem/maximum.txt",
        "problem/maxItems.txt",
        "problem/maxProperties.txt",
        "problem/minimum.txt",
        "problem/minItems.txt",
        "problem/minProperties.txt",
        "problem/oneOf.txt",
        "problem/patternProperties.txt",
        "problem/properties.txt",
        "problem/propertyNames.txt",
        "problem/required.txt",
        "problem/type.txt",
        "problem/uniqueItems.txt",
    })
    public void testProblem(String displayName, String schema, String instance, String problems) {
        List<ExpectedProblem> expected = toExpectedProblems(problems);
        JsonSchema jsonSchema = readSchema(schema);
        List<Problem> actual = new ArrayList<>();

        try (JsonReader reader = service.createReader(
                new StringReader(instance), jsonSchema, actual::addAll)) {
            reader.readValue();
        }

        if (!actual.isEmpty()) {
            log.info(displayName);
            printer.print(actual);
        }

        checkProblems(actual, expected);
    }

    private static void checkProblems(List<Problem> actual, List<ExpectedProblem> expected) {
        assertThat(actual).hasSameSizeAs(expected);

        Iterator<Problem> a = actual.iterator();
        Iterator<ExpectedProblem> b = expected.iterator();
        while (a.hasNext() && b.hasNext()) {
            checkProblem(a.next(), b.next());
        }
    }

    private static void checkProblem(Problem actual, ExpectedProblem expected) {
        JsonLocation loc = actual.getLocation();
        if (loc != null) {
            assertThat(loc.getLineNumber()).isEqualTo(expected.location[0]);
            assertThat(loc.getColumnNumber()).isEqualTo(expected.location[1]);
        }

        assertThat(actual.getPointer()).isEqualTo(expected.pointer);
        assertThat(actual.getKeyword()).isEqualTo(expected.keyword);

        assertThat(actual.hasBranches()).isEqualTo(expected.hasBranches());

        if (actual.hasBranches()) {
            assertThat(actual.countBranches()).isEqualTo(expected.branches.size());
            for (int i = 0; i < actual.countBranches(); i++) {
                checkProblems(actual.getBranch(i), expected.branches.get(i));
            }
        }
    }

    private static List<ExpectedProblem> toExpectedProblems(String string) {
        @SuppressWarnings("serial")
        Type targetType = new ArrayList<ExpectedProblem>() { }.getClass().getGenericSuperclass();
        return jsonb.fromJson(string, targetType);
    }

    /**
     * @author leadpony
     */
    public static class ExpectedProblem {
        public int[] location;
        public String pointer;
        public String keyword;

        public List<List<ExpectedProblem>> branches;

        boolean hasBranches() {
            return branches != null;
        }
    }

    private JsonSchema readSchema(String schema) {
        JsonSchemaReader reader = service.createSchemaReader(new StringReader(schema));
        return reader.read();
    }
}
