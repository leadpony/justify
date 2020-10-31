/*
 * Copyright 2018, 2020 the Justify authors.
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
package org.leadpony.justify.internal.base.regex;

import java.util.regex.Pattern;

/**
 * ECMA 262 regular expression pattern.
 *
 * @author leadpony
 */
public final class Ecma262Pattern {

    /*
     * Whitespace: U+0009, U+000B, U+000C, U+0020, U+0009, U+00a0, U+FEFF, and other Space_Separator(Zs)
     * Line Terminator: U+000A, U+000D, U+2028, U+2029
     */
    private static final String WHITESPACE_CHARACTER = "\\h\\f\\n\\r\\u000b​\\u2028\\u2029\\ufeff";

    private static final String WHITESPACE_CHARACTER_CLASS = "[" + WHITESPACE_CHARACTER + "]";

    private static final String NON_WHITESPACE_CHARACTER_CLASS = "[^" + WHITESPACE_CHARACTER + "]";

    /**
     * Compiles the given regular expression into a pattern.
     *
     * @param regex the expression to be compiled
     * @return the regular expression compiled into a pattern
     * @throws PatternSyntaxException if the syntax of the given expression is
     *                                invalid
     */
    public static Pattern compile(String regex) {
        String translated = translate(regex);
        return Pattern.compile(translated);
    }

    private static String translate(String regex) {
        StringBuilder builder = new StringBuilder();
        final int length = regex.length();
        for (int i = 0; i < length; i++) {
            char c = regex.charAt(i);
            if (c == '\\') {
                if (++i < length) {
                    c = regex.charAt(i);
                    if (c == 'c') {
                        if (++i < length) {
                            c = regex.charAt(i);
                            if ('a' <= c && c <= 'z') {
                                builder.append("\\c");
                                builder.append((char) ('A' + c - 'a'));
                            } else {
                                builder.append("\\c").append(c);
                            }
                        }
                    } else if (c == 's') {
                        builder.append(WHITESPACE_CHARACTER_CLASS);
                    } else if (c == 'S') {
                        builder.append(NON_WHITESPACE_CHARACTER_CLASS);
                    } else {
                        builder.append("\\").append(c);
                    }
                } else {
                    builder.append("\\");
                }
            } else if (c == '$' && i + 1 >= length) {
                builder.append("\\z");
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private Ecma262Pattern() {
    }
}
