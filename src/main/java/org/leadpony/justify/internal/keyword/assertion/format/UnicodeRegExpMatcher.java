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
 * @author leadpony
 */
public class UnicodeRegExpMatcher extends RegExpMatcher {

    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     */
    UnicodeRegExpMatcher(CharSequence input) {
        super(input);
    }

    @Override
    protected boolean regExpUnicodeEscapeSequence(ClassAtom atom) {
        if (!hasNext('u')) {
            return false;
        }
        int start = pos();
        next();
        if (hasNext('{')) {
            next();
            int value = codePoint();
            if (value >= 0) {
                if (hasNext('}')) {
                    next();
                    return atom.setValue(value);
                }
            }
        } else {
            int high = hex4Digits();
            if (high >= 0) {
                if (Character.isHighSurrogate((char)high)) {
                    start = pos();
                    if (isFollowedBy('\\', 'u')) {
                        int low = hex4Digits();
                        if (low >= 0) {
                            int codePoint = Character.toCodePoint((char)high, (char)low);
                            return atom.setValue(codePoint);
                        }
                    }
                    backtrack(start);
                }
                return atom.setValue(high);
            }
        }
        return backtrack(start);
    }

    @Override
    protected boolean identityEscape(ClassAtom atom) {
        if (super.identityEscape(atom)) {
            return true;
        } else {
            int c = peek();
            if (!Character.isUnicodeIdentifierPart(c)) {
                // SourceCharacter but not UnicodeIDContinue
                next();
                return atom.setValue(c);
            }
            return false;
        }
    }
    
    private int codePoint() {
        if (!hasNext()) {
            return -1;
        }
        final int start = pos();
        int value = hexDigitToValue(next());
        while (hasNext()) {
            if (isHexDigit(peek())) {
                value = value * 16 + hexDigitToValue(next());
            } else {
                return value;
            }
        }
        backtrack(start);
        return -1;
    }
    
    private boolean isFollowedBy(int... chars) {
        final int start = pos();
        for (int c : chars) {
            if (hasNext((char)c)) {
                next();
            } else {
                return backtrack(start);
            }
        }
        return true;
    }
}
