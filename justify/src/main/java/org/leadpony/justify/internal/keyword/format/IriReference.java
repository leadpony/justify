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

import org.leadpony.justify.api.Localizable;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;

/**
 * Format attribute representing "iri-reference" attribute.
 *
 * @author leadpony
 *
 * @see <a href="https://tools.ietf.org/html/rfc3987">
 *      "Internationalized Resource Identifiers (IRIs)", RFC 3987</a>
 */
@Spec(SpecVersion.DRAFT_07)
class IriReference extends AbstractFormatAttribute {

    @Override
    public String name() {
        return "iri-reference";
    }

    @Override
    public Localizable localizedName() {
        return Message.FORMAT_IRI_REFERENCE;
    }

    @Override
    public boolean test(String value) {
        return new IriReferenceMatcher(value).matches();
    }
}
