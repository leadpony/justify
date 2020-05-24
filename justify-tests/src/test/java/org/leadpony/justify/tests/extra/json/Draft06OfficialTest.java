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
package org.leadpony.justify.tests.extra.json;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;

/**
 * The official test suite tests for Draft-06.
 *
 * @author leadpony
 */
@Spec(SpecVersion.DRAFT_06)
public class Draft06OfficialTest extends AbstractOfficialTest {

    private static final String[] MANDATORY = {
            "additionalItems.json",
            "additionalProperties.json",
            "allOf.json",
            "anyOf.json",
            "boolean_schema.json",
            "const.json",
            "contains.json",
            "default.json",
            "definitions.json",
            "dependencies.json",
            "enum.json",
            "exclusiveMaximum.json",
            "exclusiveMinimum.json",
            "items.json",
            "maximum.json",
            "maxItems.json",
            "maxLength.json",
            "maxProperties.json",
            "minimum.json",
            "minItems.json",
            "minLength.json",
            "minProperties.json",
            "multipleOf.json",
            "not.json",
            "oneOf.json",
            "pattern.json",
            "patternProperties.json",
            "properties.json",
            "propertyNames.json",
            "ref.json",
            "refRemote.json",
            "required.json",
            "type.json",
            "uniqueItems.json",
    };

    public static Stream<TestCase> mandatory() {
        return generateTestCases(MANDATORY);
    }

    private static final String[] OPTIONAL = {
            "optional/bignum.json",
            // Draft-06 does not have the "regex" format.
            "optional/ecmascript-regex.json",
            "optional/format.json",
            "optional/non-bmp-regex.json",
    };

    public static Stream<TestCase> optional() {
        return generateTestCases(OPTIONAL);
    }

    @ParameterizedTest
    @MethodSource("mandatory")
    public void testMandatory(TestCase test) {
        test(test);
    }

    @ParameterizedTest
    @MethodSource("optional")
    public void testOptional(TestCase test) {
        test(test);
    }
}
