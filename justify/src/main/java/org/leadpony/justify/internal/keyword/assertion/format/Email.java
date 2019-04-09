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
 * Format attribute representing "email" attribute. As defined by RFC 5322,
 * section 3.4.1.
 *
 * @author leadpony
 *
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322, section
 *      3.4.1</a>
 */
@Spec({ SpecVersion.DRAFT_06, SpecVersion.DRAFT_07 })
class Email extends AbstractFormatAttribute {

    @Override
    public String name() {
        return "email";
    }

    @Override
    public Localizable localizedName() {
        return Message.FORMAT_EMAIL;
    }

    @Override
    public boolean test(String value) {
        return new EmailMatcher(value).matches();
    }
}
