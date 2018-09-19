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

package org.leadpony.justify.internal.keyword.assertion.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test cases for {@link Regex} class.
 * 
 * @author leadpony
 */
public class RegexTest {

    // System under test
    private static Regex sut;
    
    private static int index;
    
    @BeforeAll
    public static void setUpOnce() {
        sut = new Regex();
    }
    
    public static Stream<RegexFixture> provideFixtures() {
        return RegexFixture.load("regex.json");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideFixtures")
    public void test(RegexFixture fixture) {
        Assumptions.assumeTrue(++index >= 0);
        assertThat(sut.test(fixture.pattern())).isEqualTo(fixture.result());
    }
}
