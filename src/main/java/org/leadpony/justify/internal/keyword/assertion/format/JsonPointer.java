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
 * Format attribute representing "json-pointer" attribute.
 * 
 * @author leadpony
 * 
 * @see <a href="https://tools.ietf.org/html/rfc6901">
 * "JavaScript Object Notation (JSON) Pointer", RFC 6901</a>
 */
class JsonPointer implements StringFormatAttribute {

    @Override
    public String name() {
        return "json-pointer";
    }

    @Override
    public boolean test(String value) {
        if (value.isEmpty()) {
            return true;
        }
        char c = value.charAt(0);
        if (c != '/') {
            return false;
        }
        for (int i = 1; i < value.length(); i++) {
            c = value.charAt(i);
            if (c == '~') {
                if (i + 1 >= value.length()) {
                    return false;
                }
                c = value.charAt(++i);
                if (c != '0' && c != '1') {
                    return false;
                }
            }
        }
        return true;
    }

}
