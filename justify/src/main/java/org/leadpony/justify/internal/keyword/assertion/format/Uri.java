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
 * Format attribute representing "uri" attribute.
 *
 * @author leadpony
 *
 * @see <a href="https://tools.ietf.org/html/rfc3986">
 *      "Uniform Resource Identifier (URI): Generic Syntax", STD 66, RFC
 *      3986</a>
 */
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
class Uri extends AbstractFormatAttribute {

    private final boolean verbose;

    Uri() {
        this(false);
    }

    Uri(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String name() {
        return "uri";
    }

    @Override
    public Localizable localizedName() {
        return Message.FORMAT_URI;
    }

    @Override
    public boolean test(String value) {
        UriMatcher m = verbose ? new VerboseUriMatcher(value) : new UriMatcher(value);
        return m.matches();
    }
}
