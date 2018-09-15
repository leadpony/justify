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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test cases for {@link Regex} class.
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
    
    private static final List<String> files = Arrays.asList(
            //"/org/ecma_international/test262/built_ins/regexp/regexp.json",
            //"/org/ecma_international/test262/built_ins/regexp/named-group.json",
            //"/org/ecma_international/test262/built_ins/regexp/property-escapes.json",
            "/org/ecma_international/test262/built_ins/regexp/property-escapes-generated.json"
            );
    
    public static Stream<RegexFixture> provideFixtures() {
        return files.stream().flatMap(RegexFixture::load);
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
     * https://github.com/tc39/test262/blob/master/test/built-ins/RegExp/S15.10.2_A1_T1.js
     * 
     * REX/Javascript 1.0 
     * Robert D. Cameron "REX: XML Shallow Parsing with Regular Expressions",
     * Technical Report TR 1998-17, School of Computing Science, Simon Fraser 
     * University, November, 1998.
     * Copyright (c) 1998, Robert D. Cameron. 
     * The following code may be freely used and distributed provided that
     * this copyright and citation notice remains intact and that modifications
     * or additions are clearly identified.
     */
    public static Stream<String> provideXmlRegex() {
        final String TextSE = "[^<]+";
        final String UntilHyphen = "[^-]*-";
        final String Until2Hyphens = UntilHyphen + "([^-]" + UntilHyphen + ")*-";
        final String CommentCE = Until2Hyphens + ">?";
        final String UntilRSBs = "[^\\]]*\\]([^\\]]+\\])*\\]+";
        final String CDATA_CE = UntilRSBs + "([^\\]>]" + UntilRSBs + ")*>";
        final String S = "[ \\n\\t\\r]+";
        final String NameStrt = "[A-Za-z_:]|[^\\x00-\\x7F]";
        final String NameChar = "[A-Za-z0-9_:.-]|[^\\x00-\\x7F]";
        final String Name = "(" + NameStrt + ")(" + NameChar + ")*";
        final String QuoteSE = "\"[^\"]" + "*" + "\"" + "|'[^']*'";
        final String DT_IdentSE = S + Name + "(" + S + "(" + Name + "|" + QuoteSE + "))*";
        final String MarkupDeclCE = "([^\\]\"'><]+|" + QuoteSE + ")*>";
        final String S1 = "[\\n\\r\\t ]";
        final String UntilQMs = "[^?]*\\?+";
        final String PI_Tail = "\\?>|" + S1 + UntilQMs + "([^>?]" + UntilQMs + ")*>";
        final String DT_ItemSE = "<(!(--" + Until2Hyphens + ">|[^-]" + MarkupDeclCE + ")|\\?" + Name + "(" + PI_Tail + "))|%" + Name + ";|" + S;
        final String DocTypeCE = DT_IdentSE + "(" + S + ")?(\\[(" + DT_ItemSE + ")*\\](" + S + ")?)?>?";
        final String DeclCE = "--(" + CommentCE + ")?|\\[CDATA\\[(" + CDATA_CE + ")?|DOCTYPE(" + DocTypeCE + ")?";
        final String PI_CE = Name + "(" + PI_Tail + ")?";
        final String EndTagCE = Name + "(" + S + ")?>?";
        final String AttValSE = "\"[^<\"]" + "*" + "\"" + "|'[^<']*'";
        final String ElemTagCE = Name + "(" + S + Name + "(" + S + ")?=(" + S + ")?(" + AttValSE + "))*(" + S + ")?/?>?";
        final String MarkupSPE = "<(!(" + DeclCE + ")?|\\?(" + PI_CE + ")?|/(" + EndTagCE + ")?|(" + ElemTagCE + ")?)";
        final String XML_SPE = TextSE + "|" + MarkupSPE;
        return Stream.of(
                TextSE,UntilHyphen,Until2Hyphens,CommentCE,UntilRSBs,CDATA_CE,S,NameStrt, NameChar, 
                Name, QuoteSE, DT_IdentSE, MarkupDeclCE, S1,UntilQMs, PI_Tail, DT_ItemSE, DocTypeCE, DeclCE, 
                PI_CE, EndTagCE, AttValSE, ElemTagCE, MarkupSPE, XML_SPE
                );        
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideXmlRegex")
    public void testXmlRegex(String value) {
        assertThat(sut.test(value)).isEqualTo(true);
    }
}
