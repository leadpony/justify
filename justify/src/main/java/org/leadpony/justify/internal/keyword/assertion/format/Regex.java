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

import org.leadpony.justify.api.Localizable;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;

/**
 * A format attribute representing "regex" attribute.
 *
 * <p>
 * Note that the specification Draft-04 and Draft-06 actually does not support
 * this format attribute, but the official test suite includes tests for this
 * attribute as optional tests.
 * </p>
 *
 * @author leadpony
 * @see <a href=
 *      "http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-262.pdf">
 *      ECMA 262 specification</a>
 */
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
class Regex extends AbstractFormatAttribute {

    @Override
    public String name() {
        return "regex";
    }

    @Override
    public Localizable localizedName() {
        return Message.FORMAT_REGEX;
    }

    @Override
    public boolean test(String value) {
        return testWithoutUnicodeFlag(value);
    }

    public boolean test(String value, String flags) {
        if (flags.indexOf('u') >= 0) {
            return testWithUnicodeFlag(value);
        } else {
            return testWithoutUnicodeFlag(value);
        }
    }

    private boolean testWithUnicodeFlag(String value) {
        return new UnicodeRegExpMatcher(value).matches();
    }

    private boolean testWithoutUnicodeFlag(String value) {
        return new NonUnicodeRegExpMatcher(value).matches();
    }
}
