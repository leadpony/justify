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

package org.leadpony.justify.internal.base;

/**
 * Utility class operating on ASCII characters.
 *
 * @author leadpony
 */
public final class AsciiCode {

    private AsciiCode() {
    }

    /**
     * Determines if the specified character is an alphabet and an ASCII code.
     *
     * @param c the character to test.
     * @return {@code true} if the specified character is an alphabet.
     */
    public static boolean isAlphabetic(int c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    /**
     * Determines if the specified character is a digit and an ASCII code.
     *
     * @param c the character to test.
     * @return {@code true} if the specified character is a digit.
     */
    public static boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isAlphanumeric(int c) {
        return isAlphabetic(c) || isDigit(c);
    }

    public static boolean isHexDigit(int c) {
        return isDigit(c)
                || (c >= 'A' && c <= 'F')
                || (c >= 'a' && c <= 'f');
    }

    private static final int RADIX = 10;

    public static int hexDigitToValue(int c) {
        if (isDigit(c)) {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return RADIX + (c - 'a');
        } else if (c >= 'A' && c <= 'F') {
            return RADIX + (c - 'A');
        }
        throw new IllegalArgumentException();
    }
}
