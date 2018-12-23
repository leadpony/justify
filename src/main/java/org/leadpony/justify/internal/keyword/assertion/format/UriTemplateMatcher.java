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
 * Matcher for URI template conformant to RFC 6570.
 * 
 * @author leadpony
 */
class UriTemplateMatcher extends FormatMatcher {

    /**
     * Constructs this matcher.
     * 
     * @param input the input character sequence.
     */
    UriTemplateMatcher(CharSequence input) {
        super(input);
    }

    @Override
    boolean all() {
        while (hasNext()) {
            if (literals() || expression()) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    boolean literals() {
        int c = peek();
        if (c == 0x21 || (0x23 <= c && c <= 0x24) || c == 0x26 ||
           (0x28 <= c && c <= 0x3b) || c == 0x3d ||
           (0x3f <= c && c <= 0x5b) ||
            c == 0x5d || c == 0x5f ||
           (0x61 <= c && c <= 0x7a) ||c == 0x7e ||
            UriCode.isUcschar(c) || UriCode.isIprivate(c)) {
            next();
            return true;
        } else if (pctEncoded()) {
            return true;
        }
        return false;
    }
    
    boolean expression() {
        if (!hasNext('{')) {
            return false;
        }
        next();
        if (hasNext()) {
            operator();
            if (variableList()) {
                if (hasNext('}')) {
                    next();
                    return true;
                }
            }
        }
        return fail();
    }
    
    boolean operator() {
        switch (peek()) {
        case '+':
        case '#':
        case '.':
        case '/':
        case ';':
        case '?':
        case '&':
        // reserved
        case '=':
        case ',':
        case '!':
        case '@':
        case '|':
            next();
            return true;
        default:
            return false;
        }
    }
    
    boolean variableList() {
        if (!hasNext() || !varspec()) {
            return false;
        }
        while (hasNext(',')) {
            next();
            if (!varspec()) {
                return fail();
            }
        }
        return true;
    }
    
    boolean varspec() {
        if (varname()) {
            modifierLevel4(); // optional
            return true;
        }
        return false;
    }
    
    boolean varname() {
        if (!varchar()) {
            return false;
        }
        while (hasNext()) {
            if (hasNext('.')) {
                next();
                if (!varchar()) {
                    return fail();
                }
            } else if (!varchar()) {
                break;
            }
        }
        return true;
    }
    
    boolean varchar() {
        if (hasNext()) {
            int c = peek();
            if (AsciiCode.isAlphanumeric(c) || c == '_') {
                next();
                return true;
            } else if (pctEncoded()) {
                return true;
            }
        }
        return false;
    }
    
    boolean modifierLevel4() {
        return prefix() || explode();
    }
    
    boolean prefix() {
        if (hasNext(':')) {
            next();
            if (maxLength()) {
                return true;
            } else {
                return fail();
            }
        }
        return false;
    }
    
    boolean maxLength() {
        if (!hasNext()) {
            return false;
        }
        int c = next();
        if (c == '0' || !AsciiCode.isDigit(c)) {
            return false;
        }
        int digits = 1;
        while (hasNext() && AsciiCode.isDigit(peek())) {
            if (++digits < 5) {
                next();
            } else {
                return false;
            }
        }
        return true;
    }
    
    boolean explode() {
        if (hasNext('*')) {
            next();
            return true;
        } else {
            return false;
        }
    }
    
    boolean pctEncoded() {
        if (!hasNext('%')) {
            return false;
        }    
        next();
        if (hasNext() && AsciiCode.isHexDigit(next()) &&
            hasNext() && AsciiCode.isHexDigit(next())) {
            return true;
        }
        return fail();
    }
}
