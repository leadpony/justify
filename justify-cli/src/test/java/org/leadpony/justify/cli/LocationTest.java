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
import static org.assertj.core.api.Assertions.catchThrowable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author leadpony
 */
public class LocationTest {

    public static Stream<Arguments> validLocal() {
        return Stream.of(
                Arguments.of("test.json"),
                Arguments.of("target/test.json"),
                Arguments.of("../test.json"),
                Arguments.of("/root/test.json")
        );
    }

    @ParameterizedTest
    @MethodSource("validLocal")
    public void at_shouldReturnLocalResource(String string) throws MalformedURLException {
        Location actual = Location.at(string);
        assertThat(actual).isNotNull();
        assertThat(actual.toURL().getProtocol()).isEqualTo("file");
    }

    public static Stream<Arguments> validRemote() throws MalformedURLException {
        return Stream.of(
                Arguments.of("http://localhost/test.json", new URL("http://localhost/test.json")),
                Arguments.of("https://example.org/test.json", new URL("https://example.org/test.json"))
        );
    }

    @ParameterizedTest
    @MethodSource("validRemote")
    public void at_shouldReturnRemoteResource(String string, URL expected) throws MalformedURLException {
        Location actual = Location.at(string);
        assertThat(actual.toURL()).isEqualTo(expected);
    }

    public static Stream<Arguments> invalidRemote() throws MalformedURLException {
        return Stream.of(
                Arguments.of("")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidRemote")
    public void at_shouldThrowException(String string) {
        Throwable thrown = catchThrowable(()->{
            Location.at(string);
        });
        assertThat(thrown).isNotNull().isInstanceOf(IllegalArgumentException.class);
    }
}
