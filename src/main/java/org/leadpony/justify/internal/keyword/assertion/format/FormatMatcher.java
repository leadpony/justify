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
     * @param input the input character sequence.
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
            return all();
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
    abstract boolean all(); 
    
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
     * Backtracks to the specified position.
     * 
     * @param pos the position at which the next read occurs.
     * @return always {@code false}.
     */
    final boolean backtrack(int pos) {
        this.index = pos;
        return false;
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
     * Checks if the input has next character and 
     * the character is the same as the expected.
     * 
     * @param expected the code point of the expected character.
     * @return {@code true} if the next character is the expected one.
     */
    final boolean hasNext(int expected) {
        return hasNext() && peek() == expected;
    }
    
    /**
     * Returns the next character in the input.
     * Calling this method advances the current position.
     * 
     * @return the code point of the next character.
     * @throws NoSuchElementException if the input has no more characters.
     */
    final int next() {
        if (hasNext()) {
            int codePoint = codePointAt(input, index);
            index = offsetByCodePoint(input, index);
            return codePoint;
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * Peeks the next character in the input.
     * Calling this method never change the current position.
     * 
     * @return the code point of the next character.
     * @throws NoSuchElementException if the input has no more characters.
     */
    final int peek() {
        if (hasNext()) {
            return codePointAt(input, index);
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * Extracts a substring.
     * 
     * @param start the start index of the substring. 
     * @return the extracted substring.
     */
    final String extract(int start) {
        return extract(start, pos());
    }

    /**
     * Extracts a substring from the input character sequence.
     * 
     * @param start the start index of the substring. 
     * @param end the end index of the substring. 
     * @return the extracted substring.
     */
    final String extract(int start, int end) {
        return input.subSequence(start, end).toString();
    }

    /**
     * Returns the string representation of the currentinput.
     * 
     * @return the string representation of the current input.
     */
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
    static boolean fail() {
        throw new FormatMismatchException();
    }
    
    /**
     * Returns the code point at the given index of the input. 
     * 
     * @param input the input character sequence.
     * @param index the index to the character in the input.
     *              This must be less than the length of the input.
     * @return the code point at the given index.
     * @throws IndexOutOfBoundsException 
     *         if the {@code index} is not less than the input length.
     */
    protected int codePointAt(CharSequence input, int index) {
        return Character.codePointAt(input, index);
    }
    
    /**
     * Returns the next index offset by a character.
     *  
     * @param input the input character sequence.
     * @param index the index to be offset.
     *              This must be less than the length of the input.
     * @return the index within the input sequence.
     */
    protected int offsetByCodePoint(CharSequence input, int index) {
        return Character.offsetByCodePoints(input, index, 1);
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
