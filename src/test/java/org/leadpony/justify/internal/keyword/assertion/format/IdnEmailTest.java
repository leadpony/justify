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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author leadpony
 */
public class IdnEmailTest {

    private static Email sut;
    
    @BeforeAll
    public static void setUpOnce() {
        sut = new IdnEmail();
    }
    
    public static Stream<Fixture> provideEmails() {
        return EmailTest.provideEmails();
    };

    public static Stream<Fixture> provideIdnEmails() {
        return EmailTest.provideIdnEmails();
    };

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideEmails")
    public void testEmail(Fixture fixture) {
        assertThat(sut.test(fixture.value())).isEqualTo(fixture.result());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideIdnEmails")
    public void testIdnEmail(Fixture fixture) {
        assertThat(sut.test(fixture.value())).isEqualTo(fixture.result());
    }
}
