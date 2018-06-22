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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author leadpony
 */
@RunWith(Parameterized.class)
public class InvalidSchemaTest extends AbstractSpecTest {

    private static final String[] TESTS = {
            "/additional/invalid_schema.json",
        };
    
    @Parameters(name = "{0}@{1}: {2}")
    public static Iterable<Object[]> parameters() {
        return fixtures(TESTS);
    }

    public InvalidSchemaTest(String name, int testIndex, String description, Fixture fixture) {
        super(name, testIndex, description, fixture);
    }
    
    @Test
    public void testInvalidSchema() {
        String value = getFixture().schema().toString(); 
        JsonSchemaReader reader = JsonSchemaReader.from(new StringReader(value));
        Throwable thrown = catchThrowable(()->reader.read());
        assertThat(thrown).isInstanceOf(JsonValidatingException.class);
        JsonValidatingException e = (JsonValidatingException)thrown;
        printProblems(e.problems());
    }
}
