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
 * Matcher for URI conformant to RFC 3986.
 * 
 * @author leadpony
 * 
 * @see <a href="https://tools.ietf.org/html/rfc3986">
 * "Uniform Resource Identifier (URI): Generic Syntax", STD 66, RFC 3986</a>
 */
class UriMatcher extends FormatMatcher {
    
    /**
     * Constructs this matcher.
     * 
     * @param input the input character sequence.
     */
    UriMatcher(CharSequence input) {
        super(decodeAllUnreserved(input));
    }

    @Override
    boolean all() {
        return uri();
    }
    
    boolean uri() {
        if (scheme() && hasNext(':')) {
            next();
            if (hierPart()) {
                if (hasNext('?')) {
                    next();
                    if (!query()) {
                        return false;
                    }
                }
                if (hasNext('#')) {
                    next();
                    if (!fragment()) {
                        return false;
                    }
                }
                return !hasNext();
            }
        }
        return false;
    }
    
    boolean scheme() {
        final int mark = pos();
        if (hasNext()) {
            int c = next();
            if (AsciiCode.isAlphabetic(c)) {
                while(hasNext() && peek() != ':') {
                    c = next();
                    if (AsciiCode.isAlphanumeric(c) || 
                        c == '+' || c == '-' || c == '.') {
                        continue;
                    } else {
                        return backtrack(mark);
                    }
                }
                return true;
            }
        }
        return backtrack(mark);
    }

    boolean hierPart() {
        if (hasNext('/')) {
            final int mark = pos();
            next();
            if (hasNext('/')) {
                next();
                return authority() && pathAbempty();
            }
            backtrack(mark);
            return pathAbsolute();
        }
        return pathRootless() || pathEmpty();
    }

    boolean authority() {
        if (!hasNext()) {
            return false;
        }
        if (userinfo()) {
            // Skips '@'
            next();
        }
        if (host()) {
            if (hasNext(':')) {
                next();
                port();
            }
            return true;
        }
        return false;
    }
    
    boolean userinfo() {
        final int start = pos();
        while (hasNext()) {
            if (unreserved() || pctEncoded()) {
                continue;
            }
            int c = peek();
            if (c == '@') {
                return true;
            } else if (UriCode.isSubDelim(c) || c == ':') {
                next();
                continue;
            } else {
                break;
            }
        }
        return backtrack(start);
    }

    boolean host() {
        if (ipLiteral() || ipv4Address() || regName()) {
            return true;
        } else {
            return false;
        }
    }
    
    boolean ipLiteral() {
        if (hasNext('[')) {
            next();
            if (ipvFuture() || ipv6Address()) {
                if (next() == ']') {
                    return true;
                }
            }
            fail();
        }
        return false;
    }
    
    boolean ipvFuture() {
        if (hasNext('v') || hasNext('V')) {
            next();
            if (AsciiCode.isHexDigit(next())) {
                while (AsciiCode.isHexDigit(peek())) {
                    next();
                }
                if (next() == '.') {
                    int c = next();
                    if (UriCode.isUnreserved(c) || UriCode.isSubDelim(c) || c == ':') {
                        while (hasNext()) {
                            c = peek();
                            if (UriCode.isUnreserved(c) || UriCode.isSubDelim(c) || c == ':') {
                                next();
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
            fail();
        }
        return false;
    }
    
    boolean ipv6Address() {
        final int start = pos();
        while (hasNext()) {
            if (peek() == ']') {
                Ipv6Matcher m = new Ipv6Matcher(input(), start, pos());
                return m.matches();
            } else {
                next();
            }
        }
        return false;
    }
    
    boolean ipv4Address() {
        final int start = pos();
        while (hasNext()) {
            if (UriCode.isReserved(peek())) {
                break;
            }
            next();
        }
        Ipv4Matcher m = new Ipv4Matcher(input(), start, pos());
        if (m.matches()) {
            return true;
        }
        return backtrack(start);
    }

    boolean regName() {
        while (hasNext()) {
            if (unreserved() || pctEncoded()) {
                continue;
            }
            if (UriCode.isSubDelim(peek())) {
                next();
            } else {
                break;
            }
        }
        return true;
    }

    void port() {
        // Skips digits
        while (hasNext()) {
            if (AsciiCode.isDigit(peek())) {
                next();
            } else {
                break;
            }
        }
    }

    boolean pathAbempty() {
        while (hasNext('/')) {
            // Skips '/'
            next();
            segment();
        }
        return true;
    }
    
    boolean pathNoscheme() {
        if (segmentNzNc()) {
            while (hasNext('/')) {
                next();
                segment();
            }
            return true;
        } else {
            return false;
        }
    }
    
    boolean pathAbsolute() {
        if (hasNext('/')) {
            next();
            if (segmentNz()) {
                while (hasNext('/')) {
                    next();
                    segment();
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    boolean pathRootless() {
        if (segmentNz()) {
            while (hasNext('/')) {
                next();
                segment();
            }
            return true;
        } else {
            return false;
        }
    }
    
    boolean pathEmpty() {
        if (hasNext()) {
            int c = peek();
            if (c != '?' && c != '#') {
                return false;
            }
        }
        return true;
    }
    
    boolean segment() {
        while (pchar()) {
        }
        return true;
    }
    
    boolean segmentNz() {
        if (pchar()) {
            while (pchar()) {
            }
            return true;
        }
        return false;
    }
    
    boolean segmentNzNc() {
        int length = 0;
        while (hasNext()) {
            if (unreserved() || pctEncoded()) {
                length++;
            } else {
                int c = peek();
                if (UriCode.isSubDelim(c) || c == '@') {
                    next();
                    length++;
                } else {
                    break;
                }
            }
        }
        return length > 0;
    }
    
    boolean pchar() {
        if (hasNext()) {
            if (unreserved() || pctEncoded()) {
                return true;
            }
            int c = peek();
            if (UriCode.isSubDelim(c) || c == ':' || c == '@') {
                next();
                return true;
            }
        }
        return false;
    }
 
    /**
     * Rule for query.
     * <p>
     * The query component is terminated by a number sign ("#") character
     * or by the end of the URI.
     * </p>
     * 
     * @return {@code true} if the query component is valid.
     */
    boolean query() {
        while (hasNext() && peek() != '#') {
            if (pchar()) {
                continue;
            }
            int c = peek();
            if (c == '/' || c == '?') {
                next();
            } else {
                return fail();
            }
        }
        return true;
    }

    /**
     * Rule for fragment.
     * <p>
     * The fragment component is terminated by the end of the URI.
     * </p>
     * 
     * @return {@code true} if the fragment component is valid.
     */
    boolean fragment() {
        while (hasNext()) {
            if (pchar()) {
                continue;
            }
            int c = peek();
            if (c == '/' || c == '?') {
                next();
            } else {
                return fail();
            }
        }
        return true;
    }
    
    boolean unreserved() {
        if (hasNext() && UriCode.isUnreserved(peek())) {
            next();
            return true;
        }
        return false;
    }
    
    boolean pctEncoded() {
        if (hasNext('%')) {
            // Skips '%'
            next();
            if (AsciiCode.isHexDigit(next()) &&
                AsciiCode.isHexDigit(next())) {
                return true;
            }
            return fail();
        } else {
            // Not percent encoded.
            return false;
        }
    }
    
    boolean relativeRef() {
        if (relativePart()) {
            if (hasNext('?')) {
                next();
                if (!query()) {
                    return false;
                }
            }
            if (hasNext('#')) {
                next();
                if (!fragment()) {
                    return false;
                }
            }
            return !hasNext();
        }
        return false;
    }
    
    boolean relativePart() {
        if (hasNext('/')) {
            final int mark = pos();
            next();
            if (hasNext('/')) {
                next();
                return authority() && pathAbempty();
            }
            backtrack(mark);
            return pathAbsolute();
        }
        return pathNoscheme() || pathEmpty();
    }
    
    private static CharSequence decodeAllUnreserved(CharSequence input) {
        StringBuilder b = new StringBuilder();
        final int length = input.length();
        int startIndex = 0;
        int index = 0;
        while (index < length) {
            if (input.charAt(index) == '%' && index + 2 < length) {
                char high = input.charAt(index + 1);
                char low = input.charAt(index + 2);
                int codePoint = decodePercentEncoded(high, low);
                if (codePoint >= 0 && UriCode.isUnreserved(codePoint)) {
                    b.append(input, startIndex, index).appendCodePoint(codePoint);
                    startIndex = index + 3;
                }
                index += 3;
            } else {
                index++;
            }
        }
        if (startIndex == 0) {
            return input;
        } else if (startIndex < length) {
            b.append(input, startIndex, length);
        }
        return b.toString();
    }
    
    private static int decodePercentEncoded(int high, int low) {
        int codePoint = -1;
        if (AsciiCode.isHexDigit(high) && AsciiCode.isHexDigit(low)) {
            codePoint = AsciiCode.hexDigitToValue(high) * 16 +
                        AsciiCode.hexDigitToValue(low);
        }
        return codePoint;
    }
    
    private static final int[] GEN_DELIMS = { ':', '/', '?', '#', '[', ']', '@' };
    private static final int[] SUB_DELIMS = { '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=' };

    private static final BitSet genDelims = new BitSet();
    private static final BitSet subDelims = new BitSet();
    private static final BitSet reserved = new BitSet();
    
    static {
        for (int c : GEN_DELIMS) {
            genDelims.set(c);
        }
        for (int c : SUB_DELIMS) {
            subDelims.set(c);
        }
        reserved.or(genDelims);
        reserved.or(subDelims);
    }
}
