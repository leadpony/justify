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

import static org.leadpony.justify.internal.keyword.assertion.format.Characters.isAsciiAlphanumeric;

/**
 * Matcher for hostnames.
 * 
 * @author leadpony
 * @see <a href="https://tools.ietf.org/html/rfc1123">RFC 1123</a>
 */
class HostnameMatcher extends FormatMatcher {
    
    static int MAX_LABEL_CHARS = 63;
    static final int MAX_DOMAIN_CHARS = 253;
    
    HostnameMatcher(CharSequence input) {
        super(input);
    }

    HostnameMatcher(CharSequence input, int start, int end) {
        super(input, start, end);
    }

    @Override
    public boolean all() {
        final int start = pos();
        subdomain();
        int length = pos() - start;
        if (length > MAX_DOMAIN_CHARS) {
            return false;
        }
        return true;
    }
    
    private void subdomain() {
        label();
        while (hasNext()) {
            if (next() == '.') {
                label();
            } else {
                fail();
            };
        }
    }
    
    /**
     * Parses the domain label.
     * <p>
     * Note that RFC 1123 relaxed the restriction on on the first character 
     * to allow either a letter or a digit.
     * </p>
     */
    private void label() {
        final int start = pos();
        int c = next();
        if (!checkFirstLabelLetter(c)) {
            fail();
        }
        while (hasNext()) {
            if (peek() == '.') {
                break;
            }
            c = next();
            if (!checkLabelLetter(c)) {
                fail();
            }
        }
        if (c == '-') {
            fail();
        }
        int length = pos() - start;
        if (length == 0 || length > MAX_LABEL_CHARS) {
            fail();
        }
    }
    
    protected boolean checkFirstLabelLetter(int c) {
        return isAsciiAlphanumeric(c);
    }

    protected boolean checkLabelLetter(int c) {
        return isAsciiAlphanumeric(c) || c == '-';
    }
}
