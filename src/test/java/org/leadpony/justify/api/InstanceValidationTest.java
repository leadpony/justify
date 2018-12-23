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

package org.leadpony.justify.api;

import java.util.stream.Stream;

/**
 * Test suite provided by json-schema.org.
 * 
 * @author leadpony
 */
public class InstanceValidationTest extends BaseValidationTest {

    private static final String[] TESTS = {
            "/org/json_schema/tests/draft7/additionalItems.json",
            "/org/json_schema/tests/draft7/additionalProperties.json",
            "/org/json_schema/tests/draft7/allOf.json",
            "/org/json_schema/tests/draft7/anyOf.json",
            "/org/json_schema/tests/draft7/boolean_schema.json",
            "/org/json_schema/tests/draft7/const.json",
            "/org/json_schema/tests/draft7/contains.json",
            "/org/json_schema/tests/draft7/default.json",
            "/org/json_schema/tests/draft7/definitions.json",
            "/org/json_schema/tests/draft7/dependencies.json",
            "/org/json_schema/tests/draft7/enum.json",
            "/org/json_schema/tests/draft7/exclusiveMaximum.json",
            "/org/json_schema/tests/draft7/exclusiveMinimum.json",
            "/org/json_schema/tests/draft7/if-then-else.json",
            "/org/json_schema/tests/draft7/items.json",
            "/org/json_schema/tests/draft7/maximum.json",
            "/org/json_schema/tests/draft7/maxItems.json",
            "/org/json_schema/tests/draft7/maxLength.json",
            "/org/json_schema/tests/draft7/maxProperties.json",
            "/org/json_schema/tests/draft7/minimum.json",
            "/org/json_schema/tests/draft7/minItems.json",
            "/org/json_schema/tests/draft7/minLength.json",
            "/org/json_schema/tests/draft7/minProperties.json",
            "/org/json_schema/tests/draft7/multipleOf.json",
            "/org/json_schema/tests/draft7/not.json",
            "/org/json_schema/tests/draft7/oneOf.json",
            "/org/json_schema/tests/draft7/pattern.json",
            "/org/json_schema/tests/draft7/patternProperties.json",
            "/org/json_schema/tests/draft7/properties.json",
            "/org/json_schema/tests/draft7/propertyNames.json",
            "/org/json_schema/tests/draft7/ref.json",
            "/org/json_schema/tests/draft7/required.json",
            "/org/json_schema/tests/draft7/type.json",
            "/org/json_schema/tests/draft7/uniqueItems.json",
    };
    
    public static Stream<ValidationFixture> provideFixtures() {
        return Stream.of(TESTS).flatMap(ValidationFixture::newStream);
    }
}
