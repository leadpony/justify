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

import org.junit.Assume;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test suite provided by json-schema.org.
 * 
 * @author leadpony
 */
@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class SpecTest extends AbstractValidationTest {

    private static final String[] TESTS = {
            "/spec/tests/draft7/additionalItems.json",
            "/spec/tests/draft7/additionalProperties.json",
            "/spec/tests/draft7/allOf.json",
            "/spec/tests/draft7/anyOf.json",
            "/spec/tests/draft7/boolean_schema.json",
            "/spec/tests/draft7/const.json",
            "/spec/tests/draft7/contains.json",
            "/spec/tests/draft7/default.json",
            "/spec/tests/draft7/definitions.json",
            "/spec/tests/draft7/dependencies.json",
            "/spec/tests/draft7/enum.json",
            "/spec/tests/draft7/exclusiveMaximum.json",
            "/spec/tests/draft7/exclusiveMinimum.json",
            "/spec/tests/draft7/if-then-else.json",
            "/spec/tests/draft7/items.json",
            "/spec/tests/draft7/maximum.json",
            "/spec/tests/draft7/maxItems.json",
            "/spec/tests/draft7/maxLength.json",
            "/spec/tests/draft7/maxProperties.json",
            "/spec/tests/draft7/minimum.json",
            "/spec/tests/draft7/minItems.json",
            "/spec/tests/draft7/minLength.json",
            "/spec/tests/draft7/minProperties.json",
            "/spec/tests/draft7/multipleOf.json",
            "/spec/tests/draft7/not.json",
            "/spec/tests/draft7/oneOf.json",
            "/spec/tests/draft7/pattern.json",
            "/spec/tests/draft7/patternProperties.json",
            "/spec/tests/draft7/properties.json",
            "/spec/tests/draft7/propertyNames.json",
            "/spec/tests/draft7/ref.json",
            "/spec/tests/draft7/required.json",
            "/spec/tests/draft7/type.json",
            "/spec/tests/draft7/uniqueItems.json",
            // for draft 8
            "/spec/tests/draft8/maxContains.json",
            "/spec/tests/draft8/minContains.json"
    };
    
    @Parameters(name = "{0}@{1}: {2}")
    public static Iterable<Object[]> parameters() {
        return fixtures(TESTS);
    }

    public SpecTest(String name, int testIndex, String description, Fixture fixture) {
        super(name, testIndex, description, fixture);
    }

    @Before
    public void setUp() {
        //Assume.assumeTrue(getTestIndex() == 1);
    }
}
