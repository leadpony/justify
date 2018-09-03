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

import java.util.NoSuchElementException;

/**
 * Format matcher.
 * 
 * @author leadpony
 */
interface FormatMatcher {

    /**
     * Checks if the input matches the format or not.
     * 
     * @return {@code true} if the input matched, {@code false} otherwise.
     */
    default boolean matches() {
        try {
            all();
            return true;
        } catch (FormatMismatchException | NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Tests the whole input.
     * 
     * @throws NoSuchElementException if unexpected end of input was detected.
     * @throws FormatMismatchException if the input did not match the format.
     */
    void all(); 
    
    /**
     * Returns the input character sequence.
     * 
     * @return the input character sequence.
     */
    CharSequence input();

    /**
     * Returns the current position.
     * 
     * @return the current position, zero or positive integer.
     */
    int pos();

    /**
     * Returns {@code true} if the input has more characters. 
     * 
     * @return {@code true} if the input has more characters.
     */
    boolean hasNext();
    
    /**
     * Returns the next character in the input.
     * This method advances the current position.
     * 
     * @return the next character.
     * @throws NoSuchElementException if the input has no more characters.
     */
    char next();
    
    /**
     * Peeks the next character in the input.
     * This method does not advance the current position.
     * 
     * @return the next character.
     * @throws NoSuchElementException if the input has no more characters.
     */
    char peek();
}
