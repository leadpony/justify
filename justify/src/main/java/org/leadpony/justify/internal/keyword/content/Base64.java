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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.Base64.Decoder;

import org.leadpony.justify.internal.base.text.AsciiCode;
import org.leadpony.justify.spi.ContentEncodingScheme;

/**
 * Content encoding scheme representing "base64".
 *
 * @author leadpony
 */
final class Base64 implements ContentEncodingScheme {

    static final Base64 INSTANCE = new Base64();

    private Base64() {
    }

    @Override
    public String name() {
        return "base64";
    }

    @Override
    public boolean canDecode(String src) {
        requireNonNull(src, "src");
        final int length = src.length();
        if ((length % 4) != 0) {
            return false;
        }
        int i = 0;
        while (i < length) {
            int c = src.charAt(i++);
            if (c == '=') {
                int pads = 1;
                while (i < length) {
                    c = src.charAt(i++);
                    if (c != '=' || ++pads > 3) {
                        return false;
                    }
                }
                break;
            }
            if (!AsciiCode.isAlphanumeric(c) && c != '+' && c != '/') {
                return false;
            }
        }
        return true;
    }

    @Override
    public byte[] decode(String src) {
        requireNonNull(src, "src");
        if ((src.length() % 4) != 0) {
            throw new IllegalArgumentException();
        }
        return getDecoder().decode(src);
    }

    private static Decoder getDecoder() {
        return java.util.Base64.getDecoder();
    }
}
