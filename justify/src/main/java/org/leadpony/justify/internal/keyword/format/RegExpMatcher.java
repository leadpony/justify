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

package org.leadpony.justify.internal.keyword.format;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.leadpony.justify.internal.base.AsciiCode;

/**
 * The base matcher for ECMA262 regular expression pattern.
 *
 * @author leadpony
 */
abstract class RegExpMatcher extends AbstractFormatMatcher {

    @SuppressWarnings("serial")
    private static final BitSet SYNTAX_CHAR_SET = new BitSet() {
        {
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
        }
    };

    private int maxCapturingGroupNumber;
    private int leftCapturingParentheses;

    private Set<String> groups;
    private Set<String> groupReferences;

    protected int lastNumericValue;
    private ClassAtom lastClassAtom;

    /**
     * Constructs this matcher.
     *
     * @param input the input string.
     */
    RegExpMatcher(CharSequence input) {
        super(input);
    }

    @Override
    public boolean test() {
        disjunction();
        if (hasNext()) {
            return false;
        }
        checkCapturingNumber();
        checkGroupReferences();
        return true;
    }

    /**
     * Returns all group names.
     *
     * @return all names of the found groups.
     */
    Set<String> groupNames() {
        return groups;
    }

    /**
     * Early error:
     * <ul>
     * <li>It is a Syntax Error if Pattern contains multiple GroupSpecifiers whose
     * enclosed RegExpIdentifierNames have the same StringValue.</li>
     * </ul>
     */
    private void disjunction() {
        alternative();
        while (hasNext('|')) {
            next();
            alternative();
        }
    }

    /**
     * Alternative can be empty.
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
        final int mark = pos();
        int c = next();
        if (c == '^' || c == '$') {
            return true;
        } else if (c == '\\') {
            if (hasNext()) {
                c = next();
                if (c == 'b' || c == 'B') {
                    return true;
                }
            }
        } else if (c == '(' && hasNext('?')) {
            next();
            if (hasNext('<')) {
                next();
            }
            if (hasNext()) {
                c = next();
                if (c == '=' || c == '!') {
                    return capturingGroup();
                }
            }
        }
        return backtrack(mark);
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

    /**
     * Early error:
     * <ul>
     * <li>It is a Syntax Error if the MV of the first DecimalDigits is larger than
     * the MV of the second DecimalDigits.</li>
     * </ul>
     */
    private boolean quantifierPrefix() {
        if (!hasNext()) {
            return false;
        }
        final int mark = pos();
        int c = peek();
        if (c == '*' || c == '+' || c == '?') {
            next();
            return true;
        } else if (c == '{') {
            next();
            if (decimalDigits()) {
                final int first = this.lastNumericValue;
                if (hasNext()) {
                    c = next();
                    if (c == '}') {
                        return true;
                    } else if (c == ',') {
                        if (hasNext('}')) {
                            next();
                            return true;
                        } else if (decimalDigits()) {
                            final int second = this.lastNumericValue;
                            if (hasNext('}')) {
                                next();
                                return checkQuantifierRange(first, second);
                            }
                        }
                    }
                }
            }
        }
        return backtrack(mark);
    }

    private boolean atom() {
        if (patternCharacter() || characterClass()) {
            return true;
        }
        final int mark = pos();
        int c = peek();
        if (c == '.') {
            next();
            return true;
        } else if (c == '\\') {
            next();
            if (atomEscape()) {
                return true;
            }
        } else if (c == '(') {
            next();
            final int parenthesis = pos();
            // We should test "?:" pattern before GroupSpecifier production.
            if (hasNext('?')) {
                next();
                if (hasNext(':')) {
                    return capturingGroup();
                }
                backtrack(parenthesis);
            }
            if (groupSpecifier()) {
                return capturingGroup();
            }
        }
        return backtrack(mark);
    }

    protected boolean syntaxCharacter() {
        if (hasNext() && isSyntaxCharacter(peek())) {
            next();
            return true;
        }
        return false;
    }

    private boolean patternCharacter() {
        if (hasNext() && !isSyntaxCharacter(peek())) {
            next();
            return true;
        }
        return false;
    }

    /**
     * Early error:
     * <ul>
     * <li>It is a Syntax Error if the enclosing Pattern does not contain a
     * GroupSpecifier with an enclosed RegExpIdentifierName whose StringValue equals
     * the StringValue of the RegExpIdentifierName of this production's GroupName.
     * </li>
     * </ul>
     */
    private boolean atomEscape() {
        if (decimalEscape() || characterClassEscape()) {
            return true;
        } else if (characterEscape()) {
            return true;
        }
        if (hasNext('k')) {
            next();
            if (groupName(true)) {
                return true;
            }
        }
        return false;
    }

    private boolean characterEscape() {
        if (!hasNext()) {
            return false;
        }
        if (controlEscape()
                || hexEscapeSequence()
                || regExpUnicodeEscapeSequence()
                || identityEscape()) {
            return true;
        }
        final int mark = pos();
        int c = peek();
        if (c == 'c') {
            next();
            if (controlLetter()) {
                return true;
            }
        } else if (c == '0') {
            next();
            if (!hasNext() || !AsciiCode.isDigit(peek())) {
                return withClassAtomOf('\u0000');
            }
        }
        return backtrack(mark);
    }

    private boolean controlEscape() {
        if (!hasNext()) {
            return false;
        }
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
        return withClassAtomOf(value);
    }

    private boolean controlLetter() {
        if (!hasNext()) {
            return false;
        }
        int c = peek();
        if (isControlLetter(c)) {
            next();
            return withClassAtomOf(c % 32);
        }
        return false;
    }

    /**
     * group specifier can be empty.
     *
     * @return always {@code true}
     */
    private boolean groupSpecifier() {
        if (hasNext('?')) {
            final int mark = pos();
            next();
            if (groupName(false)) {
                return true;
            }
            backtrack(mark);
        }
        // empty
        return true;
    }

    private boolean groupName(boolean reference) {
        if (!hasNext('<')) {
            return false;
        }
        final int mark = pos();
        next();
        final int nameStart = pos();
        if (regExpIdentifierName()) {
            String name = extract(nameStart);
            if (hasNext('>')) {
                next();
                if (reference) {
                    addGroupReference(name);
                } else {
                    addGroup(name);
                }
                return true;
            }
        }
        return backtrack(mark);
    }

    private boolean regExpIdentifierName() {
        if (!regExpIdentifierStart()) {
            return false;
        }
        while (regExpIdentifierPart()) {
        }
        return true;
    }

    /**
     * Early error:
     * <ul>
     * <li>It is a Syntax Error if SV(RegExpUnicodeEscapeSequence) is none of "$",
     * or "_", or the UTF16Encoding of a code point matched by the UnicodeIDStart
     * lexical grammar production.</li>
     * </ul>
     */
    private boolean regExpIdentifierStart() {
        if (!hasNext()) {
            return false;
        }
        final int mark = pos();
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
                    return earlyError();
                }
            }
        }
        return backtrack(mark);
    }

    /**
     * Early error:
     * <ul>
     * <li>It is a Syntax Error if SV(RegExpUnicodeEscapeSequence) is none of "$",
     * or "_", or the UTF16Encoding of either <ZWNJ> or <ZWJ>, or the UTF16Encoding
     * of a Unicode code point that would be matched by the UnicodeIDContinue
     * lexical grammar production.</li>
     * </ul>
     */
    private boolean regExpIdentifierPart() {
        if (!hasNext()) {
            return false;
        }
        final int mark = pos();
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
                    earlyError();
                }
            }
        }
        return backtrack(mark);
    }

    protected boolean regExpUnicodeEscapeSequence() {
        if (!hasNext('u')) {
            return false;
        }
        final int mark = pos();
        next();
        if (hex4Digits()) {
            return withClassAtomOf(this.lastNumericValue);
        }
        return backtrack(mark);
    }

    protected abstract boolean identityEscape();

    /**
     * Early error:
     * <ul>
     * <li>It is a Syntax Error if the CapturingGroupNumber of DecimalEscape is
     * larger than NcapturingParens.</li>
     * </ul>
     */
    private boolean decimalEscape() {
        if (!hasNext() || !isNonZeroDigit(peek())) {
            return false;
        }
        int number = digitToValue(next());
        while (hasNext() && AsciiCode.isDigit(peek())) {
            number = number * 10 + digitToValue(next());
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
        }
        return false;
    }

    private boolean characterClass() {
        if (!hasNext('[')) {
            return false;
        }
        final int mark = pos();
        next();
        if (peek() == '^') {
            next();
        }
        classRanges();
        if (hasNext(']')) {
            next();
            return true;
        }
        return backtrack(mark);
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

    /**
     * Early error:
     * <ul>
     * <li>It is a Syntax Error if IsCharacterClass of the first ClassAtom is true
     * or IsCharacterClass of the second ClassAtom is true.</li>
     * <li>It is a Syntax Error if IsCharacterClass of the first ClassAtom is false
     * and IsCharacterClass of the second ClassAtom is false and the CharacterValue
     * of the first ClassAtom is larger than the CharacterValue of the second
     * ClassAtom.</li>
     * </ul>
     *
     * @return
     */
    private boolean nonemptyClassRanges() {
        if (classAtom()) {
            ClassAtom first = this.lastClassAtom;
            if (hasNext('-')) {
                final int mark = pos();
                next();
                if (classAtom()) {
                    ClassAtom second = this.lastClassAtom;
                    // This may throw early error.
                    return checkClassRange(first, second);
                }
                backtrack(mark);
            } else if (nonemptyClassRangesNoDash()) {
                return true;
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean nonemptyClassRangesNoDash() {
        final int mark = pos();
        if (classAtomNoDash()) {
            ClassAtom first = this.lastClassAtom;
            if (hasNext('-')) {
                next();
                if (classAtom()) {
                    ClassAtom second = this.lastClassAtom;
                    // This may throw early error.
                    return checkClassRange(first, second);
                }
            } else if (nonemptyClassRangesNoDash()) {
                return true;
            }
        }
        backtrack(mark);
        return classAtom();
    }

    private boolean classAtom() {
        if (hasNext('-')) {
            return withClassAtomOf(next());
        } else {
            return classAtomNoDash();
        }
    }

    private boolean classAtomNoDash() {
        if (!hasNext()) {
            return false;
        }
        int c = peek();
        if (c == '\\') {
            final int mark = pos();
            next();
            if (classEscape()) {
                return true;
            }
            return backtrack(mark);
        } else if (c == ']' || c == '-') {
            return false;
        } else {
            // SourceCharacter but not one of \ or ] or -
            return withClassAtomOf(next());
        }
    }

    private boolean classEscape() {
        if (!hasNext()) {
            return false;
        }
        int c = peek();
        if (c == 'b') {
            next();
            return withClassAtomOf('\u0008');
        } else if (c == '-') {
            return withClassAtomOf(next());
        }
        return characterClassEscape() || characterEscape();
    }

    private boolean decimalDigits() {
        if (!hasNext()) {
            return false;
        }
        int c = peek();
        if (AsciiCode.isDigit(c)) {
            next();
            int value = c - '0';
            while (hasNext() && AsciiCode.isDigit(peek())) {
                c = next();
                value = (value * 10) + (c - '0');
            }
            this.lastNumericValue = value;
            return true;
        }
        return false;
    }

    private boolean hexEscapeSequence() {
        if (!hasNext('x')) {
            return false;
        }
        final int mark = pos();
        next();
        if (hasNext()) {
            int first = next();
            if (AsciiCode.isHexDigit(first)) {
                if (hasNext()) {
                    int second = next();
                    if (AsciiCode.isHexDigit(second)) {
                        int value = AsciiCode.hexDigitToValue(first) * 16
                                + AsciiCode.hexDigitToValue(second);
                        return withClassAtomOf(value);
                    }
                }
            }
        }
        return backtrack(mark);
    }

    protected boolean hex4Digits() {
        final int mark = pos();
        int value = 0;
        int digits = 0;
        while (hasNext()) {
            int c = next();
            if (!AsciiCode.isHexDigit(c)) {
                break;
            }
            value = value * 16 + AsciiCode.hexDigitToValue(c);
            if (++digits >= 4) {
                this.lastNumericValue = value;
                return true;
            }
        }
        return backtrack(mark);
    }

    protected boolean testCharacterClassEscape() {
        int c = peek();
        if (c == 'd' || c == 'D'
                || c == 's' || c == 'S'
                || c == 'w' || c == 'W') {
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

    /**
     * Assigns a new class atom and returns {@code true}.
     *
     * @param codePoint the code point of the class atom.
     * @return {@code true}.
     */
    protected boolean withClassAtomOf(int codePoint) {
        this.lastClassAtom = ClassAtom.of(codePoint);
        return true;
    }

    private void addGroup(String name) {
        if (this.groups == null) {
            this.groups = new HashSet<>();
        }
        if (this.groups.contains(name)) {
            earlyError();
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
     * It is a Syntax Error if the MV of the first DecimalDigits is larger than the
     * MV of the second DecimalDigits.
     *
     * @param first  the first decimal digits.
     * @param second the second decimal digits.
     * @return {@code true} if the test passed.
     */
    private static boolean checkQuantifierRange(int first, int second) {
        if (first <= second) {
            return true;
        } else {
            return earlyError();
        }
    }

    private static boolean checkClassRange(ClassAtom lower, ClassAtom upper) {
        if (lower.isCharacterClass() || upper.isCharacterClass()) {
            return earlyError();
        } else if (lower.codePoint() > upper.codePoint()) {
            return earlyError();
        }
        return true;
    }

    private boolean checkCapturingNumber() {
        if (this.maxCapturingGroupNumber <= this.leftCapturingParentheses) {
            return true;
        } else {
            return earlyError();
        }
    }

    private boolean checkGroupReferences() {
        if (this.groupReferences == null) {
            return true;
        }
        for (String ref : this.groupReferences) {
            if (!findGroup(ref)) {
                return earlyError();
            }
        }
        return true;
    }

    protected static boolean isSyntaxCharacter(int c) {
        return SYNTAX_CHAR_SET.get(c);
    }

    protected static boolean isNonZeroDigit(int c) {
        return c >= '1' && c <= '9';
    }

    protected static boolean isRegExpIdentifierStart(int c) {
        return Character.isUnicodeIdentifierStart(c)
                || c == '$'
                || c == '_';
    }

    protected static boolean isRegExpIdentifierPart(int c) {
        return Character.isUnicodeIdentifierPart(c)
                || c == '$'
                || c == '\u200c'
                || c == '\u200d';
    }

    protected static boolean isControlLetter(int c) {
        return AsciiCode.isAlphabetic(c);
    }

    protected static int digitToValue(int c) {
        return (c - '0');
    }

    protected static boolean optional(boolean result) {
        return true;
    }

    /**
     * Throws early error defined in the specification.
     *
     * @return always {@code false}.
     */
    protected static boolean earlyError() {
        return fail();
    }

    /**
     * A class atom.
     *
     * @author leadpony
     */
    interface ClassAtom {

        boolean isCharacterClass();

        int codePoint();

        static ClassAtom of(int c) {
            return new DefaultClassAtom(c);
        }

        ClassAtom CHARACTER_CLASS = new ClassAtom() {

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
