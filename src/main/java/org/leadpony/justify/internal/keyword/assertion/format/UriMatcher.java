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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Matcher for URI based on RFC 3986.
 * 
 * @author leadpony
 * 
 * @see <a href="https://tools.ietf.org/html/rfc3986">
 * "Uniform Resource Identifier (URI): Generic Syntax", STD 66, RFC 3986</a>
 */
class UriMatcher extends FormatMatcher {
    
    private static final Logger log = Logger.getLogger(UriMatcher.class.getName());

    private int schemaStart, schemaEnd;
    private int userInfoStart, userInfoEnd;
    private int hostStart, hostEnd;
    private int portStart, portEnd;
    private int pathStart, pathEnd;
    private int queryStart, queryEnd;
    private int fragmentStart, fragmentEnd;
    
    UriMatcher(CharSequence input) {
        super(input);
        this.schemaStart = 
        this.userInfoStart = 
        this.hostStart =
        this.portStart = 
        this.pathStart = 
        this.queryStart = 
        this.fragmentStart = -1;
    }

    @Override
    boolean all() {
        boolean result = uri();
        if (result) {
            printComponents();
        }
        return result;
    }
    
    private boolean uri() {
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
    
    private boolean scheme() {
        final int start = pos();
        if (hasNext()) {
            int c = next();
            if (Characters.isAsciiAlphabetic(c)) {
                while(hasNext() && peek() != ':') {
                    c = next();
                    if (!Characters.isAsciiAlphanumeric(c) && 
                        c != '+' && c != '-' && c != '.') {
                        return false;
                    }
                }
                this.schemaStart = start;
                this.schemaEnd = pos() + 1;
                return true;
            }
        }
        return false;
    }

    private boolean hierPart() {
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

    private boolean authority() {
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
    
    private boolean userinfo() {
        final int start = pos();
        while (hasNext()) {
            int c = peek();
            if (c == '@') {
                this.userInfoStart = start;
                this.userInfoEnd = pos();
                return true;
            }
            if (isUnreserved(c) || isSubDelim(c) || c == ':') {
                next();
            } else if (pctEncoded()) {
                continue;
            } else {
                break;
            }
        }
        return backtrack(start);
    }

    private boolean host() {
        final int start = pos();
        if (ipLiteral() || ipv4Address() || regName()) {
            this.hostStart = start;
            this.hostEnd = pos();
            return true;
        } else {
            return false;
        }
    }
    
    private boolean ipLiteral() {
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
    
    private boolean ipvFuture() {
        if (hasNext('v') || hasNext('V')) {
            next();
            if (Characters.isAsciiHexDigit(next())) {
                while (Characters.isAsciiHexDigit(peek())) {
                    next();
                }
                if (next() == '.') {
                    int c = next();
                    if (isUnreserved(c) || isSubDelim(c) || c == ':') {
                        while (hasNext()) {
                            c = peek();
                            if (isUnreserved(c) || isSubDelim(c) || c == ':') {
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
    
    private boolean ipv6Address() {
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
    
    private boolean ipv4Address() {
        final int start = pos();
        while (hasNext()) {
            if (isReserved(peek())) {
                break;
            }
            next();
        }
        StrictIpv4Matcher m = new StrictIpv4Matcher(input(), start, pos());
        if (m.matches()) {
            return true;
        }
        return backtrack(start);
    }

    private boolean regName() {
        while (hasNext()) {
            if (pctEncoded()) {
                continue;
            }
            int c = peek();
            if (isUnreserved(c) || isSubDelim(c)) {
                next();
            } else {
                break;
            }
        }
        return true;
    }

    private void port() {
        this.portStart = pos();
        // Skips digits
        while (hasNext()) {
            if (Characters.isAsciiDigit(peek())) {
                next();
            } else {
                break;
            }
        }
        this.portEnd = pos();
    }

    private boolean pathAbempty() {
        final int start = pos();
        while (hasNext('/')) {
            // Skips '/'
            next();
            segment();
        }
        this.pathStart = start;
        this.pathEnd = pos();
        return true;
    }
    
    private boolean pathAbsolute() {
        final int start = pos();
        if (hasNext('/')) {
            next();
            if (segmentNz()) {
                while (hasNext('/')) {
                    next();
                    segment();
                }
            }
            this.pathStart = start;
            this.pathEnd = pos();
            return true;
        } else {
            return false;
        }
    }
    
    private boolean pathRootless() {
        final int start = pos();
        if (segmentNz()) {
            while (hasNext('/')) {
                next();
                segment();
            }
            this.pathStart = start;
            this.pathEnd = pos();
            return true;
        } else {
            return false;
        }
    }
    
    private boolean pathEmpty() {
        if (hasNext()) {
            int c = peek();
            if (c != '?' && c != '#') {
                return false;
            }
        }
        this.pathStart = this.pathEnd = pos();
        return true;
    }
    
    protected boolean segment() {
        while (pchar()) {
        }
        return true;
    }
    
    protected boolean segmentNz() {
        if (pchar()) {
            while (pchar()) {
            }
            return true;
        }
        return false;
    }
    
    protected boolean pchar() {
        if (hasNext()) {
            int c = peek();
            if (isUnreserved(c) || isSubDelim(c) || c == ':' || c == '@') {
                next();
                return true;
            }
            return pctEncoded();
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
    private boolean query() {
        final int start = pos();
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
        this.queryStart = start;
        this.queryEnd = pos();
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
    private boolean fragment() {
        final int start = pos();
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
        this.fragmentStart = start;
        this.fragmentEnd = pos();
        return true;
    }
    
    private boolean pctEncoded() {
        if (hasNext('%')) {
            // Skips '%'
            next();
            if (Characters.isAsciiHexDigit(next()) &&
                Characters.isAsciiHexDigit(next())) {
                return true;
            }
            return fail();
        } else {
            // Not percent encoded.
            return false;
        }
    }
    
    private static boolean isReserved(int c) {
        return reserved.get(c);
    }
    
    private static boolean isSubDelim(int c) {
        return subDelims.get(c);
    }
    
    protected boolean isUnreserved(int c) {
        return Characters.isAsciiAlphanumeric(c) ||
               c == '-' || c == '.' || c == '_' || c == '~'; 
    }
    
    private void printComponents() {
        if (!log.isLoggable(Level.FINE)) {
            return;
        }
        StringBuilder b = new StringBuilder();
        b.append(input())
         .append(" -> ")
         .append(getComponents().stream().collect(Collectors.joining(", ", "[", "]")))
         ;
        log.fine(b.toString());
    }
    
    private List<String> getComponents() {
        List<String> components = new ArrayList<>();
        if (schemaStart >= 0) {
            components.add(extract(schemaStart, schemaEnd));
        }
        if (userInfoStart >= 0) {
            components.add(extract(userInfoStart, userInfoEnd));
        }
        if (hostStart >= 0) {
            components.add(extract(hostStart, hostEnd));
        }
        if (portStart >= 0) {
            components.add(extract(portStart, portEnd));
        }
        if (pathStart >= 0) {
            components.add(extract(pathStart, pathEnd));
        }
        if (queryStart >= 0) {
            components.add(extract(queryStart, queryEnd));
        }
        if (fragmentStart >= 0) {
            components.add(extract(fragmentStart, fragmentEnd));
        }
        return components;
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
