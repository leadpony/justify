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
 * Matcher for IPv4 addresses.
 * 
 * @author leadpony
 */
class Ipv4Matcher extends FormatMatcher {

    /**
     * Constructs this matcher.
     * 
     * @param input the input character sequence.
     */
    Ipv4Matcher(CharSequence input) {
        super(input);
    }

    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     * @param start the start index, inclusive.
     * @param end the end index, exclusive.
     */
    Ipv4Matcher(CharSequence input, int start, int end) {
        super(input, start, end);
    }

    @Override
    boolean all() {
        return dottedQuad();
    }
    
    boolean dottedQuad() {
        final int mark = pos();
        if (decbyte() && next() == '.' &&
            decbyte() && next() == '.' &&
            decbyte() && next() == '.' &&
            decbyte() && !hasNext()) {
            return true;
        } else {
            return backtrack(mark);
        }
    }
    
    boolean decbyte() {
        if (!hasNext()) {
            return false;
        }
        // 1st digit
        int c = next();
        if (!AsciiCode.isDigit(c)) {
            return false;
        }
        int value = digitToValue(c);
        
        if (hasNext() && peek() != '.') {
            // 2nd digit
            c = next();
            if (!AsciiCode.isDigit(c)) {
                return false;
            }
            value = value * 10 + digitToValue(c);

            if (hasNext() && peek() != '.') {
                // 3rd digit
                c = next();
                if (!AsciiCode.isDigit(c)) {
                    return false;
                }
                value = value * 10 + digitToValue(c);
            }

            if (hasNext() && peek() != '.') {
                return false;
            }

            return value <= 255;
        }
        return true;
    }
    
    static int digitToValue(int codePoint) {
        return codePoint - '0';
    }
}
