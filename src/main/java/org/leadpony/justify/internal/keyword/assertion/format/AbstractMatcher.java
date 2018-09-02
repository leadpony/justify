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
 * Base type for matchers.
 * 
 * @author leadpony
 */
abstract class AbstractMatcher {
    
    private final CharSequence value;
    private final int length;
    private int index;
    
    protected AbstractMatcher(CharSequence value) {
        this.value = value;
        this.length = value.length();
    }
    
    boolean matches() {
        try {
            all();
            return true;
        } catch (MismatchException | NoSuchElementException e) {
            return false;
        }
    }
    
    protected abstract void all(); 

    protected int pos() {
        return index;
    }

    protected boolean hasNext() {
        return index < length;
    }
    
    protected char next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return value.charAt(index++);
    }
    
    protected char peek() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return value.charAt(index);
    }
    
    protected static void fail() {
        throw new MismatchException();
    }
    
    protected CharSequence subSequence() {
        return value.subSequence(index, length);
    }
    
    @SuppressWarnings("serial")
    private static class MismatchException extends RuntimeException {
    };
}
