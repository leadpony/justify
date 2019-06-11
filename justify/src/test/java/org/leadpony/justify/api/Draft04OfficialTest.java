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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.internal.annotation.Spec;

/**
 * The official test suite tests for Draft-04.
 *
 * @author leadpony
 */
@Spec(SpecVersion.DRAFT_04)
public class Draft04OfficialTest extends AbstractOfficialTest {

    private static final String[] MANDATORY = {
            "additionalItems.json",
            "additionalProperties.json",
            "allOf.json",
            "anyOf.json",
            "default.json",
            "definitions.json",
            "dependencies.json",
            "enum.json",
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
            "ref.json",
            "refRemote.json",
            "required.json",
            "type.json",
            "uniqueItems.json",
    };

    public static Stream<Fixture> mandatory() {
        return generateFixtures(MANDATORY);
    }

    private static final String[] OPTIONAL = {
            "optional/bignum.json",
            // Draft-04 does not have the "regex" format.
            "optional/ecmascript-regex.json",
            "optional/format.json",
            "optional/zeroTerminatedFloats.json",
    };

    public static Stream<Fixture> optional() {
        return generateFixtures(OPTIONAL);
    }

    @ParameterizedTest
    @MethodSource("mandatory")
    public void testMandatory(Fixture fixture) {
        test(fixture);
    }

    @ParameterizedTest
    @MethodSource("optional")
    public void testOptional(Fixture fixture) {
        test(fixture);
    }
}
