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
package org.leadpony.justify.internal.keyword.assertion.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test for Base64 content encoding.
 *
 * @author leadpony
 */
public class Base64Test {

    public static Stream<Arguments> fixtures() {
        return Stream.of(
                Arguments.of("", true, bytes("")),
                Arguments.of("Zg==", true, bytes("f")),
                Arguments.of("Zm8=", true, bytes("fo")),
                Arguments.of("Zm9v", true, bytes("foo")),
                Arguments.of("Zm9vYg==", true, bytes("foob")),
                Arguments.of("Zm9vYmE=", true, bytes("fooba")),
                Arguments.of("Zm9vYmFy", true, bytes("foobar")),

                Arguments.of("====", false, null),
                Arguments.of("Zg", false, null),
                Arguments.of("Zm8", false, null),
                Arguments.of("Zm9vYg", false, null),
                Arguments.of("4rdHFh%2BHYoS8oLdVvbUzEVqB8Lvm7kSPnuwF0AAABYQ%3D", false, null));
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    public void canDecodeShouldReturnExpectedResult(String src, boolean valid, byte[] decoded) {
        Base64 base64 = new Base64();
        boolean actual = base64.canDecode(src);

        assertThat(actual).isEqualTo(valid);
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    public void decodeShouldDecodeString(String src, boolean valid, byte[] decoded) {
        Base64 base64 = new Base64();
        if (valid) {
            byte[] actual = base64.decode(src);
            assertThat(actual).isEqualTo(decoded);
        } else {
            Throwable thrown = catchThrowable(() -> base64.decode(src));
            assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static byte[] bytes(String str) {
        return str.getBytes(StandardCharsets.US_ASCII);
    }
}
