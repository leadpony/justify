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

import static org.leadpony.justify.internal.base.AsciiCode.isAlphanumeric;

import java.util.BitSet;

/**
 * Matcher for email addresses.
 *
 * @author leadpony
 */
class EmailMatcher extends FormatMatcher {

    static final int MAX_LOCAL_PART_CHARS = 64;
    static final String ATOM_TEXT_CHARS = "!#$%&'*+-/=?^_`{|}~";

    @SuppressWarnings("serial")
    static final BitSet ATOM_TEXT_CHARSET = new BitSet(128) {
        {
            for (int i = 0; i < ATOM_TEXT_CHARS.length(); i++) {
                set(ATOM_TEXT_CHARS.charAt(i));
            }
        }
    };

    EmailMatcher(CharSequence input) {
        super(input);
    }

    @Override
    public boolean all() {
        localPart();
        if (next() == '@') {
            domainPart();
            return true;
        } else {
            return false;
        }
    }

    private void localPart() {
        final int start = pos();
        zeroOrMoreComments();
        if (peek() == '\"') {
            quotedString();
        } else {
            dotAtom();
        }
        zeroOrMoreComments();
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
            int c = peek();
            if (c == '@' || c == '.' || c == '(' || isWhiteSpace(c) || c == '\r') {
                break;
            }
            next();
            if (checkAtomLetter(c)) {
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
        int c = next();
        while ((c = next()) != '\"') {
            if (c == '\\') {
                if (!checkQuotedLetter(next())) {
                    fail();
                }
            } else if (isWhiteSpace(c) || c == '\r') {
                foldingWhiteSpace();
            } else if (!checkQuotedLetter(c)) {
                fail();
            }
        }
    }

    private void domainPart() {
        final int start = pos();
        zeroOrMoreComments();
        if (peek() == '[') {
            domainLiteral();
        } else {
            hostname();
        }
        zeroOrMoreComments();
        if (hasNext()) {
            fail();
        }
        int length = pos() - start;
        if (length > HostnameMatcher.MAX_DOMAIN_CHARS) {
            fail();
        }
    }

    private void domainLiteral() {
        // Skips opening bracket.
        int c = next();
        while ((c = next()) != ']') {
            if (isWhiteSpace(c) || c == '\r') {
                foldingWhiteSpace();
            } else if (!checkDomainLiteralLetter(c)) {
                fail();
            }
        }
    }

    private void hostname() {
        final int start = pos();
        while (hasNext()) {
            int c = peek();
            if (c == '(' || isWhiteSpace(c) || c == '\r') {
                break;
            } else {
                next();
            }
        }
        createHostnameMatcher(start, pos()).all();
    }

    /**
     * Zero or more comments.
     */
    private void zeroOrMoreComments() {
        foldingWhiteSpace();
        if (hasNext() && peek() == '(') {
            comment();
            foldingWhiteSpace();
            while (hasNext() && peek() == '(') {
                comment();
                foldingWhiteSpace();
            }
        }
    }

    private void comment() {
        // Skips opening parenthesis.
        next();
        for (;;) {
            int c = peek();
            if (c == '(') {
                comment();
            } else if (isWhiteSpace(c) || c == '\r') {
                // Folding white space appears in the comment.
                foldingWhiteSpace();
            } else {
                next();
                if (c == ')') {
                    break;
                } else if (c == '\\') {
                    if (!checkQuotedLetter(next())) {
                        fail();
                    }
                } else if (!checkCommentLetter(c)) {
                    fail();
                }
            }
        }
    }

    /**
     * Folding white space (optional).
     */
    private void foldingWhiteSpace() {
        zeroOrMoreWhiteSpaces();
        if (hasNext() && peek() == '\r') {
            if (next() != 'n') {
                fail();
            }
            if (isWhiteSpace(next())) {
                zeroOrMoreWhiteSpaces();
            } else {
                fail();
            }
        }
    }

    private void zeroOrMoreWhiteSpaces() {
        while (hasNext() && isWhiteSpace(peek())) {
            next();
        }
    }

    protected boolean checkQuotedLetter(int c) {
        return c >= 32 && c < 127;
    }

    protected boolean checkAtomLetter(int c) {
        return isAlphanumeric(c) || ATOM_TEXT_CHARSET.get(c);
    }

    protected boolean checkDomainLiteralLetter(int c) {
        return (c >= 33 && c <= 90)
                || (c >= 94 && c <= 126);
    }

    protected boolean checkCommentLetter(int c) {
        return isNonWhiteSpaceControl(c)
                || (c >= 33 && c <= 39)
                || (c >= 42 && c <= 91)
                || (c >= 93 && c <= 126);
    }

    protected FormatMatcher createHostnameMatcher(int start, int end) {
        return new HostnameMatcher(input(), start, end);
    }

    private static boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\t';
    }

    private static boolean isNonWhiteSpaceControl(int c) {
        return (c >= 1 && c <= 9)
                || (c == 11)
                || (c == 12)
                || (c >= 14 && c <= 31)
                || (c == 127);
    }
}
