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
 * Skeletal implementation of {@link FormatMatcher}.
 * 
 * @author leadpony
 */
abstract class AbstractFormatMatcher implements FormatMatcher {
    
    private final CharSequence input;
    private final int length;
    private int index;
    
    protected AbstractFormatMatcher(CharSequence input) {
        this.input = input;
        this.length = input.length();
        this.index = 0;
    }
    
    protected AbstractFormatMatcher(CharSequence input, int start, int end) {
        this.input = input;
        this.length = end;
        this.index = start;
    }

    @Override
    public final CharSequence input() {
        return input;
    }
    
    @Override
    public final int pos() {
        return index;
    }

    @Override
    public final boolean hasNext() {
        return index < length;
    }
    
    @Override
    public final char next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return input.charAt(index++);
    }
    
    @Override
    public final char peek() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return input.charAt(index);
    }
    
    @Override
    public String toString() {
        return input.subSequence(index, length).toString();
    }

    /**
     * Should be called when matching failed.
     * 
     * @throws FormatMismatchException always thrown.
     */
    protected static void fail() {
        throw new FormatMismatchException();
    }
}
