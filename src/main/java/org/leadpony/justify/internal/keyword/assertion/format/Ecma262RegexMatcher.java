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

import java.util.BitSet;

/**
 * ECMA262 RegExp matcher.
 * 
 * @author leadpony
 */
class Ecma262RegexMatcher extends FormatMatcher {
    
    @SuppressWarnings("serial")
    private static final BitSet syntaxCharSet = new BitSet() {{
        set('^');
        set('$');
        set('\\');
        set('.');
        set('*');
        set('+');
        set('?');
        set('(');
        set(')');
        set('[');
        set(']');
        set('{');
        set('}');
        set('|');
    }};
    
    private static final ClassAtom classAtomDiscarded = new ClassAtom();
    
    private int maxCapturingGroupNumber;
    private int leftCapturingParentheses;
    
    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     */
    Ecma262RegexMatcher(CharSequence input) {
        super(input);
    }

    @Override
    public void all() {
        disjunction();
        if (hasNext()) {
            fail();
        }
        checkCapturingNumber();
    }
    
    private void disjunction() {
        alternative();
        while (hasNext() && peek() == '|') {
            next();
            alternative();
        }
    }
    
    /**
     * Note that {@code alternative} can be empty.
     */
    private void alternative() {
        while (hasNext()) {
            if (!term()) {
                break;
            }
        }
    }
    
    private boolean term() {
        if (assertion()) {
            return true;
        } else if (atom() && optional(quantifier())) {
            return true;
        }
        return false;
    }
    
    private boolean assertion() {
        final int start = pos();
        char c = next();
        if (c == '^' || c == '$') {
            return true;
        } else if (c == '\\') {
            c = next();
            if (c == 'b' || c == 'B') {
                return true;
            }
        } else if (c == '(') {
            c = next();
            if (c == '?') {
                c = next();
                if (c == '<') {
                    c = next();
                }
                if (c == '=' || c == '!') {
                    return capturingGroup();
                }
            }
        }
        return backtrack(start);
    }
    
    private boolean atom() {
        if (patternCharacter() || characterClass()) {
            return true;
        }
        char c = peek();
        if (c == '.' ) {
            next();
            return true;
        } else if (c == '\\') {
            next();
            if (atomEscape()) {
                return true;
            }
            return fail();
        } else if (c == '(') {
            next();
            if (peek() == '?') {
                next();
            }
            return capturingGroup();
        }
        // no atom
        return false;
    }
    
    private boolean capturingGroup() {
        this.leftCapturingParentheses++;
        disjunction();
        if (next() != ')') {
            return fail();
        }
        return true;
    }
   
    private boolean syntaxCharacter() {
        if (isSyntaxCharacter(peek())) {
            next();
            return true;
        } else {
            return false;
        }
    }
    
    private boolean patternCharacter() {
        if (!isSyntaxCharacter(peek())) {
            next();
            return true;
        } else {
            return false;
        }
    }

    private boolean quantifier() {
        if (qualifierPrefix()) {
            if (hasNext() && peek() == '?') {
                next();
            }
            return true;
        }
        return false;
    }
    
    private boolean qualifierPrefix() {
        if (!hasNext()) {
            return false;
        }
        char c = peek();
        if (c == '*' || c == '+' || c == '?') {
            next();
            return true;
        } else if (c == '{') {
            next();
            final long first = decimalDigits();
            c = next();
            if (c == '}') {
                return true;
            } else if (c == ',') {
                if (peek() == '}') {
                    next();
                    return true;
                } else {
                    final long second = decimalDigits();
                    if (checkQuantifierRange(first, second) &&
                        next() == '}') {
                        return true;
                    }
                      
                }
            }
            return fail();
        }
        return false;
    }
    
    private boolean characterClass() {
        if (peek() != '[') {
            return false;
        }
        next();
        if (peek() == '^') {
            next();
        }
        classRanges();
        return next() == ']' || fail();
    }
    
    /**
     * Multiple class ranges.
     */
    private void classRanges() {
        while (hasNext()) {
            if (!nonemptyClassRanges()) {
                break;
            }
        }
    }
    
    private boolean nonemptyClassRanges() {
        ClassAtom first = new ClassAtom();
        if (classAtom(first)) {
            if (peek() == '-') {
                final int start = pos();
                next();
                ClassAtom second = new ClassAtom();
                if (classAtom(second)) {
                    return checkClassRange(first, second);
                }
                backtrack(start);
            } else if (nonemptyClassRangesNoDash()) {
                return true;
            }
            return true;
        } else {
            return false;
        }
    }
    
    private boolean nonemptyClassRangesNoDash() {
        final int start = pos();
        ClassAtom first = new ClassAtom();
        if (classAtomNoDash(first)) {
            if (peek() == '-') {
                next();
                ClassAtom second = new ClassAtom();
                if (classAtom(second)) {
                    return checkClassRange(first, second);
                }
            } else if (nonemptyClassRangesNoDash()) {
                return true;
            }
        }
        backtrack(start);
        return classAtom(first);
    }

    private boolean classAtom(ClassAtom atom) {
        if (peek() == '-') {
            return atom.setValue(next());
        } else {
            return classAtomNoDash(atom);
        }
    }
    
    private boolean classAtomNoDash(ClassAtom atom) {
        char c = peek();
        if (c == '\\') {
            next();
            return classEscape(atom);
        } else if (c == ']' || c == '-') {
            return false;
        } else {
            // SourceCharacter but not one of \ or ] or -
            return atom.setValue(next());
        }
    }
    
    private boolean classEscape(ClassAtom atom) {
        char c = peek();
        if (c == 'b') {
            next();
            return atom.setValue('\u0008');
        } else if (c == '-') {
            return atom.setValue(next());
        }
        return characterClassEscape() || characterEscape(atom);
    }
    
    private long decimalDigits() {
        char c = next();
        if (Characters.isAsciiDigit(c)) {
            long value = c - '0';
            while (hasNext() && Characters.isAsciiDigit(peek())) {
                c = next();
                value = (value * 10) + (c - '0');
            }
            return value;
        } else {
            fail();
            return 0;
        }
    }
    
    private boolean atomEscape() {
        if (decimalEscape() || characterClassEscape()) {
            return true;
        } else if (characterEscape(classAtomDiscarded)) {
            return true;
        } if (peek() == 'k') {
            next();
            return groupName() || fail();
        }
        return fail();
    }
    
    private boolean decimalEscape() {
        if (!isNonZeroDigit(peek())) {
            return false;
        }
        int number = digitToValue(next());
        while (hasNext() && Characters.isAsciiDigit(peek())) {
            number = number * 10  + digitToValue(next());
        }
        if (number > this.maxCapturingGroupNumber) {
            this.maxCapturingGroupNumber = number;
        }
        return true;
    }
    
    private boolean characterClassEscape() {
        char c = peek();
        if (c == 'd' || c == 'D' ||
            c == 's' || c == 'S' ||
            c == 'w' || c == 'W') {
            next();
            return true;
        } else if (c == 'p' || c == 'P') {
            // TODO:
            return false;
        }
        return false;
    }
    
    private boolean characterEscape(ClassAtom atom) {
        if (controlEscape(atom) ||
            hexEscapeSequence(atom) ||
            regExpUnicodeEscapeSequence(atom) ||
            identityEscape(atom)) {
            return true;
        }
        char c = peek();
        if (c == 'c') {
            next();
            return controlLetter(atom) || fail();
        } else if (c == '0') {
            next();
            if (hasNext() && Characters.isAsciiDigit(peek())) {
                return fail();
            }
            return atom.setValue('\u0000');
        }
        return false;
    }
    
    private boolean controlEscape(ClassAtom atom) {
        char value;
        switch (peek()) {
        case 't':
            value = 9;
            break;
        case 'n':
            value = 10;
            break;
        case 'v':
            value = 11;
            break;
        case 'f':
            value = 12;
            break;
        case 'r':
            value = 13;
            break;
        default:
            return false;
        }
        return atom.setValue(value);
    }
    
    private boolean controlLetter(ClassAtom atom) {
        char c  = peek();
        if (Characters.isAsciiAlphabetic(c)) {
            next();
            return atom.setValue((char)(c % 32));
        } else {
            return false;
        }
    }
    
    private boolean groupName() {
        if (peek() != '<') {
            return false;
        }
        // TODO:
        return next() == '>' || fail();
    }
    
    private boolean hexEscapeSequence(ClassAtom atom) {
        if (peek() != 'x') {
            return false;
        }
        next();
        char first = next();
        if (isHexDigit(first)) {
            char second = next();
            if (isHexDigit(second)) {
                int value = hexDigitToValue(first) * 16 + hexDigitToValue(second);
                return atom.setValue((char)value);
            }
        }
        return fail();
    }
    
    private boolean regExpUnicodeEscapeSequence(ClassAtom atom) {
        if (peek() != 'u') {
            return false;
        }
        next();
        if (peek() == '{') {
            next();
            int value = codePoint();
            if (next() == '}') {
                return atom.setValue((char)value);
            }
        } else {
            return atom.setValue((char)hex4Digits());
        }
        // TODO:
        return fail();
    }
    
    private boolean identityEscape(ClassAtom atom) {
        char c = peek();
        if (syntaxCharacter()) {
            return atom.setValue(c);
        } else if (c == '/') {
            next();
            return atom.setValue(c);
        } else if (!Character.isUnicodeIdentifierPart(c)) {
            // SourceCharacter but not UnicodeIDContinue
            next();
            return atom.setValue(c);
        }
        return false;
    }
    
    private int hex4Digits() {
        int value = 0;
        int digits = 0;
        while (hasNext()) {
            char c = next();
            if (!isHexDigit(c)) {
                break;
            }
            value = value * 16 + hexDigitToValue(c);
            if (++digits >= 4) {
                return value;
            }
        }
        fail();
        return 0;
    }
    
    private int codePoint() {
        char c = next();
        int value = hexDigitToValue(c);
        while (hasNext()) {
            c = next();
            if (isHexDigit(c)) {
                value = value * 16 + hexDigitToValue(c);
            } else {
                return value;
            }
        }
        fail();
        return 0;
    }

    /**
     * It is a Syntax Error if the MV of the first DecimalDigits is 
     * larger than the MV of the second DecimalDigits.
     * 
     * @param first the first decimal digits.
     * @param second the second  decimal digits.
     * @return {@code true} if the test passed.
     */
    private static boolean checkQuantifierRange(long first, long second) {
        if (first > second) {
            return fail();
        }
        return true;
    }
    
    private static boolean checkClassRange(ClassAtom lower, ClassAtom upper) {
        if (lower.isCharacterClass() || upper.isCharacterClass()) {
            return fail();
        } else if (lower.getValue() > upper.getValue()) {
            return fail();
        }
        return true;
    }
    
    private boolean checkCapturingNumber() {
        if (this.maxCapturingGroupNumber > this.leftCapturingParentheses) {
            return fail();
        }
        return true;
    }
    
    private static boolean isSyntaxCharacter(char c) {
        return syntaxCharSet.get(c);
    }
   
    private static boolean isNonZeroDigit(char c) {
        return c >= '1' && c <= '9';
    }
    
    private static boolean isHexDigit(char c) {
        return Characters.isAsciiDigit(c) ||
               (c >= 'a' && c <= 'f') ||
               (c >= 'A' && c <= 'F');
    }
    
    private static int digitToValue(char c) {
        return (c - '0');
    }
    
    private static int hexDigitToValue(char c) {
        if (Characters.isAsciiDigit(c)) {
            return c - '0';
        } else if (c >= 'a' || c <= 'f') {
            return 10 + (c - 'a');
        } else if (c >= 'A' || c <= 'F') {
            return 10 + (c - 'A');
        }
        throw new IllegalArgumentException();
    }
    
    private static boolean optional(boolean result) {
        return true;
    }
    
    /**
     * Instance of class atom.
     * 
     * @author leadpony
     */
    private static class ClassAtom {

        private char value;
        private boolean isCharacterClass = true;
        
        char getValue() {
            assert !isCharacterClass;
            return value;
        }
        
        boolean setValue(char value) {
            this.value = value;
            this.isCharacterClass = false;
            return true;
        }
        
        boolean isCharacterClass() {
            return isCharacterClass;
        }
    }
}
