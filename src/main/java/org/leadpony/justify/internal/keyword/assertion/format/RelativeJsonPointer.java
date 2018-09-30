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
 * Format attribute representing "relative-json-pointer" attribute.
 * 
 * @author leadpony
 * 
 */
class RelativeJsonPointer implements StringFormatAttribute {
    
    private final JsonPointer jsonPointer = new JsonPointer();

    @Override
    public String name() {
        return "relative-json-pointer";
    }

    @Override
    public boolean test(String value) {
        final int length = value.length();
        if (value.isEmpty()) {
            return false;
        }
        char c = value.charAt(0);
        if (c == '0') {
            return testAfterPrefix(value, 1);
        } else if (c >= '1' && c <= '9') {
            int offset = 0;
            while (++offset < length) {
                c = value.charAt(offset);
                if (!AsciiCode.isDigit(c)) {
                    break;
                }
            }
            return testAfterPrefix(value, offset);
        } else {
            return false;
        }
    }
    
    private boolean testAfterPrefix(String value, int offset) {
        final int length = value.length();
        if (offset >= length) {
            return true;
        }
        char c = value.charAt(offset);
        if (c == '#') {
            return offset + 1 >= length;
        } else if (c == '/') {
            return jsonPointer.test(value.substring(offset));
        } else {
            return false;
        }
    }
}
