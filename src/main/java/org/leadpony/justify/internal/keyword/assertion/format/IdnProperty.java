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

import java.lang.Character.UnicodeBlock;
import java.util.Arrays;

/**
 * IDN Property defined in <a href="https://tools.ietf.org/html/rfc5892">RFC 5892</a>.
 * 
 * @author leadpony
 */
public enum IdnProperty {
    /**
     * Code points with this property value are permitted for general use in IDNs.
     */
    PVALID,
    /**
     * One of subdivision of CONTEXTUAL RULE REQUIRED.
     * Some characteristics of the character, such as it being invisible 
     * in certain contexts or problematic in others, require that it not be used
     * in labels unless specific other characters or properties are present.
     * This subdivision is for Join_controls.
     */
    CONTEXTJ,
    /**
     * Another subdivision of CONTEXTUAL RULE REQUIRED for other characters.
     */
    CONTEXTO,
    /**
     * Those that should clearly not be included in IDNs.
     * Code points with this property value are not permitted in IDNs.
     */
    DISALLOWED,
    /**
     * Those code points that are not designated 
     * (i.e., are unassigned) in the Unicode Standard.
     */
    UNASSIGNED
    ;
    
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
        case 0x00DF:    // LATIN SMALL LETTER SHARP S
        case 0x03C2:    // GREEK SMALL LETTER FINAL SIGMA
        case 0x06FD:    // ARABIC SIGN SINDHI AMPERSAND
        case 0x06FE:    // ARABIC SIGN SINDHI POSTPOSITION MEN
        case 0x0F0B:    // TIBETAN MARK INTERSYLLABIC TSHEG
        case 0x3007:    // IDEOGRAPHIC NUMBER ZERO
            return PVALID;
        case 0x00B7:    // MIDDLE DOT
        case 0x0375:    // GREEK LOWER NUMERAL SIGN (KERAIA)
        case 0x05F3:    // HEBREW PUNCTUATION GERESH
        case 0x05F4:    // HEBREW PUNCTUATION GERSHAYIM
        case 0x30FB:    // KATAKANA MIDDLE DOT
            return CONTEXTO;
        case 0x0660:    // ARABIC-INDIC DIGIT ZERO
        case 0x0661:    // ARABIC-INDIC DIGIT ONE
        case 0x0662:    // ARABIC-INDIC DIGIT TWO
        case 0x0663:    // ARABIC-INDIC DIGIT THREE
        case 0x0664:    // ARABIC-INDIC DIGIT FOUR
        case 0x0665:    // ARABIC-INDIC DIGIT FIVE
        case 0x0666:    // ARABIC-INDIC DIGIT SIX
        case 0x0667:    // ARABIC-INDIC DIGIT SEVEN
        case 0x0668:    // ARABIC-INDIC DIGIT EIGHT
        case 0x0669:    // ARABIC-INDIC DIGIT NINE
        case 0x06F0:    // EXTENDED ARABIC-INDIC DIGIT ZERO
        case 0x06F1:    // EXTENDED ARABIC-INDIC DIGIT ONE
        case 0x06F2:    // EXTENDED ARABIC-INDIC DIGIT TWO
        case 0x06F3:    // EXTENDED ARABIC-INDIC DIGIT THREE
        case 0x06F4:    // EXTENDED ARABIC-INDIC DIGIT FOUR
        case 0x06F5:    // EXTENDED ARABIC-INDIC DIGIT FIVE
        case 0x06F6:    // EXTENDED ARABIC-INDIC DIGIT SIX
        case 0x06F7:    // EXTENDED ARABIC-INDIC DIGIT SEVEN
        case 0x06F8:    // EXTENDED ARABIC-INDIC DIGIT EIGHT
        case 0x06F9:    // EXTENDED ARABIC-INDIC DIGIT NINE
            return CONTEXTO;
        case 0x0640:    // ARABIC TATWEEL
        case 0x07FA:    // NKO LAJANYALAN
        case 0x302E:    // HANGUL SINGLE DOT TONE MARK
        case 0x302F:    // HANGUL DOUBLE DOT TONE MARK
        case 0x3031:    // VERTICAL KANA REPEAT MARK
        case 0x3032:    // VERTICAL KANA REPEAT WITH VOICED SOUND MARK
        case 0x3033:    // VERTICAL KANA REPEAT MARK UPPER HALF
        case 0x3034:    // VERTICAL KANA REPEAT WITH VOICED SOUND MARK UPPER HA
        case 0x3035:    // VERTICAL KANA REPEAT MARK LOWER HALF
        case 0x303B:    // VERTICAL IDEOGRAPHIC ITERATION MARK
            return DISALLOWED;
        default:
            return null;
        }
    }
    
    private static boolean isLetterDigit(int codePoint) {
        final int type = Character.getType(codePoint);
        return type == Character.LOWERCASE_LETTER ||        // General category "Ll"
               type == Character.UPPERCASE_LETTER ||        // General category "Lu"
               type == Character.OTHER_LETTER ||            // General category "Lo" 
               type == Character.DECIMAL_DIGIT_NUMBER ||    // General category "Nd"
               type == Character.MODIFIER_LETTER ||         // General category "Lm" 
               type == Character.NON_SPACING_MARK ||        // General category "Mn"
               type == Character.COMBINING_SPACING_MARK;    // General category "Mc"
    }
    
    private static boolean isUnassigned(int codePoint) {
        return Character.getType(codePoint) == Character.UNASSIGNED &&
               !isNoncharacter(codePoint); 
    }
    
    private static boolean isLDH(int codePoint) {
        return (codePoint == 0x002d) || // '-'
               (codePoint >= 0x0030 && codePoint <= 0x0039) || // '0' to '9'
               (codePoint >= 0x0061 && codePoint <= 0x007a);   // 'a' to 'z' 
    }
    
    private static boolean isJoinControl(int codePoint) {
        return codePoint == 0x200c || codePoint == 0x200d;
    }

    private static boolean isUnstable(int codePoint) {
        // Only BMP code points are supported.
        if (Character.isSupplementaryCodePoint(codePoint)) {
            return false;
        }
        int index = Arrays.binarySearch(unstableCodePoints, (char)codePoint);
        return index >= 0;
    }
    
    private static boolean isIgnorableProperties(int codePoint) {
        return isDefaultIgnorable(codePoint) ||
               Character.isWhitespace(codePoint) ||
               isNoncharacter(codePoint);
    }
    
    private static boolean isDefaultIgnorable(int codePoint) {
        return  codePoint == 0x00AD ||
                codePoint == 0x034F ||  
               (codePoint >= 0x115F &&  codePoint <= 0x1160) ||
               (codePoint >= 0x17B4 &&  codePoint <= 0x17B5) ||
               (codePoint >= 0x180B &&  codePoint <= 0x180D) ||
               (codePoint >= 0x200B &&  codePoint <= 0x200F) ||
               (codePoint >= 0x202A && codePoint <= 0x202E) ||
               (codePoint >= 0x2060 && codePoint <= 0x2064) ||
               (codePoint >= 0x2065 && codePoint <= 0x2069) ||
               (codePoint >= 0x206A && codePoint <= 0x206F) ||
                codePoint == 0x3164 ||
               (codePoint >= 0xFE00 && codePoint <= 0xFE0F) ||
                codePoint == 0xFEFF ||
                codePoint == 0xFFA0 ||
               (codePoint >= 0xFFF0 && codePoint <= 0xFFF8) ||
               (codePoint >= 0x1D173 && codePoint <= 0x1D17A) ||
               (codePoint >= 0xe0000 && codePoint <= 0xe0fff);
    }
    
    private static boolean isIgnorableBlocks(int codePoint) {
        UnicodeBlock block = UnicodeBlock.of(codePoint);
        return block == UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS  ||
               block == UnicodeBlock.MUSICAL_SYMBOLS ||
               block == UnicodeBlock.ANCIENT_GREEK_MUSICAL_NOTATION;
    }
    
    private static boolean isOldHangulJamo(int codePoint) {
        // TODO:
        return false;
    }
    
    /**
     * Checks if the specified character is a noncharacter or not.
     * There are 66 noncharacters defined in the Unicode specification.
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
    
    /**
     * Unstable code points in BMP.
     */
    private static final char[] unstableCodePoints = {
            '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047', '\u0048', 
            '\u0049', '\u004a', '\u004b', '\u004c', '\u004d', '\u004e', '\u004f', '\u0050', 
            '\u0051', '\u0052', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057', '\u0058', 
            '\u0059', '\u005a', '\u00a0', '\u00a8', '\u00aa', '\u00af', '\u00b2', '\u00b3', 
            '\u00b4', '\u00b5', '\u00b8', '\u00b9', '\u00ba', '\u00bc', '\u00bd', '\u00be', 
            '\u00c0', '\u00c1', '\u00c2', '\u00c3', '\u00c4', '\u00c5', '\u00c6', '\u00c7', 
            '\u00c8', '\u00c9', '\u00ca', '\u00cb', '\u00cc', '\u00cd', '\u00ce', '\u00cf', 
            '\u00d0', '\u00d1', '\u00d2', '\u00d3', '\u00d4', '\u00d5', '\u00d6', '\u00d8', 
            '\u00d9', '\u00da', '\u00db', '\u00dc', '\u00dd', '\u00de', '\u00df', '\u0100', 
            '\u0102', '\u0104', '\u0106', '\u0108', '\u010a', '\u010c', '\u010e', '\u0110', 
            '\u0112', '\u0114', '\u0116', '\u0118', '\u011a', '\u011c', '\u011e', '\u0120', 
            '\u0122', '\u0124', '\u0126', '\u0128', '\u012a', '\u012c', '\u012e', '\u0130', 
            '\u0132', '\u0133', '\u0134', '\u0136', '\u0139', '\u013b', '\u013d', '\u013f', 
            '\u0140', '\u0141', '\u0143', '\u0145', '\u0147', '\u0149', '\u014a', '\u014c', 
            '\u014e', '\u0150', '\u0152', '\u0154', '\u0156', '\u0158', '\u015a', '\u015c', 
            '\u015e', '\u0160', '\u0162', '\u0164', '\u0166', '\u0168', '\u016a', '\u016c', 
            '\u016e', '\u0170', '\u0172', '\u0174', '\u0176', '\u0178', '\u0179', '\u017b', 
            '\u017d', '\u017f', '\u0181', '\u0182', '\u0184', '\u0186', '\u0187', '\u0189', 
            '\u018a', '\u018b', '\u018e', '\u018f', '\u0190', '\u0191', '\u0193', '\u0194', 
            '\u0196', '\u0197', '\u0198', '\u019c', '\u019d', '\u019f', '\u01a0', '\u01a2', 
            '\u01a4', '\u01a6', '\u01a7', '\u01a9', '\u01ac', '\u01ae', '\u01af', '\u01b1', 
            '\u01b2', '\u01b3', '\u01b5', '\u01b7', '\u01b8', '\u01bc', '\u01c4', '\u01c5', 
            '\u01c6', '\u01c7', '\u01c8', '\u01c9', '\u01ca', '\u01cb', '\u01cc', '\u01cd', 
            '\u01cf', '\u01d1', '\u01d3', '\u01d5', '\u01d7', '\u01d9', '\u01db', '\u01de', 
            '\u01e0', '\u01e2', '\u01e4', '\u01e6', '\u01e8', '\u01ea', '\u01ec', '\u01ee', 
            '\u01f1', '\u01f2', '\u01f3', '\u01f4', '\u01f6', '\u01f7', '\u01f8', '\u01fa', 
            '\u01fc', '\u01fe', '\u0200', '\u0202', '\u0204', '\u0206', '\u0208', '\u020a', 
            '\u020c', '\u020e', '\u0210', '\u0212', '\u0214', '\u0216', '\u0218', '\u021a', 
            '\u021c', '\u021e', '\u0220', '\u0222', '\u0224', '\u0226', '\u0228', '\u022a', 
            '\u022c', '\u022e', '\u0230', '\u0232', '\u023a', '\u023b', '\u023d', '\u023e', 
            '\u0241', '\u0243', '\u0244', '\u0245', '\u0246', '\u0248', '\u024a', '\u024c', 
            '\u024e', '\u02b0', '\u02b1', '\u02b2', '\u02b3', '\u02b4', '\u02b5', '\u02b6', 
            '\u02b7', '\u02b8', '\u02d8', '\u02d9', '\u02da', '\u02db', '\u02dc', '\u02dd', 
            '\u02e0', '\u02e1', '\u02e2', '\u02e3', '\u02e4', '\u0340', '\u0341', '\u0343', 
            '\u0344', '\u0345', '\u0370', '\u0372', '\u0374', '\u0376', '\u037a', '\u037e', 
            '\u037f', '\u0384', '\u0385', '\u0386', '\u0387', '\u0388', '\u0389', '\u038a', 
            '\u038c', '\u038e', '\u038f', '\u0391', '\u0392', '\u0393', '\u0394', '\u0395', 
            '\u0396', '\u0397', '\u0398', '\u0399', '\u039a', '\u039b', '\u039c', '\u039d', 
            '\u039e', '\u039f', '\u03a0', '\u03a1', '\u03a3', '\u03a4', '\u03a5', '\u03a6', 
            '\u03a7', '\u03a8', '\u03a9', '\u03aa', '\u03ab', '\u03c2', '\u03cf', '\u03d0', 
            '\u03d1', '\u03d2', '\u03d3', '\u03d4', '\u03d5', '\u03d6', '\u03d8', '\u03da', 
            '\u03dc', '\u03de', '\u03e0', '\u03e2', '\u03e4', '\u03e6', '\u03e8', '\u03ea', 
            '\u03ec', '\u03ee', '\u03f0', '\u03f1', '\u03f2', '\u03f4', '\u03f5', '\u03f7', 
            '\u03f9', '\u03fa', '\u03fd', '\u03fe', '\u03ff', '\u0400', '\u0401', '\u0402', 
            '\u0403', '\u0404', '\u0405', '\u0406', '\u0407', '\u0408', '\u0409', '\u040a', 
            '\u040b', '\u040c', '\u040d', '\u040e', '\u040f', '\u0410', '\u0411', '\u0412', 
            '\u0413', '\u0414', '\u0415', '\u0416', '\u0417', '\u0418', '\u0419', '\u041a', 
            '\u041b', '\u041c', '\u041d', '\u041e', '\u041f', '\u0420', '\u0421', '\u0422', 
            '\u0423', '\u0424', '\u0425', '\u0426', '\u0427', '\u0428', '\u0429', '\u042a', 
            '\u042b', '\u042c', '\u042d', '\u042e', '\u042f', '\u0460', '\u0462', '\u0464', 
            '\u0466', '\u0468', '\u046a', '\u046c', '\u046e', '\u0470', '\u0472', '\u0474', 
            '\u0476', '\u0478', '\u047a', '\u047c', '\u047e', '\u0480', '\u048a', '\u048c', 
            '\u048e', '\u0490', '\u0492', '\u0494', '\u0496', '\u0498', '\u049a', '\u049c', 
            '\u049e', '\u04a0', '\u04a2', '\u04a4', '\u04a6', '\u04a8', '\u04aa', '\u04ac', 
            '\u04ae', '\u04b0', '\u04b2', '\u04b4', '\u04b6', '\u04b8', '\u04ba', '\u04bc', 
            '\u04be', '\u04c0', '\u04c1', '\u04c3', '\u04c5', '\u04c7', '\u04c9', '\u04cb', 
            '\u04cd', '\u04d0', '\u04d2', '\u04d4', '\u04d6', '\u04d8', '\u04da', '\u04dc', 
            '\u04de', '\u04e0', '\u04e2', '\u04e4', '\u04e6', '\u04e8', '\u04ea', '\u04ec', 
            '\u04ee', '\u04f0', '\u04f2', '\u04f4', '\u04f6', '\u04f8', '\u04fa', '\u04fc', 
            '\u04fe', '\u0500', '\u0502', '\u0504', '\u0506', '\u0508', '\u050a', '\u050c', 
            '\u050e', '\u0510', '\u0512', '\u0514', '\u0516', '\u0518', '\u051a', '\u051c', 
            '\u051e', '\u0520', '\u0522', '\u0524', '\u0526', '\u0528', '\u052a', '\u052c', 
            '\u052e', '\u0531', '\u0532', '\u0533', '\u0534', '\u0535', '\u0536', '\u0537', 
            '\u0538', '\u0539', '\u053a', '\u053b', '\u053c', '\u053d', '\u053e', '\u053f', 
            '\u0540', '\u0541', '\u0542', '\u0543', '\u0544', '\u0545', '\u0546', '\u0547', 
            '\u0548', '\u0549', '\u054a', '\u054b', '\u054c', '\u054d', '\u054e', '\u054f', 
            '\u0550', '\u0551', '\u0552', '\u0553', '\u0554', '\u0555', '\u0556', '\u0587', 
            '\u0675', '\u0676', '\u0677', '\u0678', '\u0958', '\u0959', '\u095a', '\u095b', 
            '\u095c', '\u095d', '\u095e', '\u095f', '\u09dc', '\u09dd', '\u09df', '\u0a33', 
            '\u0a36', '\u0a59', '\u0a5a', '\u0a5b', '\u0a5e', '\u0b5c', '\u0b5d', '\u0e33', 
            '\u0eb3', '\u0edc', '\u0edd', '\u0f0c', '\u0f43', '\u0f4d', '\u0f52', '\u0f57', 
            '\u0f5c', '\u0f69', '\u0f73', '\u0f75', '\u0f76', '\u0f77', '\u0f78', '\u0f79', 
            '\u0f81', '\u0f93', '\u0f9d', '\u0fa2', '\u0fa7', '\u0fac', '\u0fb9', '\u10a0', 
            '\u10a1', '\u10a2', '\u10a3', '\u10a4', '\u10a5', '\u10a6', '\u10a7', '\u10a8', 
            '\u10a9', '\u10aa', '\u10ab', '\u10ac', '\u10ad', '\u10ae', '\u10af', '\u10b0', 
            '\u10b1', '\u10b2', '\u10b3', '\u10b4', '\u10b5', '\u10b6', '\u10b7', '\u10b8', 
            '\u10b9', '\u10ba', '\u10bb', '\u10bc', '\u10bd', '\u10be', '\u10bf', '\u10c0', 
            '\u10c1', '\u10c2', '\u10c3', '\u10c4', '\u10c5', '\u10c7', '\u10cd', '\u10fc', 
            '\u13f8', '\u13f9', '\u13fa', '\u13fb', '\u13fc', '\u13fd', '\u1c80', '\u1c81', 
            '\u1c82', '\u1c83', '\u1c84', '\u1c85', '\u1c86', '\u1c87', '\u1c88', '\u1c90', 
            '\u1c91', '\u1c92', '\u1c93', '\u1c94', '\u1c95', '\u1c96', '\u1c97', '\u1c98', 
            '\u1c99', '\u1c9a', '\u1c9b', '\u1c9c', '\u1c9d', '\u1c9e', '\u1c9f', '\u1ca0', 
            '\u1ca1', '\u1ca2', '\u1ca3', '\u1ca4', '\u1ca5', '\u1ca6', '\u1ca7', '\u1ca8', 
            '\u1ca9', '\u1caa', '\u1cab', '\u1cac', '\u1cad', '\u1cae', '\u1caf', '\u1cb0', 
            '\u1cb1', '\u1cb2', '\u1cb3', '\u1cb4', '\u1cb5', '\u1cb6', '\u1cb7', '\u1cb8', 
            '\u1cb9', '\u1cba', '\u1cbd', '\u1cbe', '\u1cbf', '\u1d2c', '\u1d2d', '\u1d2e', 
            '\u1d30', '\u1d31', '\u1d32', '\u1d33', '\u1d34', '\u1d35', '\u1d36', '\u1d37', 
            '\u1d38', '\u1d39', '\u1d3a', '\u1d3c', '\u1d3d', '\u1d3e', '\u1d3f', '\u1d40', 
            '\u1d41', '\u1d42', '\u1d43', '\u1d44', '\u1d45', '\u1d46', '\u1d47', '\u1d48', 
            '\u1d49', '\u1d4a', '\u1d4b', '\u1d4c', '\u1d4d', '\u1d4f', '\u1d50', '\u1d51', 
            '\u1d52', '\u1d53', '\u1d54', '\u1d55', '\u1d56', '\u1d57', '\u1d58', '\u1d59', 
            '\u1d5a', '\u1d5b', '\u1d5c', '\u1d5d', '\u1d5e', '\u1d5f', '\u1d60', '\u1d61', 
            '\u1d62', '\u1d63', '\u1d64', '\u1d65', '\u1d66', '\u1d67', '\u1d68', '\u1d69', 
            '\u1d6a', '\u1d78', '\u1d9b', '\u1d9c', '\u1d9d', '\u1d9e', '\u1d9f', '\u1da0', 
            '\u1da1', '\u1da2', '\u1da3', '\u1da4', '\u1da5', '\u1da6', '\u1da7', '\u1da8', 
            '\u1da9', '\u1daa', '\u1dab', '\u1dac', '\u1dad', '\u1dae', '\u1daf', '\u1db0', 
            '\u1db1', '\u1db2', '\u1db3', '\u1db4', '\u1db5', '\u1db6', '\u1db7', '\u1db8', 
            '\u1db9', '\u1dba', '\u1dbb', '\u1dbc', '\u1dbd', '\u1dbe', '\u1dbf', '\u1e00', 
            '\u1e02', '\u1e04', '\u1e06', '\u1e08', '\u1e0a', '\u1e0c', '\u1e0e', '\u1e10', 
            '\u1e12', '\u1e14', '\u1e16', '\u1e18', '\u1e1a', '\u1e1c', '\u1e1e', '\u1e20', 
            '\u1e22', '\u1e24', '\u1e26', '\u1e28', '\u1e2a', '\u1e2c', '\u1e2e', '\u1e30', 
            '\u1e32', '\u1e34', '\u1e36', '\u1e38', '\u1e3a', '\u1e3c', '\u1e3e', '\u1e40', 
            '\u1e42', '\u1e44', '\u1e46', '\u1e48', '\u1e4a', '\u1e4c', '\u1e4e', '\u1e50', 
            '\u1e52', '\u1e54', '\u1e56', '\u1e58', '\u1e5a', '\u1e5c', '\u1e5e', '\u1e60', 
            '\u1e62', '\u1e64', '\u1e66', '\u1e68', '\u1e6a', '\u1e6c', '\u1e6e', '\u1e70', 
            '\u1e72', '\u1e74', '\u1e76', '\u1e78', '\u1e7a', '\u1e7c', '\u1e7e', '\u1e80', 
            '\u1e82', '\u1e84', '\u1e86', '\u1e88', '\u1e8a', '\u1e8c', '\u1e8e', '\u1e90', 
            '\u1e92', '\u1e94', '\u1e9a', '\u1e9b', '\u1e9e', '\u1ea0', '\u1ea2', '\u1ea4', 
            '\u1ea6', '\u1ea8', '\u1eaa', '\u1eac', '\u1eae', '\u1eb0', '\u1eb2', '\u1eb4', 
            '\u1eb6', '\u1eb8', '\u1eba', '\u1ebc', '\u1ebe', '\u1ec0', '\u1ec2', '\u1ec4', 
            '\u1ec6', '\u1ec8', '\u1eca', '\u1ecc', '\u1ece', '\u1ed0', '\u1ed2', '\u1ed4', 
            '\u1ed6', '\u1ed8', '\u1eda', '\u1edc', '\u1ede', '\u1ee0', '\u1ee2', '\u1ee4', 
            '\u1ee6', '\u1ee8', '\u1eea', '\u1eec', '\u1eee', '\u1ef0', '\u1ef2', '\u1ef4', 
            '\u1ef6', '\u1ef8', '\u1efa', '\u1efc', '\u1efe', '\u1f08', '\u1f09', '\u1f0a', 
            '\u1f0b', '\u1f0c', '\u1f0d', '\u1f0e', '\u1f0f', '\u1f18', '\u1f19', '\u1f1a', 
            '\u1f1b', '\u1f1c', '\u1f1d', '\u1f28', '\u1f29', '\u1f2a', '\u1f2b', '\u1f2c', 
            '\u1f2d', '\u1f2e', '\u1f2f', '\u1f38', '\u1f39', '\u1f3a', '\u1f3b', '\u1f3c', 
            '\u1f3d', '\u1f3e', '\u1f3f', '\u1f48', '\u1f49', '\u1f4a', '\u1f4b', '\u1f4c', 
            '\u1f4d', '\u1f59', '\u1f5b', '\u1f5d', '\u1f5f', '\u1f68', '\u1f69', '\u1f6a', 
            '\u1f6b', '\u1f6c', '\u1f6d', '\u1f6e', '\u1f6f', '\u1f71', '\u1f73', '\u1f75', 
            '\u1f77', '\u1f79', '\u1f7b', '\u1f7d', '\u1f80', '\u1f81', '\u1f82', '\u1f83', 
            '\u1f84', '\u1f85', '\u1f86', '\u1f87', '\u1f88', '\u1f89', '\u1f8a', '\u1f8b', 
            '\u1f8c', '\u1f8d', '\u1f8e', '\u1f8f', '\u1f90', '\u1f91', '\u1f92', '\u1f93', 
            '\u1f94', '\u1f95', '\u1f96', '\u1f97', '\u1f98', '\u1f99', '\u1f9a', '\u1f9b', 
            '\u1f9c', '\u1f9d', '\u1f9e', '\u1f9f', '\u1fa0', '\u1fa1', '\u1fa2', '\u1fa3', 
            '\u1fa4', '\u1fa5', '\u1fa6', '\u1fa7', '\u1fa8', '\u1fa9', '\u1faa', '\u1fab', 
            '\u1fac', '\u1fad', '\u1fae', '\u1faf', '\u1fb2', '\u1fb3', '\u1fb4', '\u1fb7', 
            '\u1fb8', '\u1fb9', '\u1fba', '\u1fbb', '\u1fbc', '\u1fbd', '\u1fbe', '\u1fbf', 
            '\u1fc0', '\u1fc1', '\u1fc2', '\u1fc3', '\u1fc4', '\u1fc7', '\u1fc8', '\u1fc9', 
            '\u1fca', '\u1fcb', '\u1fcc', '\u1fcd', '\u1fce', '\u1fcf', '\u1fd3', '\u1fd8', 
            '\u1fd9', '\u1fda', '\u1fdb', '\u1fdd', '\u1fde', '\u1fdf', '\u1fe3', '\u1fe8', 
            '\u1fe9', '\u1fea', '\u1feb', '\u1fec', '\u1fed', '\u1fee', '\u1fef', '\u1ff2', 
            '\u1ff3', '\u1ff4', '\u1ff7', '\u1ff8', '\u1ff9', '\u1ffa', '\u1ffb', '\u1ffc', 
            '\u1ffd', '\u1ffe', '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', 
            '\u2006', '\u2007', '\u2008', '\u2009', '\u200a', '\u2011', '\u2017', '\u2024', 
            '\u2025', '\u2026', '\u202f', '\u2033', '\u2034', '\u2036', '\u2037', '\u203c', 
            '\u203e', '\u2047', '\u2048', '\u2049', '\u2057', '\u205f', '\u2070', '\u2071', 
            '\u2074', '\u2075', '\u2076', '\u2077', '\u2078', '\u2079', '\u207a', '\u207b', 
            '\u207c', '\u207d', '\u207e', '\u207f', '\u2080', '\u2081', '\u2082', '\u2083', 
            '\u2084', '\u2085', '\u2086', '\u2087', '\u2088', '\u2089', '\u208a', '\u208b', 
            '\u208c', '\u208d', '\u208e', '\u2090', '\u2091', '\u2092', '\u2093', '\u2094', 
            '\u2095', '\u2096', '\u2097', '\u2098', '\u2099', '\u209a', '\u209b', '\u209c', 
            '\u20a8', '\u2100', '\u2101', '\u2102', '\u2103', '\u2105', '\u2106', '\u2107', 
            '\u2109', '\u210a', '\u210b', '\u210c', '\u210d', '\u210e', '\u210f', '\u2110', 
            '\u2111', '\u2112', '\u2113', '\u2115', '\u2116', '\u2119', '\u211a', '\u211b', 
            '\u211c', '\u211d', '\u2120', '\u2121', '\u2122', '\u2124', '\u2126', '\u2128', 
            '\u212a', '\u212b', '\u212c', '\u212d', '\u212f', '\u2130', '\u2131', '\u2132', 
            '\u2133', '\u2134', '\u2135', '\u2136', '\u2137', '\u2138', '\u2139', '\u213b', 
            '\u213c', '\u213d', '\u213e', '\u213f', '\u2140', '\u2145', '\u2146', '\u2147', 
            '\u2148', '\u2149', '\u2150', '\u2151', '\u2152', '\u2153', '\u2154', '\u2155', 
            '\u2156', '\u2157', '\u2158', '\u2159', '\u215a', '\u215b', '\u215c', '\u215d', 
            '\u215e', '\u215f', '\u2160', '\u2161', '\u2162', '\u2163', '\u2164', '\u2165', 
            '\u2166', '\u2167', '\u2168', '\u2169', '\u216a', '\u216b', '\u216c', '\u216d', 
            '\u216e', '\u216f', '\u2170', '\u2171', '\u2172', '\u2173', '\u2174', '\u2175', 
            '\u2176', '\u2177', '\u2178', '\u2179', '\u217a', '\u217b', '\u217c', '\u217d', 
            '\u217e', '\u217f', '\u2183', '\u2189', '\u222c', '\u222d', '\u222f', '\u2230', 
            '\u2329', '\u232a', '\u2460', '\u2461', '\u2462', '\u2463', '\u2464', '\u2465', 
            '\u2466', '\u2467', '\u2468', '\u2469', '\u246a', '\u246b', '\u246c', '\u246d', 
            '\u246e', '\u246f', '\u2470', '\u2471', '\u2472', '\u2473', '\u2474', '\u2475', 
            '\u2476', '\u2477', '\u2478', '\u2479', '\u247a', '\u247b', '\u247c', '\u247d', 
            '\u247e', '\u247f', '\u2480', '\u2481', '\u2482', '\u2483', '\u2484', '\u2485', 
            '\u2486', '\u2487', '\u2488', '\u2489', '\u248a', '\u248b', '\u248c', '\u248d', 
            '\u248e', '\u248f', '\u2490', '\u2491', '\u2492', '\u2493', '\u2494', '\u2495', 
            '\u2496', '\u2497', '\u2498', '\u2499', '\u249a', '\u249b', '\u249c', '\u249d', 
            '\u249e', '\u249f', '\u24a0', '\u24a1', '\u24a2', '\u24a3', '\u24a4', '\u24a5', 
            '\u24a6', '\u24a7', '\u24a8', '\u24a9', '\u24aa', '\u24ab', '\u24ac', '\u24ad', 
            '\u24ae', '\u24af', '\u24b0', '\u24b1', '\u24b2', '\u24b3', '\u24b4', '\u24b5', 
            '\u24b6', '\u24b7', '\u24b8', '\u24b9', '\u24ba', '\u24bb', '\u24bc', '\u24bd', 
            '\u24be', '\u24bf', '\u24c0', '\u24c1', '\u24c2', '\u24c3', '\u24c4', '\u24c5', 
            '\u24c6', '\u24c7', '\u24c8', '\u24c9', '\u24ca', '\u24cb', '\u24cc', '\u24cd', 
            '\u24ce', '\u24cf', '\u24d0', '\u24d1', '\u24d2', '\u24d3', '\u24d4', '\u24d5', 
            '\u24d6', '\u24d7', '\u24d8', '\u24d9', '\u24da', '\u24db', '\u24dc', '\u24dd', 
            '\u24de', '\u24df', '\u24e0', '\u24e1', '\u24e2', '\u24e3', '\u24e4', '\u24e5', 
            '\u24e6', '\u24e7', '\u24e8', '\u24e9', '\u24ea', '\u2a0c', '\u2a74', '\u2a75', 
            '\u2a76', '\u2adc', '\u2c00', '\u2c01', '\u2c02', '\u2c03', '\u2c04', '\u2c05', 
            '\u2c06', '\u2c07', '\u2c08', '\u2c09', '\u2c0a', '\u2c0b', '\u2c0c', '\u2c0d', 
            '\u2c0e', '\u2c0f', '\u2c10', '\u2c11', '\u2c12', '\u2c13', '\u2c14', '\u2c15', 
            '\u2c16', '\u2c17', '\u2c18', '\u2c19', '\u2c1a', '\u2c1b', '\u2c1c', '\u2c1d', 
            '\u2c1e', '\u2c1f', '\u2c20', '\u2c21', '\u2c22', '\u2c23', '\u2c24', '\u2c25', 
            '\u2c26', '\u2c27', '\u2c28', '\u2c29', '\u2c2a', '\u2c2b', '\u2c2c', '\u2c2d', 
            '\u2c2e', '\u2c60', '\u2c62', '\u2c63', '\u2c64', '\u2c67', '\u2c69', '\u2c6b', 
            '\u2c6d', '\u2c6e', '\u2c6f', '\u2c70', '\u2c72', '\u2c75', '\u2c7c', '\u2c7d', 
            '\u2c7e', '\u2c7f', '\u2c80', '\u2c82', '\u2c84', '\u2c86', '\u2c88', '\u2c8a', 
            '\u2c8c', '\u2c8e', '\u2c90', '\u2c92', '\u2c94', '\u2c96', '\u2c98', '\u2c9a', 
            '\u2c9c', '\u2c9e', '\u2ca0', '\u2ca2', '\u2ca4', '\u2ca6', '\u2ca8', '\u2caa', 
            '\u2cac', '\u2cae', '\u2cb0', '\u2cb2', '\u2cb4', '\u2cb6', '\u2cb8', '\u2cba', 
            '\u2cbc', '\u2cbe', '\u2cc0', '\u2cc2', '\u2cc4', '\u2cc6', '\u2cc8', '\u2cca', 
            '\u2ccc', '\u2cce', '\u2cd0', '\u2cd2', '\u2cd4', '\u2cd6', '\u2cd8', '\u2cda', 
            '\u2cdc', '\u2cde', '\u2ce0', '\u2ce2', '\u2ceb', '\u2ced', '\u2cf2', '\u2d6f', 
            '\u2e9f', '\u2ef3', '\u2f00', '\u2f01', '\u2f02', '\u2f03', '\u2f04', '\u2f05', 
            '\u2f06', '\u2f07', '\u2f08', '\u2f09', '\u2f0a', '\u2f0b', '\u2f0c', '\u2f0d', 
            '\u2f0e', '\u2f0f', '\u2f10', '\u2f11', '\u2f12', '\u2f13', '\u2f14', '\u2f15', 
            '\u2f16', '\u2f17', '\u2f18', '\u2f19', '\u2f1a', '\u2f1b', '\u2f1c', '\u2f1d', 
            '\u2f1e', '\u2f1f', '\u2f20', '\u2f21', '\u2f22', '\u2f23', '\u2f24', '\u2f25', 
            '\u2f26', '\u2f27', '\u2f28', '\u2f29', '\u2f2a', '\u2f2b', '\u2f2c', '\u2f2d', 
            '\u2f2e', '\u2f2f', '\u2f30', '\u2f31', '\u2f32', '\u2f33', '\u2f34', '\u2f35', 
            '\u2f36', '\u2f37', '\u2f38', '\u2f39', '\u2f3a', '\u2f3b', '\u2f3c', '\u2f3d', 
            '\u2f3e', '\u2f3f', '\u2f40', '\u2f41', '\u2f42', '\u2f43', '\u2f44', '\u2f45', 
            '\u2f46', '\u2f47', '\u2f48', '\u2f49', '\u2f4a', '\u2f4b', '\u2f4c', '\u2f4d', 
            '\u2f4e', '\u2f4f', '\u2f50', '\u2f51', '\u2f52', '\u2f53', '\u2f54', '\u2f55', 
            '\u2f56', '\u2f57', '\u2f58', '\u2f59', '\u2f5a', '\u2f5b', '\u2f5c', '\u2f5d', 
            '\u2f5e', '\u2f5f', '\u2f60', '\u2f61', '\u2f62', '\u2f63', '\u2f64', '\u2f65', 
            '\u2f66', '\u2f67', '\u2f68', '\u2f69', '\u2f6a', '\u2f6b', '\u2f6c', '\u2f6d', 
            '\u2f6e', '\u2f6f', '\u2f70', '\u2f71', '\u2f72', '\u2f73', '\u2f74', '\u2f75', 
            '\u2f76', '\u2f77', '\u2f78', '\u2f79', '\u2f7a', '\u2f7b', '\u2f7c', '\u2f7d', 
            '\u2f7e', '\u2f7f', '\u2f80', '\u2f81', '\u2f82', '\u2f83', '\u2f84', '\u2f85', 
            '\u2f86', '\u2f87', '\u2f88', '\u2f89', '\u2f8a', '\u2f8b', '\u2f8c', '\u2f8d', 
            '\u2f8e', '\u2f8f', '\u2f90', '\u2f91', '\u2f92', '\u2f93', '\u2f94', '\u2f95', 
            '\u2f96', '\u2f97', '\u2f98', '\u2f99', '\u2f9a', '\u2f9b', '\u2f9c', '\u2f9d', 
            '\u2f9e', '\u2f9f', '\u2fa0', '\u2fa1', '\u2fa2', '\u2fa3', '\u2fa4', '\u2fa5', 
            '\u2fa6', '\u2fa7', '\u2fa8', '\u2fa9', '\u2faa', '\u2fab', '\u2fac', '\u2fad', 
            '\u2fae', '\u2faf', '\u2fb0', '\u2fb1', '\u2fb2', '\u2fb3', '\u2fb4', '\u2fb5', 
            '\u2fb6', '\u2fb7', '\u2fb8', '\u2fb9', '\u2fba', '\u2fbb', '\u2fbc', '\u2fbd', 
            '\u2fbe', '\u2fbf', '\u2fc0', '\u2fc1', '\u2fc2', '\u2fc3', '\u2fc4', '\u2fc5', 
            '\u2fc6', '\u2fc7', '\u2fc8', '\u2fc9', '\u2fca', '\u2fcb', '\u2fcc', '\u2fcd', 
            '\u2fce', '\u2fcf', '\u2fd0', '\u2fd1', '\u2fd2', '\u2fd3', '\u2fd4', '\u2fd5', 
            '\u3000', '\u3036', '\u3038', '\u3039', '\u303a', '\u309b', '\u309c', '\u309f', 
            '\u30ff', '\u3131', '\u3132', '\u3133', '\u3134', '\u3135', '\u3136', '\u3137', 
            '\u3138', '\u3139', '\u313a', '\u313b', '\u313c', '\u313d', '\u313e', '\u313f', 
            '\u3140', '\u3141', '\u3142', '\u3143', '\u3144', '\u3145', '\u3146', '\u3147', 
            '\u3148', '\u3149', '\u314a', '\u314b', '\u314c', '\u314d', '\u314e', '\u314f', 
            '\u3150', '\u3151', '\u3152', '\u3153', '\u3154', '\u3155', '\u3156', '\u3157', 
            '\u3158', '\u3159', '\u315a', '\u315b', '\u315c', '\u315d', '\u315e', '\u315f', 
            '\u3160', '\u3161', '\u3162', '\u3163', '\u3164', '\u3165', '\u3166', '\u3167', 
            '\u3168', '\u3169', '\u316a', '\u316b', '\u316c', '\u316d', '\u316e', '\u316f', 
            '\u3170', '\u3171', '\u3172', '\u3173', '\u3174', '\u3175', '\u3176', '\u3177', 
            '\u3178', '\u3179', '\u317a', '\u317b', '\u317c', '\u317d', '\u317e', '\u317f', 
            '\u3180', '\u3181', '\u3182', '\u3183', '\u3184', '\u3185', '\u3186', '\u3187', 
            '\u3188', '\u3189', '\u318a', '\u318b', '\u318c', '\u318d', '\u318e', '\u3192', 
            '\u3193', '\u3194', '\u3195', '\u3196', '\u3197', '\u3198', '\u3199', '\u319a', 
            '\u319b', '\u319c', '\u319d', '\u319e', '\u319f', '\u3200', '\u3201', '\u3202', 
            '\u3203', '\u3204', '\u3205', '\u3206', '\u3207', '\u3208', '\u3209', '\u320a', 
            '\u320b', '\u320c', '\u320d', '\u320e', '\u320f', '\u3210', '\u3211', '\u3212', 
            '\u3213', '\u3214', '\u3215', '\u3216', '\u3217', '\u3218', '\u3219', '\u321a', 
            '\u321b', '\u321c', '\u321d', '\u321e', '\u3220', '\u3221', '\u3222', '\u3223', 
            '\u3224', '\u3225', '\u3226', '\u3227', '\u3228', '\u3229', '\u322a', '\u322b', 
            '\u322c', '\u322d', '\u322e', '\u322f', '\u3230', '\u3231', '\u3232', '\u3233', 
            '\u3234', '\u3235', '\u3236', '\u3237', '\u3238', '\u3239', '\u323a', '\u323b', 
            '\u323c', '\u323d', '\u323e', '\u323f', '\u3240', '\u3241', '\u3242', '\u3243', 
            '\u3244', '\u3245', '\u3246', '\u3247', '\u3250', '\u3251', '\u3252', '\u3253', 
            '\u3254', '\u3255', '\u3256', '\u3257', '\u3258', '\u3259', '\u325a', '\u325b', 
            '\u325c', '\u325d', '\u325e', '\u325f', '\u3260', '\u3261', '\u3262', '\u3263', 
            '\u3264', '\u3265', '\u3266', '\u3267', '\u3268', '\u3269', '\u326a', '\u326b', 
            '\u326c', '\u326d', '\u326e', '\u326f', '\u3270', '\u3271', '\u3272', '\u3273', 
            '\u3274', '\u3275', '\u3276', '\u3277', '\u3278', '\u3279', '\u327a', '\u327b', 
            '\u327c', '\u327d', '\u327e', '\u3280', '\u3281', '\u3282', '\u3283', '\u3284', 
            '\u3285', '\u3286', '\u3287', '\u3288', '\u3289', '\u328a', '\u328b', '\u328c', 
            '\u328d', '\u328e', '\u328f', '\u3290', '\u3291', '\u3292', '\u3293', '\u3294', 
            '\u3295', '\u3296', '\u3297', '\u3298', '\u3299', '\u329a', '\u329b', '\u329c', 
            '\u329d', '\u329e', '\u329f', '\u32a0', '\u32a1', '\u32a2', '\u32a3', '\u32a4', 
            '\u32a5', '\u32a6', '\u32a7', '\u32a8', '\u32a9', '\u32aa', '\u32ab', '\u32ac', 
            '\u32ad', '\u32ae', '\u32af', '\u32b0', '\u32b1', '\u32b2', '\u32b3', '\u32b4', 
            '\u32b5', '\u32b6', '\u32b7', '\u32b8', '\u32b9', '\u32ba', '\u32bb', '\u32bc', 
            '\u32bd', '\u32be', '\u32bf', '\u32c0', '\u32c1', '\u32c2', '\u32c3', '\u32c4', 
            '\u32c5', '\u32c6', '\u32c7', '\u32c8', '\u32c9', '\u32ca', '\u32cb', '\u32cc', 
            '\u32cd', '\u32ce', '\u32cf', '\u32d0', '\u32d1', '\u32d2', '\u32d3', '\u32d4', 
            '\u32d5', '\u32d6', '\u32d7', '\u32d8', '\u32d9', '\u32da', '\u32db', '\u32dc', 
            '\u32dd', '\u32de', '\u32df', '\u32e0', '\u32e1', '\u32e2', '\u32e3', '\u32e4', 
            '\u32e5', '\u32e6', '\u32e7', '\u32e8', '\u32e9', '\u32ea', '\u32eb', '\u32ec', 
            '\u32ed', '\u32ee', '\u32ef', '\u32f0', '\u32f1', '\u32f2', '\u32f3', '\u32f4', 
            '\u32f5', '\u32f6', '\u32f7', '\u32f8', '\u32f9', '\u32fa', '\u32fb', '\u32fc', 
            '\u32fd', '\u32fe', '\u3300', '\u3301', '\u3302', '\u3303', '\u3304', '\u3305', 
            '\u3306', '\u3307', '\u3308', '\u3309', '\u330a', '\u330b', '\u330c', '\u330d', 
            '\u330e', '\u330f', '\u3310', '\u3311', '\u3312', '\u3313', '\u3314', '\u3315', 
            '\u3316', '\u3317', '\u3318', '\u3319', '\u331a', '\u331b', '\u331c', '\u331d', 
            '\u331e', '\u331f', '\u3320', '\u3321', '\u3322', '\u3323', '\u3324', '\u3325', 
            '\u3326', '\u3327', '\u3328', '\u3329', '\u332a', '\u332b', '\u332c', '\u332d', 
            '\u332e', '\u332f', '\u3330', '\u3331', '\u3332', '\u3333', '\u3334', '\u3335', 
            '\u3336', '\u3337', '\u3338', '\u3339', '\u333a', '\u333b', '\u333c', '\u333d', 
            '\u333e', '\u333f', '\u3340', '\u3341', '\u3342', '\u3343', '\u3344', '\u3345', 
            '\u3346', '\u3347', '\u3348', '\u3349', '\u334a', '\u334b', '\u334c', '\u334d', 
            '\u334e', '\u334f', '\u3350', '\u3351', '\u3352', '\u3353', '\u3354', '\u3355', 
            '\u3356', '\u3357', '\u3358', '\u3359', '\u335a', '\u335b', '\u335c', '\u335d', 
            '\u335e', '\u335f', '\u3360', '\u3361', '\u3362', '\u3363', '\u3364', '\u3365', 
            '\u3366', '\u3367', '\u3368', '\u3369', '\u336a', '\u336b', '\u336c', '\u336d', 
            '\u336e', '\u336f', '\u3370', '\u3371', '\u3372', '\u3373', '\u3374', '\u3375', 
            '\u3376', '\u3377', '\u3378', '\u3379', '\u337a', '\u337b', '\u337c', '\u337d', 
            '\u337e', '\u337f', '\u3380', '\u3381', '\u3382', '\u3383', '\u3384', '\u3385', 
            '\u3386', '\u3387', '\u3388', '\u3389', '\u338a', '\u338b', '\u338c', '\u338d', 
            '\u338e', '\u338f', '\u3390', '\u3391', '\u3392', '\u3393', '\u3394', '\u3395', 
            '\u3396', '\u3397', '\u3398', '\u3399', '\u339a', '\u339b', '\u339c', '\u339d', 
            '\u339e', '\u339f', '\u33a0', '\u33a1', '\u33a2', '\u33a3', '\u33a4', '\u33a5', 
            '\u33a6', '\u33a7', '\u33a8', '\u33a9', '\u33aa', '\u33ab', '\u33ac', '\u33ad', 
            '\u33ae', '\u33af', '\u33b0', '\u33b1', '\u33b2', '\u33b3', '\u33b4', '\u33b5', 
            '\u33b6', '\u33b7', '\u33b8', '\u33b9', '\u33ba', '\u33bb', '\u33bc', '\u33bd', 
            '\u33be', '\u33bf', '\u33c0', '\u33c1', '\u33c2', '\u33c3', '\u33c4', '\u33c5', 
            '\u33c6', '\u33c7', '\u33c8', '\u33c9', '\u33ca', '\u33cb', '\u33cc', '\u33cd', 
            '\u33ce', '\u33cf', '\u33d0', '\u33d1', '\u33d2', '\u33d3', '\u33d4', '\u33d5', 
            '\u33d6', '\u33d7', '\u33d8', '\u33d9', '\u33da', '\u33db', '\u33dc', '\u33dd', 
            '\u33de', '\u33df', '\u33e0', '\u33e1', '\u33e2', '\u33e3', '\u33e4', '\u33e5', 
            '\u33e6', '\u33e7', '\u33e8', '\u33e9', '\u33ea', '\u33eb', '\u33ec', '\u33ed', 
            '\u33ee', '\u33ef', '\u33f0', '\u33f1', '\u33f2', '\u33f3', '\u33f4', '\u33f5', 
            '\u33f6', '\u33f7', '\u33f8', '\u33f9', '\u33fa', '\u33fb', '\u33fc', '\u33fd', 
            '\u33fe', '\u33ff', '\ua640', '\ua642', '\ua644', '\ua646', '\ua648', '\ua64a', 
            '\ua64c', '\ua64e', '\ua650', '\ua652', '\ua654', '\ua656', '\ua658', '\ua65a', 
            '\ua65c', '\ua65e', '\ua660', '\ua662', '\ua664', '\ua666', '\ua668', '\ua66a', 
            '\ua66c', '\ua680', '\ua682', '\ua684', '\ua686', '\ua688', '\ua68a', '\ua68c', 
            '\ua68e', '\ua690', '\ua692', '\ua694', '\ua696', '\ua698', '\ua69a', '\ua69c', 
            '\ua69d', '\ua722', '\ua724', '\ua726', '\ua728', '\ua72a', '\ua72c', '\ua72e', 
            '\ua732', '\ua734', '\ua736', '\ua738', '\ua73a', '\ua73c', '\ua73e', '\ua740', 
            '\ua742', '\ua744', '\ua746', '\ua748', '\ua74a', '\ua74c', '\ua74e', '\ua750', 
            '\ua752', '\ua754', '\ua756', '\ua758', '\ua75a', '\ua75c', '\ua75e', '\ua760', 
            '\ua762', '\ua764', '\ua766', '\ua768', '\ua76a', '\ua76c', '\ua76e', '\ua770', 
            '\ua779', '\ua77b', '\ua77d', '\ua77e', '\ua780', '\ua782', '\ua784', '\ua786', 
            '\ua78b', '\ua78d', '\ua790', '\ua792', '\ua796', '\ua798', '\ua79a', '\ua79c', 
            '\ua79e', '\ua7a0', '\ua7a2', '\ua7a4', '\ua7a6', '\ua7a8', '\ua7aa', '\ua7ab', 
            '\ua7ac', '\ua7ad', '\ua7ae', '\ua7b0', '\ua7b1', '\ua7b2', '\ua7b3', '\ua7b4', 
            '\ua7b6', '\ua7b8', '\ua7f8', '\ua7f9', '\uab5c', '\uab5d', '\uab5e', '\uab5f', 
            '\uab70', '\uab71', '\uab72', '\uab73', '\uab74', '\uab75', '\uab76', '\uab77', 
            '\uab78', '\uab79', '\uab7a', '\uab7b', '\uab7c', '\uab7d', '\uab7e', '\uab7f', 
            '\uab80', '\uab81', '\uab82', '\uab83', '\uab84', '\uab85', '\uab86', '\uab87', 
            '\uab88', '\uab89', '\uab8a', '\uab8b', '\uab8c', '\uab8d', '\uab8e', '\uab8f', 
            '\uab90', '\uab91', '\uab92', '\uab93', '\uab94', '\uab95', '\uab96', '\uab97', 
            '\uab98', '\uab99', '\uab9a', '\uab9b', '\uab9c', '\uab9d', '\uab9e', '\uab9f', 
            '\uaba0', '\uaba1', '\uaba2', '\uaba3', '\uaba4', '\uaba5', '\uaba6', '\uaba7', 
            '\uaba8', '\uaba9', '\uabaa', '\uabab', '\uabac', '\uabad', '\uabae', '\uabaf', 
            '\uabb0', '\uabb1', '\uabb2', '\uabb3', '\uabb4', '\uabb5', '\uabb6', '\uabb7', 
            '\uabb8', '\uabb9', '\uabba', '\uabbb', '\uabbc', '\uabbd', '\uabbe', '\uabbf', 
            '\uf900', '\uf901', '\uf902', '\uf903', '\uf904', '\uf905', '\uf906', '\uf907', 
            '\uf908', '\uf909', '\uf90a', '\uf90b', '\uf90c', '\uf90d', '\uf90e', '\uf90f', 
            '\uf910', '\uf911', '\uf912', '\uf913', '\uf914', '\uf915', '\uf916', '\uf917', 
            '\uf918', '\uf919', '\uf91a', '\uf91b', '\uf91c', '\uf91d', '\uf91e', '\uf91f', 
            '\uf920', '\uf921', '\uf922', '\uf923', '\uf924', '\uf925', '\uf926', '\uf927', 
            '\uf928', '\uf929', '\uf92a', '\uf92b', '\uf92c', '\uf92d', '\uf92e', '\uf92f', 
            '\uf930', '\uf931', '\uf932', '\uf933', '\uf934', '\uf935', '\uf936', '\uf937', 
            '\uf938', '\uf939', '\uf93a', '\uf93b', '\uf93c', '\uf93d', '\uf93e', '\uf93f', 
            '\uf940', '\uf941', '\uf942', '\uf943', '\uf944', '\uf945', '\uf946', '\uf947', 
            '\uf948', '\uf949', '\uf94a', '\uf94b', '\uf94c', '\uf94d', '\uf94e', '\uf94f', 
            '\uf950', '\uf951', '\uf952', '\uf953', '\uf954', '\uf955', '\uf956', '\uf957', 
            '\uf958', '\uf959', '\uf95a', '\uf95b', '\uf95c', '\uf95d', '\uf95e', '\uf95f', 
            '\uf960', '\uf961', '\uf962', '\uf963', '\uf964', '\uf965', '\uf966', '\uf967', 
            '\uf968', '\uf969', '\uf96a', '\uf96b', '\uf96c', '\uf96d', '\uf96e', '\uf96f', 
            '\uf970', '\uf971', '\uf972', '\uf973', '\uf974', '\uf975', '\uf976', '\uf977', 
            '\uf978', '\uf979', '\uf97a', '\uf97b', '\uf97c', '\uf97d', '\uf97e', '\uf97f', 
            '\uf980', '\uf981', '\uf982', '\uf983', '\uf984', '\uf985', '\uf986', '\uf987', 
            '\uf988', '\uf989', '\uf98a', '\uf98b', '\uf98c', '\uf98d', '\uf98e', '\uf98f', 
            '\uf990', '\uf991', '\uf992', '\uf993', '\uf994', '\uf995', '\uf996', '\uf997', 
            '\uf998', '\uf999', '\uf99a', '\uf99b', '\uf99c', '\uf99d', '\uf99e', '\uf99f', 
            '\uf9a0', '\uf9a1', '\uf9a2', '\uf9a3', '\uf9a4', '\uf9a5', '\uf9a6', '\uf9a7', 
            '\uf9a8', '\uf9a9', '\uf9aa', '\uf9ab', '\uf9ac', '\uf9ad', '\uf9ae', '\uf9af', 
            '\uf9b0', '\uf9b1', '\uf9b2', '\uf9b3', '\uf9b4', '\uf9b5', '\uf9b6', '\uf9b7', 
            '\uf9b8', '\uf9b9', '\uf9ba', '\uf9bb', '\uf9bc', '\uf9bd', '\uf9be', '\uf9bf', 
            '\uf9c0', '\uf9c1', '\uf9c2', '\uf9c3', '\uf9c4', '\uf9c5', '\uf9c6', '\uf9c7', 
            '\uf9c8', '\uf9c9', '\uf9ca', '\uf9cb', '\uf9cc', '\uf9cd', '\uf9ce', '\uf9cf', 
            '\uf9d0', '\uf9d1', '\uf9d2', '\uf9d3', '\uf9d4', '\uf9d5', '\uf9d6', '\uf9d7', 
            '\uf9d8', '\uf9d9', '\uf9da', '\uf9db', '\uf9dc', '\uf9dd', '\uf9de', '\uf9df', 
            '\uf9e0', '\uf9e1', '\uf9e2', '\uf9e3', '\uf9e4', '\uf9e5', '\uf9e6', '\uf9e7', 
            '\uf9e8', '\uf9e9', '\uf9ea', '\uf9eb', '\uf9ec', '\uf9ed', '\uf9ee', '\uf9ef', 
            '\uf9f0', '\uf9f1', '\uf9f2', '\uf9f3', '\uf9f4', '\uf9f5', '\uf9f6', '\uf9f7', 
            '\uf9f8', '\uf9f9', '\uf9fa', '\uf9fb', '\uf9fc', '\uf9fd', '\uf9fe', '\uf9ff', 
            '\ufa00', '\ufa01', '\ufa02', '\ufa03', '\ufa04', '\ufa05', '\ufa06', '\ufa07', 
            '\ufa08', '\ufa09', '\ufa0a', '\ufa0b', '\ufa0c', '\ufa0d', '\ufa10', '\ufa12', 
            '\ufa15', '\ufa16', '\ufa17', '\ufa18', '\ufa19', '\ufa1a', '\ufa1b', '\ufa1c', 
            '\ufa1d', '\ufa1e', '\ufa20', '\ufa22', '\ufa25', '\ufa26', '\ufa2a', '\ufa2b', 
            '\ufa2c', '\ufa2d', '\ufa2e', '\ufa2f', '\ufa30', '\ufa31', '\ufa32', '\ufa33', 
            '\ufa34', '\ufa35', '\ufa36', '\ufa37', '\ufa38', '\ufa39', '\ufa3a', '\ufa3b', 
            '\ufa3c', '\ufa3d', '\ufa3e', '\ufa3f', '\ufa40', '\ufa41', '\ufa42', '\ufa43', 
            '\ufa44', '\ufa45', '\ufa46', '\ufa47', '\ufa48', '\ufa49', '\ufa4a', '\ufa4b', 
            '\ufa4c', '\ufa4d', '\ufa4e', '\ufa4f', '\ufa50', '\ufa51', '\ufa52', '\ufa53', 
            '\ufa54', '\ufa55', '\ufa56', '\ufa57', '\ufa58', '\ufa59', '\ufa5a', '\ufa5b', 
            '\ufa5c', '\ufa5d', '\ufa5e', '\ufa5f', '\ufa60', '\ufa61', '\ufa62', '\ufa63', 
            '\ufa64', '\ufa65', '\ufa66', '\ufa67', '\ufa68', '\ufa69', '\ufa6a', '\ufa6b', 
            '\ufa6c', '\ufa6d', '\ufa70', '\ufa71', '\ufa72', '\ufa73', '\ufa74', '\ufa75', 
            '\ufa76', '\ufa77', '\ufa78', '\ufa79', '\ufa7a', '\ufa7b', '\ufa7c', '\ufa7d', 
            '\ufa7e', '\ufa7f', '\ufa80', '\ufa81', '\ufa82', '\ufa83', '\ufa84', '\ufa85', 
            '\ufa86', '\ufa87', '\ufa88', '\ufa89', '\ufa8a', '\ufa8b', '\ufa8c', '\ufa8d', 
            '\ufa8e', '\ufa8f', '\ufa90', '\ufa91', '\ufa92', '\ufa93', '\ufa94', '\ufa95', 
            '\ufa96', '\ufa97', '\ufa98', '\ufa99', '\ufa9a', '\ufa9b', '\ufa9c', '\ufa9d', 
            '\ufa9e', '\ufa9f', '\ufaa0', '\ufaa1', '\ufaa2', '\ufaa3', '\ufaa4', '\ufaa5', 
            '\ufaa6', '\ufaa7', '\ufaa8', '\ufaa9', '\ufaaa', '\ufaab', '\ufaac', '\ufaad', 
            '\ufaae', '\ufaaf', '\ufab0', '\ufab1', '\ufab2', '\ufab3', '\ufab4', '\ufab5', 
            '\ufab6', '\ufab7', '\ufab8', '\ufab9', '\ufaba', '\ufabb', '\ufabc', '\ufabd', 
            '\ufabe', '\ufabf', '\ufac0', '\ufac1', '\ufac2', '\ufac3', '\ufac4', '\ufac5', 
            '\ufac6', '\ufac7', '\ufac8', '\ufac9', '\ufaca', '\ufacb', '\ufacc', '\ufacd', 
            '\uface', '\ufacf', '\ufad0', '\ufad1', '\ufad2', '\ufad3', '\ufad4', '\ufad5', 
            '\ufad6', '\ufad7', '\ufad8', '\ufad9', '\ufb00', '\ufb01', '\ufb02', '\ufb03', 
            '\ufb04', '\ufb05', '\ufb06', '\ufb13', '\ufb14', '\ufb15', '\ufb16', '\ufb17', 
            '\ufb1d', '\ufb1f', '\ufb20', '\ufb21', '\ufb22', '\ufb23', '\ufb24', '\ufb25', 
            '\ufb26', '\ufb27', '\ufb28', '\ufb29', '\ufb2a', '\ufb2b', '\ufb2c', '\ufb2d', 
            '\ufb2e', '\ufb2f', '\ufb30', '\ufb31', '\ufb32', '\ufb33', '\ufb34', '\ufb35', 
            '\ufb36', '\ufb38', '\ufb39', '\ufb3a', '\ufb3b', '\ufb3c', '\ufb3e', '\ufb40', 
            '\ufb41', '\ufb43', '\ufb44', '\ufb46', '\ufb47', '\ufb48', '\ufb49', '\ufb4a', 
            '\ufb4b', '\ufb4c', '\ufb4d', '\ufb4e', '\ufb4f', '\ufb50', '\ufb51', '\ufb52', 
            '\ufb53', '\ufb54', '\ufb55', '\ufb56', '\ufb57', '\ufb58', '\ufb59', '\ufb5a', 
            '\ufb5b', '\ufb5c', '\ufb5d', '\ufb5e', '\ufb5f', '\ufb60', '\ufb61', '\ufb62', 
            '\ufb63', '\ufb64', '\ufb65', '\ufb66', '\ufb67', '\ufb68', '\ufb69', '\ufb6a', 
            '\ufb6b', '\ufb6c', '\ufb6d', '\ufb6e', '\ufb6f', '\ufb70', '\ufb71', '\ufb72', 
            '\ufb73', '\ufb74', '\ufb75', '\ufb76', '\ufb77', '\ufb78', '\ufb79', '\ufb7a', 
            '\ufb7b', '\ufb7c', '\ufb7d', '\ufb7e', '\ufb7f', '\ufb80', '\ufb81', '\ufb82', 
            '\ufb83', '\ufb84', '\ufb85', '\ufb86', '\ufb87', '\ufb88', '\ufb89', '\ufb8a', 
            '\ufb8b', '\ufb8c', '\ufb8d', '\ufb8e', '\ufb8f', '\ufb90', '\ufb91', '\ufb92', 
            '\ufb93', '\ufb94', '\ufb95', '\ufb96', '\ufb97', '\ufb98', '\ufb99', '\ufb9a', 
            '\ufb9b', '\ufb9c', '\ufb9d', '\ufb9e', '\ufb9f', '\ufba0', '\ufba1', '\ufba2', 
            '\ufba3', '\ufba4', '\ufba5', '\ufba6', '\ufba7', '\ufba8', '\ufba9', '\ufbaa', 
            '\ufbab', '\ufbac', '\ufbad', '\ufbae', '\ufbaf', '\ufbb0', '\ufbb1', '\ufbd3', 
            '\ufbd4', '\ufbd5', '\ufbd6', '\ufbd7', '\ufbd8', '\ufbd9', '\ufbda', '\ufbdb', 
            '\ufbdc', '\ufbdd', '\ufbde', '\ufbdf', '\ufbe0', '\ufbe1', '\ufbe2', '\ufbe3', 
            '\ufbe4', '\ufbe5', '\ufbe6', '\ufbe7', '\ufbe8', '\ufbe9', '\ufbea', '\ufbeb', 
            '\ufbec', '\ufbed', '\ufbee', '\ufbef', '\ufbf0', '\ufbf1', '\ufbf2', '\ufbf3', 
            '\ufbf4', '\ufbf5', '\ufbf6', '\ufbf7', '\ufbf8', '\ufbf9', '\ufbfa', '\ufbfb', 
            '\ufbfc', '\ufbfd', '\ufbfe', '\ufbff', '\ufc00', '\ufc01', '\ufc02', '\ufc03', 
            '\ufc04', '\ufc05', '\ufc06', '\ufc07', '\ufc08', '\ufc09', '\ufc0a', '\ufc0b', 
            '\ufc0c', '\ufc0d', '\ufc0e', '\ufc0f', '\ufc10', '\ufc11', '\ufc12', '\ufc13', 
            '\ufc14', '\ufc15', '\ufc16', '\ufc17', '\ufc18', '\ufc19', '\ufc1a', '\ufc1b', 
            '\ufc1c', '\ufc1d', '\ufc1e', '\ufc1f', '\ufc20', '\ufc21', '\ufc22', '\ufc23', 
            '\ufc24', '\ufc25', '\ufc26', '\ufc27', '\ufc28', '\ufc29', '\ufc2a', '\ufc2b', 
            '\ufc2c', '\ufc2d', '\ufc2e', '\ufc2f', '\ufc30', '\ufc31', '\ufc32', '\ufc33', 
            '\ufc34', '\ufc35', '\ufc36', '\ufc37', '\ufc38', '\ufc39', '\ufc3a', '\ufc3b', 
            '\ufc3c', '\ufc3d', '\ufc3e', '\ufc3f', '\ufc40', '\ufc41', '\ufc42', '\ufc43', 
            '\ufc44', '\ufc45', '\ufc46', '\ufc47', '\ufc48', '\ufc49', '\ufc4a', '\ufc4b', 
            '\ufc4c', '\ufc4d', '\ufc4e', '\ufc4f', '\ufc50', '\ufc51', '\ufc52', '\ufc53', 
            '\ufc54', '\ufc55', '\ufc56', '\ufc57', '\ufc58', '\ufc59', '\ufc5a', '\ufc5b', 
            '\ufc5c', '\ufc5d', '\ufc5e', '\ufc5f', '\ufc60', '\ufc61', '\ufc62', '\ufc63', 
            '\ufc64', '\ufc65', '\ufc66', '\ufc67', '\ufc68', '\ufc69', '\ufc6a', '\ufc6b', 
            '\ufc6c', '\ufc6d', '\ufc6e', '\ufc6f', '\ufc70', '\ufc71', '\ufc72', '\ufc73', 
            '\ufc74', '\ufc75', '\ufc76', '\ufc77', '\ufc78', '\ufc79', '\ufc7a', '\ufc7b', 
            '\ufc7c', '\ufc7d', '\ufc7e', '\ufc7f', '\ufc80', '\ufc81', '\ufc82', '\ufc83', 
            '\ufc84', '\ufc85', '\ufc86', '\ufc87', '\ufc88', '\ufc89', '\ufc8a', '\ufc8b', 
            '\ufc8c', '\ufc8d', '\ufc8e', '\ufc8f', '\ufc90', '\ufc91', '\ufc92', '\ufc93', 
            '\ufc94', '\ufc95', '\ufc96', '\ufc97', '\ufc98', '\ufc99', '\ufc9a', '\ufc9b', 
            '\ufc9c', '\ufc9d', '\ufc9e', '\ufc9f', '\ufca0', '\ufca1', '\ufca2', '\ufca3', 
            '\ufca4', '\ufca5', '\ufca6', '\ufca7', '\ufca8', '\ufca9', '\ufcaa', '\ufcab', 
            '\ufcac', '\ufcad', '\ufcae', '\ufcaf', '\ufcb0', '\ufcb1', '\ufcb2', '\ufcb3', 
            '\ufcb4', '\ufcb5', '\ufcb6', '\ufcb7', '\ufcb8', '\ufcb9', '\ufcba', '\ufcbb', 
            '\ufcbc', '\ufcbd', '\ufcbe', '\ufcbf', '\ufcc0', '\ufcc1', '\ufcc2', '\ufcc3', 
            '\ufcc4', '\ufcc5', '\ufcc6', '\ufcc7', '\ufcc8', '\ufcc9', '\ufcca', '\ufccb', 
            '\ufccc', '\ufccd', '\ufcce', '\ufccf', '\ufcd0', '\ufcd1', '\ufcd2', '\ufcd3', 
            '\ufcd4', '\ufcd5', '\ufcd6', '\ufcd7', '\ufcd8', '\ufcd9', '\ufcda', '\ufcdb', 
            '\ufcdc', '\ufcdd', '\ufcde', '\ufcdf', '\ufce0', '\ufce1', '\ufce2', '\ufce3', 
            '\ufce4', '\ufce5', '\ufce6', '\ufce7', '\ufce8', '\ufce9', '\ufcea', '\ufceb', 
            '\ufcec', '\ufced', '\ufcee', '\ufcef', '\ufcf0', '\ufcf1', '\ufcf2', '\ufcf3', 
            '\ufcf4', '\ufcf5', '\ufcf6', '\ufcf7', '\ufcf8', '\ufcf9', '\ufcfa', '\ufcfb', 
            '\ufcfc', '\ufcfd', '\ufcfe', '\ufcff', '\ufd00', '\ufd01', '\ufd02', '\ufd03', 
            '\ufd04', '\ufd05', '\ufd06', '\ufd07', '\ufd08', '\ufd09', '\ufd0a', '\ufd0b', 
            '\ufd0c', '\ufd0d', '\ufd0e', '\ufd0f', '\ufd10', '\ufd11', '\ufd12', '\ufd13', 
            '\ufd14', '\ufd15', '\ufd16', '\ufd17', '\ufd18', '\ufd19', '\ufd1a', '\ufd1b', 
            '\ufd1c', '\ufd1d', '\ufd1e', '\ufd1f', '\ufd20', '\ufd21', '\ufd22', '\ufd23', 
            '\ufd24', '\ufd25', '\ufd26', '\ufd27', '\ufd28', '\ufd29', '\ufd2a', '\ufd2b', 
            '\ufd2c', '\ufd2d', '\ufd2e', '\ufd2f', '\ufd30', '\ufd31', '\ufd32', '\ufd33', 
            '\ufd34', '\ufd35', '\ufd36', '\ufd37', '\ufd38', '\ufd39', '\ufd3a', '\ufd3b', 
            '\ufd3c', '\ufd3d', '\ufd50', '\ufd51', '\ufd52', '\ufd53', '\ufd54', '\ufd55', 
            '\ufd56', '\ufd57', '\ufd58', '\ufd59', '\ufd5a', '\ufd5b', '\ufd5c', '\ufd5d', 
            '\ufd5e', '\ufd5f', '\ufd60', '\ufd61', '\ufd62', '\ufd63', '\ufd64', '\ufd65', 
            '\ufd66', '\ufd67', '\ufd68', '\ufd69', '\ufd6a', '\ufd6b', '\ufd6c', '\ufd6d', 
            '\ufd6e', '\ufd6f', '\ufd70', '\ufd71', '\ufd72', '\ufd73', '\ufd74', '\ufd75', 
            '\ufd76', '\ufd77', '\ufd78', '\ufd79', '\ufd7a', '\ufd7b', '\ufd7c', '\ufd7d', 
            '\ufd7e', '\ufd7f', '\ufd80', '\ufd81', '\ufd82', '\ufd83', '\ufd84', '\ufd85', 
            '\ufd86', '\ufd87', '\ufd88', '\ufd89', '\ufd8a', '\ufd8b', '\ufd8c', '\ufd8d', 
            '\ufd8e', '\ufd8f', '\ufd92', '\ufd93', '\ufd94', '\ufd95', '\ufd96', '\ufd97', 
            '\ufd98', '\ufd99', '\ufd9a', '\ufd9b', '\ufd9c', '\ufd9d', '\ufd9e', '\ufd9f', 
            '\ufda0', '\ufda1', '\ufda2', '\ufda3', '\ufda4', '\ufda5', '\ufda6', '\ufda7', 
            '\ufda8', '\ufda9', '\ufdaa', '\ufdab', '\ufdac', '\ufdad', '\ufdae', '\ufdaf', 
            '\ufdb0', '\ufdb1', '\ufdb2', '\ufdb3', '\ufdb4', '\ufdb5', '\ufdb6', '\ufdb7', 
            '\ufdb8', '\ufdb9', '\ufdba', '\ufdbb', '\ufdbc', '\ufdbd', '\ufdbe', '\ufdbf', 
            '\ufdc0', '\ufdc1', '\ufdc2', '\ufdc3', '\ufdc4', '\ufdc5', '\ufdc6', '\ufdc7', 
            '\ufdf0', '\ufdf1', '\ufdf2', '\ufdf3', '\ufdf4', '\ufdf5', '\ufdf6', '\ufdf7', 
            '\ufdf8', '\ufdf9', '\ufdfa', '\ufdfb', '\ufdfc', '\ufe10', '\ufe11', '\ufe12', 
            '\ufe13', '\ufe14', '\ufe15', '\ufe16', '\ufe17', '\ufe18', '\ufe19', '\ufe30', 
            '\ufe31', '\ufe32', '\ufe33', '\ufe34', '\ufe35', '\ufe36', '\ufe37', '\ufe38', 
            '\ufe39', '\ufe3a', '\ufe3b', '\ufe3c', '\ufe3d', '\ufe3e', '\ufe3f', '\ufe40', 
            '\ufe41', '\ufe42', '\ufe43', '\ufe44', '\ufe47', '\ufe48', '\ufe49', '\ufe4a', 
            '\ufe4b', '\ufe4c', '\ufe4d', '\ufe4e', '\ufe4f', '\ufe50', '\ufe51', '\ufe52', 
            '\ufe54', '\ufe55', '\ufe56', '\ufe57', '\ufe58', '\ufe59', '\ufe5a', '\ufe5b', 
            '\ufe5c', '\ufe5d', '\ufe5e', '\ufe5f', '\ufe60', '\ufe61', '\ufe62', '\ufe63', 
            '\ufe64', '\ufe65', '\ufe66', '\ufe68', '\ufe69', '\ufe6a', '\ufe6b', '\ufe70', 
            '\ufe71', '\ufe72', '\ufe74', '\ufe76', '\ufe77', '\ufe78', '\ufe79', '\ufe7a', 
            '\ufe7b', '\ufe7c', '\ufe7d', '\ufe7e', '\ufe7f', '\ufe80', '\ufe81', '\ufe82', 
            '\ufe83', '\ufe84', '\ufe85', '\ufe86', '\ufe87', '\ufe88', '\ufe89', '\ufe8a', 
            '\ufe8b', '\ufe8c', '\ufe8d', '\ufe8e', '\ufe8f', '\ufe90', '\ufe91', '\ufe92', 
            '\ufe93', '\ufe94', '\ufe95', '\ufe96', '\ufe97', '\ufe98', '\ufe99', '\ufe9a', 
            '\ufe9b', '\ufe9c', '\ufe9d', '\ufe9e', '\ufe9f', '\ufea0', '\ufea1', '\ufea2', 
            '\ufea3', '\ufea4', '\ufea5', '\ufea6', '\ufea7', '\ufea8', '\ufea9', '\ufeaa', 
            '\ufeab', '\ufeac', '\ufead', '\ufeae', '\ufeaf', '\ufeb0', '\ufeb1', '\ufeb2', 
            '\ufeb3', '\ufeb4', '\ufeb5', '\ufeb6', '\ufeb7', '\ufeb8', '\ufeb9', '\ufeba', 
            '\ufebb', '\ufebc', '\ufebd', '\ufebe', '\ufebf', '\ufec0', '\ufec1', '\ufec2', 
            '\ufec3', '\ufec4', '\ufec5', '\ufec6', '\ufec7', '\ufec8', '\ufec9', '\ufeca', 
            '\ufecb', '\ufecc', '\ufecd', '\ufece', '\ufecf', '\ufed0', '\ufed1', '\ufed2', 
            '\ufed3', '\ufed4', '\ufed5', '\ufed6', '\ufed7', '\ufed8', '\ufed9', '\ufeda', 
            '\ufedb', '\ufedc', '\ufedd', '\ufede', '\ufedf', '\ufee0', '\ufee1', '\ufee2', 
            '\ufee3', '\ufee4', '\ufee5', '\ufee6', '\ufee7', '\ufee8', '\ufee9', '\ufeea', 
            '\ufeeb', '\ufeec', '\ufeed', '\ufeee', '\ufeef', '\ufef0', '\ufef1', '\ufef2', 
            '\ufef3', '\ufef4', '\ufef5', '\ufef6', '\ufef7', '\ufef8', '\ufef9', '\ufefa', 
            '\ufefb', '\ufefc', '\uff01', '\uff02', '\uff03', '\uff04', '\uff05', '\uff06', 
            '\uff07', '\uff08', '\uff09', '\uff0a', '\uff0b', '\uff0c', '\uff0d', '\uff0e', 
            '\uff0f', '\uff10', '\uff11', '\uff12', '\uff13', '\uff14', '\uff15', '\uff16', 
            '\uff17', '\uff18', '\uff19', '\uff1a', '\uff1b', '\uff1c', '\uff1d', '\uff1e', 
            '\uff1f', '\uff20', '\uff21', '\uff22', '\uff23', '\uff24', '\uff25', '\uff26', 
            '\uff27', '\uff28', '\uff29', '\uff2a', '\uff2b', '\uff2c', '\uff2d', '\uff2e', 
            '\uff2f', '\uff30', '\uff31', '\uff32', '\uff33', '\uff34', '\uff35', '\uff36', 
            '\uff37', '\uff38', '\uff39', '\uff3a', '\uff3b', '\uff3c', '\uff3d', '\uff3e', 
            '\uff3f', '\uff40', '\uff41', '\uff42', '\uff43', '\uff44', '\uff45', '\uff46', 
            '\uff47', '\uff48', '\uff49', '\uff4a', '\uff4b', '\uff4c', '\uff4d', '\uff4e', 
            '\uff4f', '\uff50', '\uff51', '\uff52', '\uff53', '\uff54', '\uff55', '\uff56', 
            '\uff57', '\uff58', '\uff59', '\uff5a', '\uff5b', '\uff5c', '\uff5d', '\uff5e', 
            '\uff5f', '\uff60', '\uff61', '\uff62', '\uff63', '\uff64', '\uff65', '\uff66', 
            '\uff67', '\uff68', '\uff69', '\uff6a', '\uff6b', '\uff6c', '\uff6d', '\uff6e', 
            '\uff6f', '\uff70', '\uff71', '\uff72', '\uff73', '\uff74', '\uff75', '\uff76', 
            '\uff77', '\uff78', '\uff79', '\uff7a', '\uff7b', '\uff7c', '\uff7d', '\uff7e', 
            '\uff7f', '\uff80', '\uff81', '\uff82', '\uff83', '\uff84', '\uff85', '\uff86', 
            '\uff87', '\uff88', '\uff89', '\uff8a', '\uff8b', '\uff8c', '\uff8d', '\uff8e', 
            '\uff8f', '\uff90', '\uff91', '\uff92', '\uff93', '\uff94', '\uff95', '\uff96', 
            '\uff97', '\uff98', '\uff99', '\uff9a', '\uff9b', '\uff9c', '\uff9d', '\uff9e', 
            '\uff9f', '\uffa0', '\uffa1', '\uffa2', '\uffa3', '\uffa4', '\uffa5', '\uffa6', 
            '\uffa7', '\uffa8', '\uffa9', '\uffaa', '\uffab', '\uffac', '\uffad', '\uffae', 
            '\uffaf', '\uffb0', '\uffb1', '\uffb2', '\uffb3', '\uffb4', '\uffb5', '\uffb6', 
            '\uffb7', '\uffb8', '\uffb9', '\uffba', '\uffbb', '\uffbc', '\uffbd', '\uffbe', 
            '\uffc2', '\uffc3', '\uffc4', '\uffc5', '\uffc6', '\uffc7', '\uffca', '\uffcb', 
            '\uffcc', '\uffcd', '\uffce', '\uffcf', '\uffd2', '\uffd3', '\uffd4', '\uffd5', 
            '\uffd6', '\uffd7', '\uffda', '\uffdb', '\uffdc', '\uffe0', '\uffe1', '\uffe2', 
            '\uffe3', '\uffe4', '\uffe5', '\uffe6', '\uffe8', '\uffe9', '\uffea', '\uffeb', 
            '\uffec', '\uffed', '\uffee' 
    };
}
