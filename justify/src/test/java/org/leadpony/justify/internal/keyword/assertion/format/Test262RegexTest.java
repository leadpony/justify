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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A test class for {@link Regex}.
 *
 * @author leadpony
 */
public class Test262RegexTest {

    // System under test
    private static Regex sut;

    private static int index;

    @BeforeAll
    public static void setUpOnce() {
        sut = new Regex();
    }

    private static final List<String> FILES = Arrays.asList(
            "/org/ecma_international/test262/built_ins/regexp/regexp.json",
            "/org/ecma_international/test262/built_ins/regexp/named-group.json",
            "/org/ecma_international/test262/built_ins/regexp/property-escapes.json",
            "/org/ecma_international/test262/built_ins/regexp/property-escapes-generated.json");

    public static Stream<RegexFixture> provideFixtures() {
        return FILES.stream().flatMap(RegexFixture::load);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideFixtures")
    public void test(RegexFixture fixture) {
        Assumptions.assumeTrue(++index >= 0);
        assertThat(sut.test(fixture.pattern(), fixture.flags()))
                .isEqualTo(fixture.result());
    }

    /*
     * The following regular expressions are copied from
     * https://github.com/tc39/test262/blob/master/test/built-ins/RegExp/S15.10.
     * 2_A1_T1.js
     *
     * REX/Javascript 1.0 Robert D. Cameron
     * "REX: XML Shallow Parsing with Regular Expressions", Technical Report TR
     * 1998-17, School of Computing Science, Simon Fraser University, November,
     * 1998. Copyright (c) 1998, Robert D. Cameron. The following code may be freely
     * used and distributed provided that this copyright and citation notice remains
     * intact and that modifications or additions are clearly identified.
     */

    static final String TEXT_SE = "[^<]+";
    static final String UNTIL_HYPHEN = "[^-]*-";
    static final String UNTIL_2_HYPHENS = UNTIL_HYPHEN + "([^-]" + UNTIL_HYPHEN + ")*-";
    static final String COMMENT_CE = UNTIL_2_HYPHENS + ">?";
    static final String UNTIL_RSBS = "[^\\]]*\\]([^\\]]+\\])*\\]+";
    static final String CDATA_CE = UNTIL_RSBS + "([^\\]>]" + UNTIL_RSBS + ")*>";
    static final String S = "[ \\n\\t\\r]+";
    static final String NAME_START = "[A-Za-z_:]|[^\\x00-\\x7F]";
    static final String NAME_CHAR = "[A-Za-z0-9_:.-]|[^\\x00-\\x7F]";
    static final String NAME = "(" + NAME_START + ")(" + NAME_CHAR + ")*";
    static final String QUOTE_SE = "\"[^\"]" + "*" + "\"" + "|'[^']*'";
    static final String DT_IDENT_SE = S + NAME + "(" + S + "(" + NAME + "|" + QUOTE_SE + "))*";
    static final String MARKUP_DECL_CE = "([^\\]\"'><]+|" + QUOTE_SE + ")*>";
    static final String S1 = "[\\n\\r\\t ]";
    static final String UNTIL_QMS = "[^?]*\\?+";
    static final String PI_TAIL = "\\?>|" + S1 + UNTIL_QMS + "([^>?]" + UNTIL_QMS + ")*>";
    static final String DT_ITEM_SE = "<(!(--" + UNTIL_2_HYPHENS + ">|[^-]" + MARKUP_DECL_CE + ")|\\?" + NAME + "("
            + PI_TAIL
            + "))|%" + NAME + ";|" + S;
    static final String DOC_TYPE_CE = DT_IDENT_SE + "(" + S + ")?(\\[(" + DT_ITEM_SE + ")*\\](" + S + ")?)?>?";
    static final String DECL_CE = "--(" + COMMENT_CE + ")?|\\[CDATA\\[(" + CDATA_CE + ")?|DOCTYPE(" + DOC_TYPE_CE
            + ")?";
    static final String PI_CE = NAME + "(" + PI_TAIL + ")?";
    static final String END_TAG_CE = NAME + "(" + S + ")?>?";
    static final String ATT_VAL_SE = "\"[^<\"]" + "*" + "\"" + "|'[^<']*'";
    static final String ELEM_TAG_CE = NAME + "(" + S + NAME + "(" + S + ")?=(" + S + ")?(" + ATT_VAL_SE + "))*(" + S
            + ")?/?>?";
    static final String MARKUP_SPE = "<(!(" + DECL_CE + ")?|\\?(" + PI_CE + ")?|/(" + END_TAG_CE + ")?|(" + ELEM_TAG_CE
            + ")?)";
    static final String XML_SPE = TEXT_SE + "|" + MARKUP_SPE;

    public static Stream<String> provideXmlRegex() {
        return Stream.of(
                TEXT_SE,
                UNTIL_HYPHEN,
                UNTIL_2_HYPHENS,
                COMMENT_CE,
                UNTIL_RSBS,
                CDATA_CE,
                S,
                NAME_START,
                NAME_CHAR,
                NAME,
                QUOTE_SE,
                DT_IDENT_SE,
                MARKUP_DECL_CE,
                S1,
                UNTIL_QMS,
                PI_TAIL,
                DT_ITEM_SE,
                DOC_TYPE_CE,
                DECL_CE,
                PI_CE,
                END_TAG_CE,
                ATT_VAL_SE,
                ELEM_TAG_CE,
                MARKUP_SPE,
                XML_SPE);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideXmlRegex")
    public void testXmlRegex(String value) {
        assertThat(sut.test(value)).isEqualTo(true);
    }
}
