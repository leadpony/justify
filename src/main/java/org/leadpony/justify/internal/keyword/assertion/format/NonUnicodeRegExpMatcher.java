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
 * @author leadpony
 */
class NonUnicodeRegExpMatcher extends RegExpMatcher {

    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     */
    NonUnicodeRegExpMatcher(CharSequence input) {
        super(input);
    }

    @Override
    boolean hasNext() {
        return index < length;
    }

    @Override
    boolean hasNext(int expected) {
        if (index < length) {
            return input.charAt(index) == expected;
        } else {
            return false;
        }
    }

    @Override
    int next() {
        if (index < length) {
            return input.charAt(index++);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    int peek() {
        if (index < length) {
            return input.charAt(index);
        } else {
            throw new NoSuchElementException();
        }
    }
}
