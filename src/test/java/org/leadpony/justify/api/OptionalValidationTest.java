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

import java.util.stream.Stream;

/**
 * Optional tests provided by the JSON Schema Test Suite.
 *
 * @author leadpony
 */
public class OptionalValidationTest extends BaseValidationTest {

    private static final String[] TESTS = {
            "/org/json_schema/tests/draft7/optional/bignum.json",
            "/org/json_schema/tests/draft7/optional/content.json",
            "/org/json_schema/tests/draft7/optional/ecmascript-regex.json",
            "/org/json_schema/tests/draft7/optional/zeroTerminatedFloats.json",
    };

    public static Stream<ValidationFixture> provideFixtures() {
        return Stream.of(TESTS).flatMap(ValidationFixture::newStream);
    }
}
