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
 * Matcher for IPv6 addresses.
 * 
 * @author leadpony
 * 
 * @see RFC 4291, section 2.2
 */
class Ipv6Matcher extends Ipv4Matcher {
    
    private static final int MAX_PIECES = 8;

    /**
     * Constructs this matcher.
     * 
     * @param input the input character sequence.
     */
    Ipv6Matcher(CharSequence input) {
        super(input);
    }

    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     * @param start the start index, inclusive.
     * @param end the end index, exclusive.
     */
    Ipv6Matcher(CharSequence input, int start, int end) {
        super(input, start, end);
    }
    
    @Override
    boolean all() {
        int pieces = 0;
        boolean compressed = false;

        if (hasNext(':')) {
            next();
            if (hasNext(':')) {
                next();
                compressed = true;
                pieces++;
            } else {
                return false;
            }
        }
        
        while (pieces < MAX_PIECES && h16()) {
            pieces++;
            if (!hasNext()) {
                break;
            } else if (next() == ':') {
                if (hasNext(':')) {
                    next();
                    if (compressed) {
                        return false;
                    }
                    compressed = true;
                    pieces++;
                }
            } else {
                return false;
            }
        }
        
        if (hasNext()) {
            if (ipv4address()) {
                pieces += 2;
            } else {
                return false;
            }
        }
        
        if (compressed) {
            return pieces <= MAX_PIECES;
        } else {
            return pieces == MAX_PIECES;
        }
    }
    
    boolean ipv4address() {
        return dottedQuad();
    }
    
    boolean h16() {
        final int mark = pos();
        if (hasNext() && AsciiCode.isHexDigit(peek())) {
            next();
            int digits = 1;
            while (digits < 4 && hasNext() && AsciiCode.isHexDigit(peek())) {
                next();
                digits++;
            }
            if (!hasNext() || peek() == ':') {
                return true;
            } else {
                return backtrack(mark);
            }
        }
        return false;
    }
}
