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
 * Optional format tests provided by the JSON Schema Test Suite.
 * 
 * @author leadpony
 */
public class FormatValidationTest extends BaseValidationTest {

    private static final String[] TESTS = {
            "/org/json_schema/tests/draft7/optional/format/date.json",
            "/org/json_schema/tests/draft7/optional/format/date-time.json",
            "/org/json_schema/tests/draft7/optional/format/email.json",
            "/org/json_schema/tests/draft7/optional/format/hostname.json",
            "/org/json_schema/tests/draft7/optional/format/idn-email.json",
            "/org/json_schema/tests/draft7/optional/format/idn-hostname.json",
            "/org/json_schema/tests/draft7/optional/format/ipv4.json",
            "/org/json_schema/tests/draft7/optional/format/ipv6.json",
            //"/org/json_schema/tests/draft7/optional/format/iri.json",
            "/org/json_schema/tests/draft7/optional/format/iri-reference.json",
            "/org/json_schema/tests/draft7/optional/format/json-pointer.json",
            "/org/json_schema/tests/draft7/optional/format/regex.json",
            "/org/json_schema/tests/draft7/optional/format/relative-json-pointer.json",
            "/org/json_schema/tests/draft7/optional/format/time.json",
            "/org/json_schema/tests/draft7/optional/format/uri.json",
            "/org/json_schema/tests/draft7/optional/format/uri-reference.json",
    };
    
    public static Stream<ValidationFixture> provideFixtures() {
        return Stream.of(TESTS).flatMap(ValidationFixture::newStream);
    }
}
