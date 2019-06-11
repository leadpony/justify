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

package org.leadpony.justify.internal.keyword.assertion.format;

import java.util.BitSet;

import org.leadpony.justify.internal.base.AsciiCode;

/**
 * @author leadpony
 */
final class UriCode {

    private UriCode() {
    }

    private static final int[] GEN_DELIMS = {':', '/', '?', '#', '[', ']', '@'};
    private static final int[] SUB_DELIMS = {'!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '='};

    private static final BitSet GEN_DELIMS_SET = new BitSet();
    private static final BitSet SUB_DELIMS_SET = new BitSet();
    private static final BitSet RESERVED = new BitSet();

    static {
        for (int c : GEN_DELIMS) {
            GEN_DELIMS_SET.set(c);
        }
        for (int c : SUB_DELIMS) {
            SUB_DELIMS_SET.set(c);
        }
        RESERVED.or(GEN_DELIMS_SET);
        RESERVED.or(SUB_DELIMS_SET);
    }

    static boolean isReserved(int c) {
        return RESERVED.get(c);
    }

    static boolean isSubDelim(int c) {
        return SUB_DELIMS_SET.get(c);
    }

    static boolean isUnreserved(int c) {
        return AsciiCode.isAlphanumeric(c)
                || c == '-' || c == '.' || c == '_' || c == '~';
    }

    static boolean isIunreserved(int c) {
        return isUnreserved(c) || UriCode.isUcschar(c);
    }

    static boolean isUcschar(int c) {
        return (0xA0 <= c && c <= 0xD7FF)
                || (0xF900 <= c && c <= 0xFDCF)
                || (0xFDF0 <= c && c <= 0xFFEF)
                || (0x10000 <= c && c <= 0x1FFFD)
                || (0x20000 <= c && c <= 0x2FFFD)
                || (0x30000 <= c && c <= 0x3FFFD)
                || (0x40000 <= c && c <= 0x4FFFD)
                || (0x50000 <= c && c <= 0x5FFFD)
                || (0x60000 <= c && c <= 0x6FFFD)
                || (0x70000 <= c && c <= 0x7FFFD)
                || (0x80000 <= c && c <= 0x8FFFD)
                || (0x90000 <= c && c <= 0x9FFFD)
                || (0xA0000 <= c && c <= 0xAFFFD)
                || (0xB0000 <= c && c <= 0xBFFFD)
                || (0xC0000 <= c && c <= 0xCFFFD)
                || (0xD0000 <= c && c <= 0xDFFFD)
                || (0xE0000 <= c && c <= 0xEFFFD);
    }

    static boolean isIprivate(int c) {
        return (0xE000 <= c && c <= 0xF8FF)
                || (0xF0000 <= c && c <= 0xFFFFD)
                || (0x100000 <= c && c <= 0x10FFFD);
    }
}
