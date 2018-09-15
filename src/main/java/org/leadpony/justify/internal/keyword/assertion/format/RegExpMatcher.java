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
    
    private static final ClassAtom classAtomDiscarded = new ClassAtom();
    
    private int maxCapturingGroupNumber;
    private int leftCapturingParentheses;
    
    private Set<String> groups;
    private Set<String> groupReferences;
    
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
            if (hasNext('?')) {
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
        int c = peek();
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
                if (hasNext('}')) {
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
        int c = peek();
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
        int c = peek();
        if (c == 'b') {
            next();
            return atom.setValue('\u0008');
        } else if (c == '-') {
            return atom.setValue(next());
        }
        return characterClassEscape() || characterEscape(atom);
    }
    
    private long decimalDigits() {
        int c = next();
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
        } if (hasNext('k')) {
            next();
            return groupName(true) || fail();
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
        int c = peek();
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
        int c = peek();
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
        int c  = peek();
        if (Characters.isAsciiAlphabetic(c)) {
            next();
            return atom.setValue((char)(c % 32));
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
        String name = input().subSequence(nameStart, pos()).toString();
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
    
    private boolean hexEscapeSequence(ClassAtom atom) {
        if (!hasNext('x')) {
            return false;
        }
        next();
        int first = next();
        if (isHexDigit(first)) {
            int second = next();
            if (isHexDigit(second)) {
                int value = hexDigitToValue(first) * 16 + hexDigitToValue(second);
                return atom.setValue((char)value);
            }
        }
        return fail();
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
        final int start = pos(); 
        int c = peek();
        if (isRegExpIdentifierStart(c)) {
            next();
            return true;
        } else if (c == '\\') {
            next();
            ClassAtom atom = new ClassAtom();
            if (regExpUnicodeEscapeSequence(atom)) {
                if (isRegExpIdentifierStart(atom.getValue())) {
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
        final int start = pos(); 
        int c = peek();
        if (isRegExpIdentifierPart(c)) {
            next();
            return true;
        } else if (c == '\\') {
            next();
            ClassAtom atom = new ClassAtom();
            if (regExpUnicodeEscapeSequence(atom)) {
                if (isRegExpIdentifierPart(atom.getValue())) {
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

    protected boolean regExpUnicodeEscapeSequence(ClassAtom atom) {
        if (!hasNext('u')) {
            return false;
        }
        final int start = pos();
        next();
        final int value = hex4Digits();
        if (value >= 0) {
            return atom.setValue(value);
        } else {
            return backtrack(start);
        }
    }
    
    protected boolean identityEscape(ClassAtom atom) {
        int c = peek();
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
    
    protected int hex4Digits() {
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
                return value;
            }
        }
        backtrack(start);
        return -1;
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
    
    private static boolean isSyntaxCharacter(int c) {
        return syntaxCharSet.get(c);
    }
   
    private static boolean isNonZeroDigit(int c) {
        return c >= '1' && c <= '9';
    }
    
    protected static boolean isHexDigit(int c) {
        return Characters.isAsciiDigit(c) ||
               (c >= 'a' && c <= 'f') ||
               (c >= 'A' && c <= 'F');
    }
    
    private static boolean isRegExpIdentifierStart(int c) {
        return Character.isUnicodeIdentifierStart(c) || 
               c == '$' || 
               c == '_';
    }
    
    private static boolean isRegExpIdentifierPart(int c) {
        return Character.isUnicodeIdentifierPart(c) || 
               c == '$' ||
               c == '\u200c' || 
               c == '\u200d';
    }

    private static int digitToValue(int c) {
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
    
    private static boolean optional(boolean result) {
        return true;
    }
    
    /**
     * Instance of class atom.
     * 
     * @author leadpony
     */
    static class ClassAtom {

        private int codePoint;
        private boolean isCharacterClass = true;
        
        int getValue() {
            assert !isCharacterClass;
            return codePoint;
        }
        
        boolean setValue(int codePoint) {
            this.codePoint = codePoint;
            this.isCharacterClass = false;
            return true;
        }
        
        boolean isCharacterClass() {
            return isCharacterClass;
        }
    }
}
