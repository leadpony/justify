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

/**
 * @author leadpony
 */
class Ipv4Matcher extends AbstractMatcher {

    Ipv4Matcher(String value) {
        super(value);
    }

    @Override
    protected void all() {
        int number;
        for (int i = 0; i < 3; i++) {
            number = requireNumber(next());
            char c = next();
            if (c != '.') {
                number = number * 10 + requireNumber(c);
                c = next();
                if (c != '.') {
                    number = number * 10 + requireNumber(c);
                    if (next() != '.') {
                        fail();
                    }
                }
            }
            if (number > 255) {
                fail();
            }
        }
        // final decbyte
        number = requireNumber(next());
        if (hasNext()) {
            number = number * 10 + requireNumber(next());
            if (hasNext()) {
                number = number * 10 + requireNumber(next());
                if (hasNext()) {
                    fail();
                }
            }
        }
        if (number > 255) {
            fail();
        }
    }
    
    private static int requireNumber(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else {
            fail();
            // Never reach here
            assert false;
            return -1;
        }
    }
}
