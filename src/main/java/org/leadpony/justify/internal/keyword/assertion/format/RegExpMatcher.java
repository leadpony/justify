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
import java.util.HashSet;
import java.util.Set;

/**
 * The base matcher for ECMA262 regular expression.
 * 
 * @author leadpony
 */
abstract class RegExpMatcher extends FormatMatcher {
    
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
    
    private int maxCapturingGroupNumber;
    private int leftCapturingParentheses;
    
    private Set<String> groups;
    private Set<String> groupReferences;
    
    protected int lastNumericValue;
    protected ClassAtom lastClassAtom;
    
    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     */
    RegExpMatcher(CharSequence input) {
        super(input);
    }

    @Override
    public void all() {
        disjunction();
        if (hasNext()) {
            fail();
        }
        checkCapturingNumber();
        checkGroupReferences();
    }
    
    /**
     * Returns all group names.
     * 
     * @return all names of the found groups.
     */
    Set<String> groupNames() {
        return groups;
    }
    
    private void disjunction() {
        alternative();
        while (hasNext('|')) {
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
        int c = next();
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
  
    private boolean quantifier() {
        if (quantifierPrefix()) {
            if (hasNext('?')) {
                next();
            }
            return true;
        }
        return false;
    }
    
    private boolean quantifierPrefix() {
        if (!hasNext()) {
            return false;
        }
        int c = peek();
        if (c == '*' || c == '+' || c == '?') {
            next();
            return true;
        } else if (c == '{') {
            next();
            decimalDigits();
            int first = this.lastNumericValue;
            c = next();
            if (c == '}') {
                return true;
            } else if (c == ',') {
                if (hasNext('}')) {
                    next();
                    return true;
                } else {
                    decimalDigits();
                    final int second = this.lastNumericValue;
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
    
    private boolean atom() {
        if (patternCharacter() || characterClass()) {
            return true;
        }
        int c = peek();
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
            if (groupSpecifier()) {
                return capturingGroup();
            } else if (next() == '?' && next() == ':') {
                return capturingGroup();
            }
            return fail();
        }
        // no atom
        return false;
    }
 
    protected boolean syntaxCharacter() {
        if (hasNext() && isSyntaxCharacter(peek())) {
            next();
            return true;
        } else {
            return false;
        }
    }
    
    private boolean patternCharacter() {
        if (hasNext() && !isSyntaxCharacter(peek())) {
            next();
            return true;
        } else {
            return false;
        }
    }

    private boolean atomEscape() {
        if (decimalEscape() || characterClassEscape()) {
            return true;
        } else if (characterEscape()) {
            return true;
        } if (hasNext('k')) {
            next();
            return groupName(true) || fail();
        }
        return fail();
    }
    
    private boolean characterEscape() {
        if (controlEscape() ||
            hexEscapeSequence() ||
            regExpUnicodeEscapeSequence() ||
            identityEscape()) {
            return true;
        }
        int c = peek();
        if (c == 'c') {
            next();
            return controlLetter() || fail();
        } else if (c == '0') {
            next();
            if (hasNext() && Characters.isAsciiDigit(peek())) {
                return fail();
            }
            this.lastClassAtom = ClassAtom.of('\u0000');
            return true;
        }
        return false;
    }
   
    private boolean controlEscape() {
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
        this.lastClassAtom = ClassAtom.of(value);
        return true;
    }
    
    private boolean controlLetter() {
        int c  = peek();
        if (isControlLetter(c)) {
            next();
            this.lastClassAtom = ClassAtom.of(c % 32);
           return true;
        } else {
            return false;
        }
    }

    private boolean groupSpecifier() {
        final int start = pos();
        if (hasNext('?')) {
            next();
            if (groupName(false)) {
                return true;
            } else {
                return backtrack(start);
            }
        } else {
            return true;
        }
    }
    
    private boolean groupName(boolean reference) {
        final int start = pos();
        if (!hasNext('<')) {
            return false;
        }
        next();
        final int nameStart = pos();
        if (!regExpIdentifierName()) {
            return false;
        }
        String name = extract(nameStart);
        if (hasNext('>')) {
            next();
            if (reference) {
                addGroupReference(name);
            } else {
                addGroup(name);
            }
            return true;
        } else {
            return backtrack(start);
        }
    }
   
    private boolean regExpIdentifierName() {
        if (!regExpIdentifierStart()) {
            return false;
        }
        while (regExpIdentifierPart()) {
        }
        return true;
    }
    
    private boolean regExpIdentifierStart() {
        if (!hasNext()) {
            return false;
        }
        final int start = pos(); 
        int c = peek();
        if (isRegExpIdentifierStart(c)) {
            next();
            return true;
        } else if (c == '\\') {
            next();
            if (regExpUnicodeEscapeSequence()) {
                if (isRegExpIdentifierStart(this.lastNumericValue)) {
                    return true;
                } else {
                    return fail();
                }
            } else {
                return backtrack(start);
            }
        }
        return false;
    }
    
    private boolean regExpIdentifierPart() {
        if (!hasNext()) {
            return false;
        }
        final int start = pos(); 
        int c = peek();
        if (isRegExpIdentifierPart(c)) {
            next();
            return true;
        } else if (c == '\\') {
            next();
            if (regExpUnicodeEscapeSequence()) {
                if (isRegExpIdentifierPart(this.lastNumericValue)) {
                    return true;
                } else {
                    return fail();
                }
            } else {
                return backtrack(start);
            }
        }
        return false;
    }
  
    protected boolean regExpUnicodeEscapeSequence() {
        if (!hasNext('u')) {
            return false;
        }
        final int start = pos();
        next();
        if (hex4Digits()) {
            this.lastClassAtom = ClassAtom.of(this.lastNumericValue);
            return true;
        } else {
            return backtrack(start);
        }
    }
    
    protected abstract boolean identityEscape();
    
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
        if (testCharacterClassEscape()) {
            this.lastClassAtom = ClassAtom.CHARACTER_CLASS;
            return true;
        } else {
            return false;
        }
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
        if (classAtom()) {
            ClassAtom first = this.lastClassAtom;
            if (peek() == '-') {
                final int start = pos();
                next();
                if (classAtom()) {
                    ClassAtom second = this.lastClassAtom;
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
        if (classAtomNoDash()) {
            ClassAtom first = this.lastClassAtom;
            if (peek() == '-') {
                next();
                if (classAtom()) {
                    ClassAtom second = this.lastClassAtom;
                    return checkClassRange(first, second);
                }
            } else if (nonemptyClassRangesNoDash()) {
                return true;
            }
        }
        backtrack(start);
        return classAtom();
    }

    private boolean classAtom() {
        if (peek() == '-') {
            this.lastClassAtom = ClassAtom.of(next());
            return true;
        } else {
            return classAtomNoDash();
        }
    }
    
    private boolean classAtomNoDash() {
        int c = peek();
        if (c == '\\') {
            next();
            return classEscape();
        } else if (c == ']' || c == '-') {
            return false;
        } else {
            // SourceCharacter but not one of \ or ] or -
            this.lastClassAtom = ClassAtom.of(next());
            return true;
        }
    }
    
    private boolean classEscape() {
        int c = peek();
        if (c == 'b') {
            next();
            this.lastClassAtom = ClassAtom.of('\u0008');
            return true;
        } else if (c == '-') {
            this.lastClassAtom = ClassAtom.of(next());
            return true;
        }
        return characterClassEscape() || characterEscape();
    }
    
    private boolean decimalDigits() {
        int c = next();
        if (Characters.isAsciiDigit(c)) {
            int value = c - '0';
            while (hasNext() && Characters.isAsciiDigit(peek())) {
                c = next();
                value = (value * 10) + (c - '0');
            }
            lastNumericValue = value;
            return true;
        } else {
            return fail();
        }
    }
    
    private boolean hexEscapeSequence() {
        if (!hasNext('x')) {
            return false;
        }
        next();
        int first = next();
        if (isHexDigit(first)) {
            int second = next();
            if (isHexDigit(second)) {
                int value = hexDigitToValue(first) * 16 + hexDigitToValue(second);
                this.lastClassAtom = ClassAtom.of(value);
                return true;
            }
        }
        return fail();
    }

    protected boolean hex4Digits() {
        final int start = pos();
        int value = 0;
        int digits = 0;
        while (hasNext()) {
            int c = next();
            if (!isHexDigit(c)) {
                break;
            }
            value = value * 16 + hexDigitToValue(c);
            if (++digits >= 4) {
                this.lastNumericValue = value;
                return true;
            }
        }
        return backtrack(start);
    }
   
    protected boolean testCharacterClassEscape() {
        int c = peek();
        if (c == 'd' || c == 'D' ||
            c == 's' || c == 'S' ||
            c == 'w' || c == 'W') {
            next();
            return true;
        }
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
   
    private void addGroup(String name) {
        if (this.groups == null) {
            this.groups = new HashSet<>();
        }
        if (this.groups.contains(name)) {
            fail();
        } else {
            this.groups.add(name);
        }
    }
    
    private void addGroupReference(String name) {
        if (this.groupReferences == null) {
            this.groupReferences = new HashSet<>();
        }
        this.groupReferences.add(name);
    }

    private boolean findGroup(String name) {
        if (this.groups == null) {
            return false;
        }
        return this.groups.contains(name);
    }
    
    /**
     * It is a Syntax Error if the MV of the first DecimalDigits is 
     * larger than the MV of the second DecimalDigits.
     * 
     * @param first the first decimal digits.
     * @param second the second  decimal digits.
     * @return {@code true} if the test passed.
     */
    private static boolean checkQuantifierRange(int first, int second) {
        if (first <= second) {
            return true;
        } else {
            return fail();
        }
    }
    
    private static boolean checkClassRange(ClassAtom lower, ClassAtom upper) {
        if (lower.isCharacterClass() || upper.isCharacterClass()) {
            return fail();
        } else if (lower.codePoint() > upper.codePoint()) {
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
    
    private boolean checkGroupReferences() {
        if (this.groupReferences == null) {
            return true;
        }
        for (String ref : this.groupReferences) {
            if (!findGroup(ref)) {
                return fail();
            }
        }
        return true;
    }
    
    protected static boolean isSyntaxCharacter(int c) {
        return syntaxCharSet.get(c);
    }
   
    protected static boolean isNonZeroDigit(int c) {
        return c >= '1' && c <= '9';
    }
    
    protected static boolean isHexDigit(int c) {
        return Characters.isAsciiDigit(c) ||
               (c >= 'a' && c <= 'f') ||
               (c >= 'A' && c <= 'F');
    }
    
    protected static boolean isRegExpIdentifierStart(int c) {
        return Character.isUnicodeIdentifierStart(c) || 
               c == '$' || 
               c == '_';
    }
    
    protected static boolean isRegExpIdentifierPart(int c) {
        return Character.isUnicodeIdentifierPart(c) || 
               c == '$' ||
               c == '\u200c' || 
               c == '\u200d';
    }
    
    protected static boolean isControlLetter(int c) {
        return Characters.isAsciiAlphabetic(c);
    }

    protected static int digitToValue(int c) {
        return (c - '0');
    }
    
    protected static int hexDigitToValue(int c) {
        if (Characters.isAsciiDigit(c)) {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return 10 + (c - 'a');
        } else if (c >= 'A' && c <= 'F') {
            return 10 + (c - 'A');
        }
        throw new IllegalArgumentException();
    }
    
    protected static boolean optional(boolean result) {
        return true;
    }
    
    static interface ClassAtom {
        
        boolean isCharacterClass();

        int codePoint();
        
        static ClassAtom of(int c) {
            return new DefaultClassAtom(c);
        }

        static ClassAtom CHARACTER_CLASS = new ClassAtom() {
            
            @Override
            public boolean isCharacterClass() {
                return true;
            }

            @Override
            public int codePoint() {
                throw new IllegalStateException();
            }
        };
    }
    
    /**
     * Class atom with a code point.
     * 
     * @author leadpony
     */
    private static class DefaultClassAtom implements ClassAtom {

        private final int codePoint;
        
        DefaultClassAtom(int codePoint) {
            this.codePoint = codePoint;
        }
        
        @Override
        public boolean isCharacterClass() {
            return false;
        }

        @Override
        public int codePoint() {
            return codePoint;
        }
    }
}
