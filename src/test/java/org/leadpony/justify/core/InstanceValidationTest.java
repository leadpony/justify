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

import java.util.stream.Stream;

/**
 * Test suite provided by json-schema.org.
 * 
 * @author leadpony
 */
public class InstanceValidationTest extends BaseValidationTest {

    private static final String[] TESTS = {
            "/official/tests/draft7/additionalItems.json",
            "/official/tests/draft7/additionalProperties.json",
            "/official/tests/draft7/allOf.json",
            "/official/tests/draft7/anyOf.json",
            "/official/tests/draft7/boolean_schema.json",
            "/official/tests/draft7/const.json",
            "/official/tests/draft7/contains.json",
            "/official/tests/draft7/default.json",
            "/official/tests/draft7/definitions.json",
            "/official/tests/draft7/dependencies.json",
            "/official/tests/draft7/enum.json",
            "/official/tests/draft7/exclusiveMaximum.json",
            "/official/tests/draft7/exclusiveMinimum.json",
            "/official/tests/draft7/if-then-else.json",
            "/official/tests/draft7/items.json",
            "/official/tests/draft7/maximum.json",
            "/official/tests/draft7/maxItems.json",
            "/official/tests/draft7/maxLength.json",
            "/official/tests/draft7/maxProperties.json",
            "/official/tests/draft7/minimum.json",
            "/official/tests/draft7/minItems.json",
            "/official/tests/draft7/minLength.json",
            "/official/tests/draft7/minProperties.json",
            "/official/tests/draft7/multipleOf.json",
            "/official/tests/draft7/not.json",
            "/official/tests/draft7/oneOf.json",
            "/official/tests/draft7/pattern.json",
            "/official/tests/draft7/patternProperties.json",
            "/official/tests/draft7/properties.json",
            "/official/tests/draft7/propertyNames.json",
            "/official/tests/draft7/ref.json",
            "/official/tests/draft7/required.json",
            "/official/tests/draft7/type.json",
            "/official/tests/draft7/uniqueItems.json",
    };
    
    public static Stream<ValidationFixture> provideFixtures() {
        return Stream.of(TESTS).flatMap(ValidationFixture::newStream);
    }
}
