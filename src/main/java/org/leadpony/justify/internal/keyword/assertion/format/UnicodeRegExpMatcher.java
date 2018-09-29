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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author leadpony
 */
public class UnicodeRegExpMatcher extends RegExpMatcher {
    
    private String lastPropertyName;
    private String lastPropertyValue;
 
    /**
     * Constructs this matcher.
     * 
     * @param input the input string.
     */
    UnicodeRegExpMatcher(CharSequence input) {
        super(input);
    }

    @Override
    protected boolean regExpUnicodeEscapeSequence() {
        if (!hasNext('u')) {
            return false;
        }
        int mark = pos();
        next();
        if (hasNext('{')) {
            next();
            if (codePoint() && hasNext('}')) {
                next();
                return withClassAtomOf(this.lastNumericValue);
            }
        } else if (hex4Digits()) {
            int high = this.lastNumericValue;
            if (Character.isHighSurrogate((char)high)) {
                mark = pos();
                if (isFollowedBy('\\', 'u')) {
                    if (hex4Digits()) {
                        int low = this.lastNumericValue;
                        int codePoint = Character.toCodePoint((char)high, (char)low);
                        return withClassAtomOf(codePoint);
                    }
                }
                backtrack(mark);
            }
            return withClassAtomOf(high);
        }
        return backtrack(mark);
    }

    @Override
    protected boolean identityEscape() {
        if (!hasNext()) {
            return false;
        }
        int c = peek();
        if (syntaxCharacter()) {
            return withClassAtomOf(c);
        } else if (c == '/') {
            next();
            return withClassAtomOf(c);
        }
        return false;
    }
    
    @Override
    protected boolean testCharacterClassEscape() {
        if (super.testCharacterClassEscape()) {
            return true;
        }
        if (!hasNext()) {
            return false;
        }
        int c = peek();
        if (c == 'p' || c == 'P') {
            final int mark = pos();
            next();
            if (hasNext('{')) {
                next();
                if (unicodePropertyValueExpression() && hasNext('}')) {
                    next();
                    // This may throw early error.
                    return checkProperty(lastPropertyName, lastPropertyValue);
                }
            }
            backtrack(mark);
        }
        return false;
    }
    
    private boolean unicodePropertyValueExpression() {
        final int nameStart = pos();
        if (unicodePropertyName()) {
            String name = extract(nameStart);
            if (hasNext('=')) {
                next();
                final int valueStart = pos();
                if (unicodePropertyValue()) {
                    String value = extract(valueStart);
                    this.lastPropertyName = name;
                    this.lastPropertyValue = value;
                    return true;
                }
            }
            backtrack(nameStart);
        }
        return loneUnicodePropertyNameOrValue();
    }
    
    private boolean unicodePropertyName() {
        return unicodePropertyNameCharacters();
    }
    
    private boolean unicodePropertyNameCharacters() {
        if (unicodePropertyNameCharacter()) {
            while (unicodePropertyNameCharacter()) {
            }
            return true;
        } else {
            return false;
        }
    }
    
    private boolean unicodePropertyValue() {
        return unicodePropertyValueCharacters();
    }
    
    private boolean loneUnicodePropertyNameOrValue() {
        final int nameStart = pos();
        if (unicodePropertyValueCharacters()) {
            this.lastPropertyName = extract(nameStart);
            this.lastPropertyValue = null;
            return true;
        }
        return false;
    }
    
    private boolean unicodePropertyValueCharacters() {
        if (unicodePropertyValueCharacter()) {
            while (unicodePropertyValueCharacter()) {
            }
            return true;
        } else {
            return false;
        }
    }
    
    private boolean unicodePropertyValueCharacter() {
        if (unicodePropertyNameCharacter()) {
            return true;
        } else if (hasNext() && Characters.isAsciiDigit(peek())) {
            next();
            return true;
        }
        return false;
    }
    
    private boolean unicodePropertyNameCharacter() {
        if (hasNext()) {
            int c = peek();
            if (isControlLetter(c) || c == '_') {
                next();
                return true;
            }
        }
        return false;
    }
    
    private boolean codePoint() {
        if (!hasNext()) {
            return false;
        }
        final int mark = pos();
        int value = hexDigitToValue(next());
        while (hasNext()) {
            if (Characters.isAsciiHexDigit(peek())) {
                value = value * 16 + hexDigitToValue(next());
            } else {
                this.lastNumericValue = value;
                return true;
            }
        }
        return backtrack(mark);
    }
    
    private boolean isFollowedBy(int... chars) {
        final int mark = pos();
        for (int c : chars) {
            if (hasNext(c)) {
                next();
            } else {
                return backtrack(mark);
            }
        }
        return true;
    }
    
    private static boolean checkProperty(String name, String value) {
        if (value != null) {
            return checkPropertyNameAndValue(name, value);
        } else {
            return checkLoneProperty(name);
        }
    }
    
    private static boolean checkPropertyNameAndValue(String name, String value) {
        if (nonBinaryPpropertySet.containsKey(name)) {
            Set<String> values = nonBinaryPpropertySet.get(name);
            if (values.contains(value)) {
                return true;
            }
        }
        return earlyError();
    }
    
    private static boolean checkLoneProperty(String property) {
        if (binaryPropertySet.contains(property) ||
            generalCategoryValueSet.contains(property)) {
            return true;
        }
        return earlyError();
    }

    private static final String[] propertyValuesForGeneralCategory = {
        "Cased_Letter",
        "LC",
        "Close_Punctuation",
        "Pe",
        "Connector_Punctuation",
        "Pc",
        "Control",
        "Cc",
        "cntrl",
        "Currency_Symbol",
        "Sc",
        "Dash_Punctuation",
        "Pd",
        "Decimal_Number",
        "Nd",
        "digit",
        "Enclosing_Mark",
        "Me",
        "Final_Punctuation",
        "Pf",
        "Format",
        "Cf",
        "Initial_Punctuation",
        "Pi",
        "Letter",
        "L",
        "Letter_Number",
        "Nl",
        "Line_Separator",
        "Zl",
        "Lowercase_Letter",
        "Ll",
        "Mark",
        "M",
        "Combining_Mark",
        "Math_Symbol",
        "Sm",
        "Modifier_Letter",
        "Lm",
        "Modifier_Symbol",
        "Sk",
        "Nonspacing_Mark",
        "Mn",
        "Number",
        "N",
        "Open_Punctuation",
        "Ps",
        "Other",
        "C",
        "Other_Letter",
        "Lo",
        "Other_Number",
        "No",
        "Other_Punctuation",
        "Po",
        "Other_Symbol",
        "So",
        "Paragraph_Separator",
        "Zp",
        "Private_Use",
        "Co",
        "Punctuation",
        "P",
        "punct",
        "Separator",
        "Z",
        "Space_Separator",
        "Zs",
        "Spacing_Mark",
        "Mc",
        "Surrogate",
        "Cs",
        "Symbol",
        "S",
        "Titlecase_Letter",
        "Lt",
        "Unassigned",
        "Cn",
        "Uppercase_Letter",
        "Lu",
    };
    
    private static final String[] propertyValuesForScript = {
            "Adlam",
            "Adlm",
            "Ahom",
            "Anatolian_Hieroglyphs",
            "Hluw",
            "Arabic",
            "Arab",
            "Armenian",
            "Armn",
            "Avestan",
            "Avst",
            "Balinese",
            "Bali",
            "Bamum",
            "Bamu",
            "Bassa_Vah",
            "Bass",
            "Batak",
            "Batk",
            "Bengali",
            "Beng",
            "Bhaiksuki",
            "Bhks",
            "Bopomofo",
            "Bopo",
            "Brahmi",
            "Brah",
            "Braille",
            "Brai",
            "Buginese",
            "Bugi",
            "Buhid",
            "Buhd",
            "Canadian_Aboriginal",
            "Cans",
            "Carian",
            "Cari",
            "Caucasian_Albanian",
            "Aghb",
            "Chakma",
            "Cakm",
            "Cham",
            "Cherokee",
            "Cher",
            "Common",
            "Zyyy",
            "Coptic",
            "Copt",
            "Qaac",
            "Cuneiform",
            "Xsux",
            "Cypriot",
            "Cprt",
            "Cyrillic",
            "Cyrl",
            "Deseret",
            "Dsrt",
            "Devanagari",
            "Deva",
            "Duployan",
            "Dupl",
            "Egyptian_Hieroglyphs",
            "Egyp",
            "Elbasan",
            "Elba",
            "Ethiopic",
            "Ethi",
            "Georgian",
            "Geor",
            "Glagolitic",
            "Glag",
            "Gothic",
            "Goth",
            "Grantha",
            "Gran",
            "Greek",
            "Grek",
            "Gujarati",
            "Gujr",
            "Gurmukhi",
            "Guru",
            "Han",
            "Hani",
            "Hangul",
            "Hang",
            "Hanunoo",
            "Hano",
            "Hatran",
            "Hatr",
            "Hebrew",
            "Hebr",
            "Hiragana",
            "Hira",
            "Imperial_Aramaic",
            "Armi",
            "Inherited",
            "Zinh",
            "Qaai",
            "Inscriptional_Pahlavi",
            "Phli",
            "Inscriptional_Parthian",
            "Prti",
            "Javanese",
            "Java",
            "Kaithi",
            "Kthi",
            "Kannada",
            "Knda",
            "Katakana",
            "Kana",
            "Kayah_Li",
            "Kali",
            "Kharoshthi",
            "Khar",
            "Khmer",
            "Khmr",
            "Khojki",
            "Khoj",
            "Khudawadi",
            "Sind",
            "Lao",
            "Laoo",
            "Latin",
            "Latn",
            "Lepcha",
            "Lepc",
            "Limbu",
            "Limb",
            "Linear_A",
            "Lina",
            "Linear_B",
            "Linb",
            "Lisu",
            "Lycian",
            "Lyci",
            "Lydian",
            "Lydi",
            "Mahajani",
            "Mahj",
            "Malayalam",
            "Mlym",
            "Mandaic",
            "Mand",
            "Manichaean",
            "Mani",
            "Marchen",
            "Marc",
            "Masaram_Gondi",
            "Gonm",
            "Meetei_Mayek",
            "Mtei",
            "Mende_Kikakui",
            "Mend",
            "Meroitic_Cursive",
            "Merc",
            "Meroitic_Hieroglyphs",
            "Mero",
            "Miao",
            "Plrd",
            "Modi",
            "Mongolian",
            "Mong",
            "Mro",
            "Mroo",
            "Multani",
            "Mult",
            "Myanmar",
            "Mymr",
            "Nabataean",
            "Nbat",
            "New_Tai_Lue",
            "Talu",
            "Newa",
            "Nko",
            "Nkoo",
            "Nushu",
            "Nshu",
            "Ogham",
            "Ogam",
            "Ol_Chiki",
            "Olck",
            "Old_Hungarian",
            "Hung",
            "Old_Italic",
            "Ital",
            "Old_North_Arabian",
            "Narb",
            "Old_Permic",
            "Perm",
            "Old_Persian",
            "Xpeo",
            "Old_South_Arabian",
            "Sarb",
            "Old_Turkic",
            "Orkh",
            "Oriya",
            "Orya",
            "Osage",
            "Osge",
            "Osmanya",
            "Osma",
            "Pahawh_Hmong",
            "Hmng",
            "Palmyrene",
            "Palm",
            "Pau_Cin_Hau",
            "Pauc",
            "Phags_Pa",
            "Phag",
            "Phoenician",
            "Phnx",
            "Psalter_Pahlavi",
            "Phlp",
            "Rejang",
            "Rjng",
            "Runic",
            "Runr",
            "Samaritan",
            "Samr",
            "Saurashtra",
            "Saur",
            "Sharada",
            "Shrd",
            "Shavian",
            "Shaw",
            "Siddham",
            "Sidd",
            "SignWriting",
            "Sgnw",
            "Sinhala",
            "Sinh",
            "Sora_Sompeng",
            "Sora",
            "Soyombo",
            "Soyo",
            "Sundanese",
            "Sund",
            "Syloti_Nagri",
            "Sylo",
            "Syriac",
            "Syrc",
            "Tagalog",
            "Tglg",
            "Tagbanwa",
            "Tagb",
            "Tai_Le",
            "Tale",
            "Tai_Tham",
            "Lana",
            "Tai_Viet",
            "Tavt",
            "Takri",
            "Takr",
            "Tamil",
            "Taml",
            "Tangut",
            "Tang",
            "Telugu",
            "Telu",
            "Thaana",
            "Thaa",
            "Thai",
            "Tibetan",
            "Tibt",
            "Tifinagh",
            "Tfng",
            "Tirhuta",
            "Tirh",
            "Ugaritic",
            "Ugar",
            "Vai",
            "Vaii",
            "Warang_Citi",
            "Wara",
            "Yi",
            "Yiii",
            "Zanabazar_Square",
            "Zanb",
    };
    
    @SuppressWarnings("serial")
    private static final Map<String, Set<String>> nonBinaryPpropertySet 
        = new HashMap<String, Set<String>>() {{
            
        Set<String> categoryValues = new HashSet<>();
        for (String value : propertyValuesForGeneralCategory) {
            categoryValues.add(value);
        }

        Set<String> scriptValues = new HashSet<>();
        for (String value : propertyValuesForScript) {
            scriptValues.add(value);
        }

        put("General_Category", categoryValues);
        put("gc", categoryValues);
        put("Script", scriptValues);
        put("sc", scriptValues);
        put("Script_Extensions", scriptValues);
        put("scx", scriptValues);
    }};
    
    private static final String[] binaryProperties = {
            "ASCII",
            "ASCII_Hex_Digit",
            "AHex",
            "Alphabetic",
            "Alpha",
            "Any",
            "Assigned",
            "Bidi_Control",
            "Bidi_C",
            "Bidi_Mirrored",
            "Bidi_M",
            "Case_Ignorable",
            "CI",
            "Cased",
            "Changes_When_Casefolded",
            "CWCF",
            "Changes_When_Casemapped",
            "CWCM",
            "Changes_When_Lowercased",
            "CWL",
            "Changes_When_NFKC_Casefolded",
            "CWKCF",
            "Changes_When_Titlecased",
            "CWT",
            "Changes_When_Uppercased",
            "CWU",
            "Dash",
            "Default_Ignorable_Code_Point",
            "DI",
            "Deprecated",
            "Dep",
            "Diacritic",
            "Dia",
            "Emoji",
            "Emoji_Component",
            "Emoji_Modifier",
            "Emoji_Modifier_Base",
            "Emoji_Presentation",
            "Extender",
            "Ext",
            "Grapheme_Base",
            "Gr_Base",
            "Grapheme_Extend",
            "Gr_Ext",
            "Hex_Digit",
            "Hex",
            "IDS_Binary_Operator",
            "IDSB",
            "IDS_Trinary_Operator",
            "IDST",
            "ID_Continue",
            "IDC",
            "ID_Start",
            "IDS",
            "Ideographic",
            "Ideo",
            "Join_Control",
            "Join_C",
            "Logical_Order_Exception",
            "LOE",
            "Lowercase",
            "Lower",
            "Math",
            "Noncharacter_Code_Point",
            "NChar",
            "Pattern_Syntax",
            "Pat_Syn",
            "Pattern_White_Space",
            "Pat_WS",
            "Quotation_Mark",
            "QMark",
            "Radical",
            "Regional_Indicator",
            "RI",
            "Sentence_Terminal",
            "STerm",
            "Soft_Dotted",
            "SD",
            "Terminal_Punctuation",
            "Term",
            "Unified_Ideograph",
            "UIdeo",
            "Uppercase",
            "Upper",
            "Variation_Selector",
            "VS",
            "White_Space",
            "space",
            "XID_Continue",
            "XIDC",
            "XID_Start",
            "XIDS",
    };
    
    @SuppressWarnings("serial")
    private static final Set<String> binaryPropertySet = new HashSet<String>() {{
        for (String property : binaryProperties) {
            add(property);
        }
    }};
    
    @SuppressWarnings("serial")
    private static final Set<String> generalCategoryValueSet = new HashSet<String>() {{
        for (String value : propertyValuesForGeneralCategory) {
            add(value);
        }
    }};
    
}
