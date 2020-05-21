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
package org.leadpony.justify.internal.keyword.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test for Base64 content encoding.
 *
 * @author leadpony
 */
public class Base64Test {

    /**
     * @author leadpony
     */
    enum Base64TestCase {
        EMPTY("", true, bytes("")),
        F("Zg==", true, bytes("f")),
        FO("Zm8=", true, bytes("fo")),
        FOO("Zm9v", true, bytes("foo")),
        FOOB("Zm9vYg==", true, bytes("foob")),
        FOOBA("Zm9vYmE=", true, bytes("fooba")),
        FOOBAR("Zm9vYmFy", true, bytes("foobar")),

        EQUAL_ONLY("====", false, null),
        F_SHORT("Zg", false, null),
        FO_SHORT("Zm8", false, null),
        FOOB_SHORT("Zm9vYg", false, null),
        INVALD_LETTERS("4rdHFh%2BHYoS8oLdVvbUzEVqB8Lvm7kSPnuwF0AAABYQ%3D", false, null);

        final String src;
        final boolean valid;
        final byte[] decoded;

        Base64TestCase(String src, boolean valid, byte[] decoded) {
            this.src = src;
            this.valid = valid;
            this.decoded = decoded;
        }

        private static byte[] bytes(String string) {
            return string.getBytes(StandardCharsets.US_ASCII);
        }
    }

    @ParameterizedTest
    @EnumSource(Base64TestCase.class)
    public void canDecodeShouldReturnExpectedResult(Base64TestCase test) {
        Base64 base64 = Base64.INSTANCE;
        boolean actual = base64.canDecode(test.src);

        assertThat(actual).isEqualTo(test.valid);
    }

    @ParameterizedTest
    @EnumSource(Base64TestCase.class)
    public void decodeShouldDecodeString(Base64TestCase test) {
        Base64 base64 = Base64.INSTANCE;
        if (test.valid) {
            byte[] actual = base64.decode(test.src);
            assertThat(actual).isEqualTo(test.decoded);
        } else {
            Throwable thrown = catchThrowable(() -> base64.decode(test.src));
            assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
