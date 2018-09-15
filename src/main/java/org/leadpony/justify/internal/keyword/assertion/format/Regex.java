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
 * Format attribute representing "regex" attribute.
 * 
 * @author leadpony
 * @see <a href="http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-262.pdf">
 *      ECMA 262 specification</a>
 */
public class Regex implements StringFormatAttribute {

    @Override
    public String name() {
        return "regex";
    }

    @Override
    public boolean test(String value) {
        return testUnicode(value);
    }
    
    public boolean test(String value, String flags) {
        if (flags.indexOf('u') >= 0) {
            return testUnicode(value);
        } else {
            return testNonUnicode(value);
        }
    }
    
    private boolean testUnicode(String value) {
        return new UnicodeRegExpMatcher(value).matches();
    }

    private boolean testNonUnicode(String value) {
        return new NonUnicodeRegExpMatcher(value).matches();
    }
}
