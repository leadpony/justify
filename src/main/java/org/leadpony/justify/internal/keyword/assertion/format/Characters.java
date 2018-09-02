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
 * Utility class operating on characters.
 * 
 * @author leadpony
 */
final class Characters {

    private Characters() {
    }

    /**
     * Determines if the specified character is an alphabet
     * and an ASCII code.
     * 
     * @param c the character to test.
     * @return {@code true} if the specified character is an alphabet.
     */
    public static boolean isAsciiAlphabetic(char c) {
        return (c >= 'A' &&  c <= 'Z') ||  (c >= 'a' &&  c <= 'z');
    }
    
    /**
     * Determines if the specified character is a digit
     * and an ASCII code.
     * 
     * @param c the character to test.
     * @return {@code true} if the specified character is a digit.
     */
    public static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    public static boolean isAsciiAlphanumeric(char c) {
        return isAsciiAlphabetic(c) || isAsciiDigit(c);
    }
    
    /**
     * Checks if the specified character is a noncharacter or not.
     * There are 66 noncharacters defined in the Unicode specification.
     * 
     * @param codePoint the code point of the character.
     * @return {@code true} if the specified character is a noncharacter.
     */
    public static boolean isNoncharacter(int codePoint) {
        if (codePoint >= 0xfdd0 && codePoint <= 0xfdef) {
            return true;
        }
        int lower = codePoint & 0x0ffff;
        return (lower == 0xfffe || lower == 0xffff);
    }
}
