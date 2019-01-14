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

/**
 * Matcher for IRI conformant to RFC 3987.
 * 
 * @author leadpony
 * 
 * @see <a href="https://tools.ietf.org/html/rfc3987">
 * "Internationalized Resource Identifiers (IRIs)", RFC 3987</a>
 */
class IriMatcher extends UriMatcher {

    /**
     * Constructs this matcher.
     * 
     * @param input the input character sequence.
     */
    IriMatcher(CharSequence input) {
        super(input);
    }
    
    @Override
    boolean pchar() {
        if (hasNext()) {
            int c = peek();
            if (UriCode.isIunreserved(c) || UriCode.isSubDelim(c) || c == ':' || c == '@') {
                next();
                return true;
            }
            return pctEncoded();
        }
        return false;
    }
    
    @Override
    boolean query() {
        while (hasNext() && peek() != '#') {
            if (pchar()) {
                continue;
            }
            int c = peek();
            if (c == '/' || c == '?' || UriCode.isIprivate(c)) {
                next();
            } else {
                return fail();
            }
        }
        return true;
    }

    @Override
    boolean unreserved() {
        if (hasNext() && UriCode.isIunreserved(peek())) {
            next();
            return true;
        }
        return false;
    }
}
