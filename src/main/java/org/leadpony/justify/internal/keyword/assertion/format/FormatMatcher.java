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
abstract class FormatMatcher {

    private final CharSequence input;
    private final int length;
    private int index;
   
    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     */
    protected FormatMatcher(CharSequence input) {
        this.input = input;
        this.length = input.length();
        this.index = 0;
    }
    
    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     * @param start the start index, inclusive.
     * @param end the end index, exclusive.
     */
    protected FormatMatcher(CharSequence input, int start, int end) {
        this.input = input;
        this.length = end;
        this.index = start;
    }
    
    /**
     * Checks if the input matches the format or not.
     * 
     * @return {@code true} if the input matched, {@code false} otherwise.
     */
    boolean matches() {
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
    abstract void all(); 
    
    /**
     * Returns the input character sequence.
     * 
     * @return the input character sequence.
     */
    final CharSequence input() {
        return input;
    }

    /**
     * Returns the current position.
     * 
     * @return the current position, zero or positive integer.
     */
    final int pos() {
        return index;
    }

    /**
     * Returns {@code true} if the input has more characters. 
     * 
     * @return {@code true} if the input has more characters.
     */
    final boolean hasNext() {
        return index < length;
    }
    
    /**
     * Returns the next character in the input.
     * This method advances the current position.
     * 
     * @return the next character.
     * @throws NoSuchElementException if the input has no more characters.
     */
    final char next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return input.charAt(index++);
    }
    
    /**
     * Peeks the next character in the input.
     * This method does not advance the current position.
     * 
     * @return the next character.
     * @throws NoSuchElementException if the input has no more characters.
     */
    final char peek() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return input.charAt(index);
    }

    /**
     * Backtracks to the specified position.
     * 
     * @param pos the position at which the next read occurs.
     * @return always {@code false}.
     */
    final boolean backtrack(int pos) {
        this.index = pos;
        return false;
    }

    @Override
    public String toString() {
        return input.subSequence(index, length).toString();
    }

    /**
     * Should be called when matching failed.
     * 
     * @returns never return.
     * @throws FormatMismatchException always thrown.
     */
    protected static boolean fail() {
        throw new FormatMismatchException();
    }

    /**
     * Exception thrown when matching failed.
     * 
     * @author leadpony
     */
    private static class FormatMismatchException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
