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
package org.leadpony.justify.tests.spi;

import java.util.stream.Stream;

import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.tests.api.AbstractOfficialTest;

/**
 * A test for custom formats.
 *
 * @author leadpony
 */
@EnabledOnJre(JRE.JAVA_8)
@Spec(SpecVersion.DRAFT_07)
class CustomFormatAttributeTest extends AbstractOfficialTest {

    private static final String[] FILES = {
            "/org/leadpony/justify/tests/spi/customFormat.json",
    };

    static Stream<TestCase> testCases() {
        return generateFixtures(FILES);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    @Override
    public void test(TestCase test) {
        super.test(test);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    @Override
    public void testNegated(TestCase test) {
        super.testNegated(test);
    }
}
