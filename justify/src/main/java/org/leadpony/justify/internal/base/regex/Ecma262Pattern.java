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
package org.leadpony.justify.internal.base.regex;

import java.util.regex.Pattern;

/**
 * ECMA 262 regular expression pattern.
 *
 * @author leadpony
 */
public final class Ecma262Pattern {

    /**
     * Compiles the given regular expression into a pattern.
     *
     * @param regex the expression to be compiled
     * @return the regular expression compiled into a pattern
     * @throws PatternSyntaxException if the syntax of the given expression is invalid
     */
    public static Pattern compile(String regex) {
        String translated = translate(regex);
        return Pattern.compile(translated);
    }

    private static String translate(String regex) {
        StringBuilder builder = new StringBuilder();
        final int length = regex.length();
        int last = 0;
        for (int i = 0; i < length; i++) {
            char c = regex.charAt(i);
            if (c == '\\') {
                if (++i < length) {
                    c = regex.charAt(i);
                    if (c == 'c') {
                        if (++i < length) {
                            c = regex.charAt(i);
                            if ('a' <= c &&  c <= 'z') {
                                builder.append(regex, last, i);
                                builder.append((char) ('A' + c - 'a'));
                                last = i + 1;
                            }
                        }
                    }
                }
            } else if (c == '$' && i + 1 >= length) {
                builder.append(regex, last, i);
                builder.append("\\z");
                last = i + 1;
            }
        }
        if (last == 0) {
            return regex;
        } else if (last < length) {
            builder.append(regex, last, length);
        }
        return builder.toString();
    }

    private Ecma262Pattern() {
    }
}
