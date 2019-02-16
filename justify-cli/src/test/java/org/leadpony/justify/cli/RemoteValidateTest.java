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
package org.leadpony.justify.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Validation test with remote resources.
 *
 * @author leadpony
 */
public class RemoteValidateTest {

    private static final int PORT = 1234;
    private static RemoteServer server;

    @BeforeAll
    public static void setUpOnce() throws Exception {
        server = new RemoteServer(PORT);
    }

    @AfterAll
    public static void tearDownOnce() throws Exception {
        server.shutdown();
    }

    /**
     * Provides test fixgtures.
     *
     * @return the stream of test fixtures.
     */
    public static Stream<Fixture> fixtures() {
        return ValidateTest.fixtures();
    }

    private static int index = 1;

    @ParameterizedTest
    @MethodSource("fixtures")
    public void test(Fixture fixture) {
        System.out.println("[" + index++ + "] " + fixture);
        Launcher launcher = new Launcher();
        Status actual = launcher.launch(fixture.remoteArgs());
        System.out.println();
        assertThat(actual).isEqualTo(fixture.getExpectedStatus());
    }
}
