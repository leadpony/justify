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

import java.lang.Character.UnicodeBlock;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.Normalizer2;

/**
 * IDN Property defined in <a href="https://tools.ietf.org/html/rfc5892">RFC
 * 5892</a>.
 *
 * @author leadpony
 */
enum IdnProperty {
    /**
     * Code points with this property value are permitted for general use in IDNs.
     */
    PVALID,
    /**
     * One of subdivision of CONTEXTUAL RULE REQUIRED. Some characteristics of the
     * character, such as it being invisible in certain contexts or problematic in
     * others, require that it not be used in labels unless specific other
     * characters or properties are present. This subdivision is for Join_controls.
     */
    CONTEXTJ,
    /**
     * Another subdivision of CONTEXTUAL RULE REQUIRED for other characters.
     */
    CONTEXTO,
    /**
     * Those that should clearly not be included in IDNs. Code points with this
     * property value are not permitted in IDNs.
     */
    DISALLOWED,
    /**
     * Those code points that are not designated (i.e., are unassigned) in the
     * Unicode Standard.
     */
    UNASSIGNED;

    /**
     * Calculates the property of the specified character.
     *
     * @param codePoint the code point of the character.
     * @return the IDN property calculated.
     */
    public static IdnProperty of(int codePoint) {
        IdnProperty property = asExceptional(codePoint);
        if (property != null) {
            return property;
        } else if (isUnassigned(codePoint)) {
            return UNASSIGNED;
        } else if (isLDH(codePoint)) {
            return PVALID;
        } else if (isJoinControl(codePoint)) {
            return CONTEXTJ;
        } else if (isUnstable(codePoint)) {
            return DISALLOWED;
        } else if (isIgnorableProperties(codePoint)) {
            return DISALLOWED;
        } else if (isIgnorableBlocks(codePoint)) {
            return DISALLOWED;
        } else if (isOldHangulJamo(codePoint)) {
            return DISALLOWED;
        } else if (isLetterDigit(codePoint)) {
            return PVALID;
        } else {
            return DISALLOWED;
        }
    }

    private static IdnProperty asExceptional(int codePoint) {
        switch (codePoint) {
        case 0x00DF: // LATIN SMALL LETTER SHARP S
        case 0x03C2: // GREEK SMALL LETTER FINAL SIGMA
        case 0x06FD: // ARABIC SIGN SINDHI AMPERSAND
        case 0x06FE: // ARABIC SIGN SINDHI POSTPOSITION MEN
        case 0x0F0B: // TIBETAN MARK INTERSYLLABIC TSHEG
        case 0x3007: // IDEOGRAPHIC NUMBER ZERO
            return PVALID;
        case 0x00B7: // MIDDLE DOT
        case 0x0375: // GREEK LOWER NUMERAL SIGN (KERAIA)
        case 0x05F3: // HEBREW PUNCTUATION GERESH
        case 0x05F4: // HEBREW PUNCTUATION GERSHAYIM
        case 0x30FB: // KATAKANA MIDDLE DOT
            return CONTEXTO;
        case 0x0660: // ARABIC-INDIC DIGIT ZERO
        case 0x0661: // ARABIC-INDIC DIGIT ONE
        case 0x0662: // ARABIC-INDIC DIGIT TWO
        case 0x0663: // ARABIC-INDIC DIGIT THREE
        case 0x0664: // ARABIC-INDIC DIGIT FOUR
        case 0x0665: // ARABIC-INDIC DIGIT FIVE
        case 0x0666: // ARABIC-INDIC DIGIT SIX
        case 0x0667: // ARABIC-INDIC DIGIT SEVEN
        case 0x0668: // ARABIC-INDIC DIGIT EIGHT
        case 0x0669: // ARABIC-INDIC DIGIT NINE
        case 0x06F0: // EXTENDED ARABIC-INDIC DIGIT ZERO
        case 0x06F1: // EXTENDED ARABIC-INDIC DIGIT ONE
        case 0x06F2: // EXTENDED ARABIC-INDIC DIGIT TWO
        case 0x06F3: // EXTENDED ARABIC-INDIC DIGIT THREE
        case 0x06F4: // EXTENDED ARABIC-INDIC DIGIT FOUR
        case 0x06F5: // EXTENDED ARABIC-INDIC DIGIT FIVE
        case 0x06F6: // EXTENDED ARABIC-INDIC DIGIT SIX
        case 0x06F7: // EXTENDED ARABIC-INDIC DIGIT SEVEN
        case 0x06F8: // EXTENDED ARABIC-INDIC DIGIT EIGHT
        case 0x06F9: // EXTENDED ARABIC-INDIC DIGIT NINE
            return CONTEXTO;
        case 0x0640: // ARABIC TATWEEL
        case 0x07FA: // NKO LAJANYALAN
        case 0x302E: // HANGUL SINGLE DOT TONE MARK
        case 0x302F: // HANGUL DOUBLE DOT TONE MARK
        case 0x3031: // VERTICAL KANA REPEAT MARK
        case 0x3032: // VERTICAL KANA REPEAT WITH VOICED SOUND MARK
        case 0x3033: // VERTICAL KANA REPEAT MARK UPPER HALF
        case 0x3034: // VERTICAL KANA REPEAT WITH VOICED SOUND MARK UPPER HA
        case 0x3035: // VERTICAL KANA REPEAT MARK LOWER HALF
        case 0x303B: // VERTICAL IDEOGRAPHIC ITERATION MARK
            return DISALLOWED;
        default:
            return null;
        }
    }

    private static boolean isLetterDigit(int codePoint) {
        final int type = Character.getType(codePoint);
        return type == Character.LOWERCASE_LETTER || // General category "Ll"
                type == Character.UPPERCASE_LETTER || // General category "Lu"
                type == Character.OTHER_LETTER || // General category "Lo"
                type == Character.DECIMAL_DIGIT_NUMBER || // General category "Nd"
                type == Character.MODIFIER_LETTER || // General category "Lm"
                type == Character.NON_SPACING_MARK || // General category "Mn"
                type == Character.COMBINING_SPACING_MARK; // General category "Mc"
    }

    private static boolean isUnassigned(int codePoint) {
        return Character.getType(codePoint) == Character.UNASSIGNED
                && !isNoncharacter(codePoint);
    }

    private static boolean isLDH(int codePoint) {
        return (codePoint == 0x002d) || // '-'
                (codePoint >= 0x0030 && codePoint <= 0x0039) || // '0' to '9'
                (codePoint >= 0x0061 && codePoint <= 0x007a); // 'a' to 'z'
    }

    private static boolean isJoinControl(int codePoint) {
        return codePoint == 0x200c || codePoint == 0x200d;
    }

    private static boolean isUnstable(int codePoint) {
        String original = String.valueOf(Character.toChars(codePoint));
        Normalizer2 normalizer = Normalizer2.getNFKCInstance();
        String normalized = normalizer.normalize(original);
        String folded = UCharacter.foldCase(normalized, UCharacter.FOLD_CASE_DEFAULT);
        String result = normalizer.normalize(folded);
        return !original.equals(result);
    }

    private static boolean isIgnorableProperties(int codePoint) {
        return isDefaultIgnorable(codePoint)
                || Character.isWhitespace(codePoint)
                || isNoncharacter(codePoint);
    }

    private static boolean isDefaultIgnorable(int codePoint) {
        return codePoint == 0x00AD
                || codePoint == 0x034F
                || (codePoint >= 0x115F && codePoint <= 0x1160)
                || (codePoint >= 0x17B4 && codePoint <= 0x17B5)
                || (codePoint >= 0x180B && codePoint <= 0x180D)
                || (codePoint >= 0x200B && codePoint <= 0x200F)
                || (codePoint >= 0x202A && codePoint <= 0x202E)
                || (codePoint >= 0x2060 && codePoint <= 0x2064)
                || (codePoint >= 0x2065 && codePoint <= 0x2069)
                || (codePoint >= 0x206A && codePoint <= 0x206F)
                || codePoint == 0x3164
                || (codePoint >= 0xFE00 && codePoint <= 0xFE0F)
                || codePoint == 0xFEFF
                || codePoint == 0xFFA0
                || (codePoint >= 0xFFF0 && codePoint <= 0xFFF8)
                || (codePoint >= 0x1D173 && codePoint <= 0x1D17A)
                || (codePoint >= 0xe0000 && codePoint <= 0xe0fff);
    }

    private static boolean isIgnorableBlocks(int codePoint) {
        UnicodeBlock block = UnicodeBlock.of(codePoint);
        return block == UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS
                || block == UnicodeBlock.MUSICAL_SYMBOLS
                || block == UnicodeBlock.ANCIENT_GREEK_MUSICAL_NOTATION;
    }

    private static boolean isOldHangulJamo(int codePoint) {
        // Hangul_Syllable_Type=Leading_Jamo
        if ((0x1100 <= codePoint && codePoint <= 0x115F)
                || (0xA960 <= codePoint && codePoint <= 0xA97C)) {
            return true;
        }
        // Hangul_Syllable_Type=Vowel_Jamo
        if ((0x1160 <= codePoint && codePoint <= 0x11A7)
                || (0xD7B0 <= codePoint && codePoint <= 0xD7C6)) {
            return true;
        }
        // Hangul_Syllable_Type=Trailing_Jamo
        if ((0x11A8 <= codePoint && codePoint <= 0x11FF)
                || (0xD7CB <= codePoint && codePoint <= 0xD7FB)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the specified character is a noncharacter or not. There are 66
     * noncharacters defined in the Unicode specification.
     *
     * @param codePoint the code point of the character.
     * @return {@code true} if the specified character is a noncharacter.
     */
    private static boolean isNoncharacter(int codePoint) {
        if (codePoint >= 0xfdd0 && codePoint <= 0xfdef) {
            return true;
        }
        int lower = codePoint & 0x0ffff;
        return (lower == 0xfffe || lower == 0xffff);
    }
}
