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

import java.util.BitSet;

/**
 * @author leadpony
 */
class EmailMatcher extends AbstractMatcher {
    
    private static final int MAX_LOCAL_PART_CHARS = 64;
    private static final int MAX_DOMAIN_CHARS = 255;
    private static final int MAX_LABEL_CHARS = 63;

    private static final String ATOM_TEXT_CHARS = "!#$%&'*+-/=?^_`{|}~";
 
    private static final BitSet atomTextCharset;
    
    private final boolean internationalized;
    
    static {
        atomTextCharset = new BitSet(128);
        for (int i = 0; i < ATOM_TEXT_CHARS.length(); i++) {
            char c = ATOM_TEXT_CHARS.charAt(i);
            atomTextCharset.set(c);
        }
    }
    
    EmailMatcher(String value, boolean internationalized) {
        super(value);
        this.internationalized = internationalized;
    }

    private static boolean isLatinOrDigit(char c) {
        return (c >= '0' && c <= '9') ||
               (c >= 'A' &&  c <= 'Z') ||
               (c >= 'a' &&  c <= 'z');
    }
    
    private static boolean isNonWhiteSpaceControl(char c) {
        return (c >= 1 && c <= 9) ||
               (c == 11) ||
               (c == 12) ||
               (c >= 14 && c <= 31) ||
               (c == 127);
    }
    
    private boolean isQuotedPair(char c) {
        return (c >= 32 && c < 127) || isUnicodeNonAscii(c);
    }
    
    private boolean isUnicodeNonAscii(char c) {
        return c >= 128 && internationalized;
    }
    
    @Override
    protected void all() {
        localPart();
        if (next() == '@') {
            domainPart();
        } else {
            fail();
        }
    }
    
    private void localPart() {
        final int start = pos();
        comments();
        if (peek() == '\"') {
            quotedString();
        } else {
            dotAtom();
        }
        comments();
        int length = pos() - start;
        if (length > MAX_LOCAL_PART_CHARS) {
            fail();
        }
    }
    
    private void dotAtom() {
        atomText();
        while (peek() == '.') {
            next();
            atomText();
        }
    }
    
    private void atomText() {
        int length = 0;
        for (;;) {
            char c = peek();
            if (c == '@' || c == '.' || c == '(') {
                break;
            }
            next();
            if (isLatinOrDigit(c) || 
                atomTextCharset.get(c) ||
                isUnicodeNonAscii(c)) {
                length++;
            } else {
                fail();
            }                
        }
        if (length == 0) {
            fail();
        }
    }
    
    private void quotedString() {
        // Skips opening quote.
        char c = next();
        while ((c = next()) != '\"') {
            if (c == '\\') {
                if (isQuotedPair(next())) {
                } else {
                    fail();
                }
            } else if ((c >= 32 && c < 127) || isUnicodeNonAscii(c)) {
                // good
            } else {
                fail();
            }
        }
    }
    
    private void domainPart() {
        final int start = pos();
        comments();
        if (peek() == '[') {
            domainLiteral();
        } else {
            hostname();
        }
        comments();
        int length = pos() - start;
        if (length > MAX_DOMAIN_CHARS) {
            fail();
        }
    }
    
    private void domainLiteral() {
        // Opening bracket.
        char c = next();
        while ((c = next()) != ']') {
            if (isUnicodeNonAscii(c)) {
                // good
            } else if (c < 32 || c == '[' || c == '\\' || c == 127) {
                fail();
            }
        }
    }
    
    private void hostname() {
        domainLabel();
        while (hasNext() && peek() == '.') {
            next();
            domainLabel();
        }
    }
    
    private void domainLabel() {
        char c = peek();
        if (c == '-' || c == '.') {
            fail();
        }
        final int start = pos();
        while (hasNext()) {
            c = peek();
            if (c == '.' || c == '(') {
                break;
            } else {
                next();
                if (isLatinOrDigit(c) || isUnicodeNonAscii(c)) {
                    // good
                } else if (c == '-') {
                    if (peek() == '.') {
                        fail();
                    }
                } else {
                    fail();
                }
            }
        }
        int length = pos() - start;
        if (length == 0 || length > MAX_LABEL_CHARS) {
            fail();
        }
    }
    
    private void comments() {
        if (!hasNext() || peek() != '(') {
            return;
        }
        comment();
        while (hasNext() && peek() == '(') {
            comment();
        }
    }
    
    private void comment() {
        next();
        for (;;) {
            char c = peek();
            if (c == '(') {
                comment();
            } else {
                next();
                if (c == ')') {
                    break;
                } else if (c == '\\') {
                    if (!isQuotedPair(next())) {
                        fail();
                    }
                } else if (isNonWhiteSpaceControl(c) ||
                   (c >= 33 && c <= 39) ||
                   (c >= 42 && c <= 91) ||
                   (c >= 93 && c <= 126) ||
                   isUnicodeNonAscii(c)) {
                    // good
                } else {
                    fail();
                }
            }
        }
    }
}
