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
 * Format attribute representing "dateTime" attribute.
 *
 * @author leadpony
 */
@Spec({ SpecVersion.DRAFT_04, SpecVersion.DRAFT_06, SpecVersion.DRAFT_07 })
class DateTime extends AbstractFormatAttribute {

    private final Date date = new Date();
    private final Time time = new Time();

    @Override
    public String name() {
        return "date-time";
    }

    @Override
    public Localizable localizedName() {
        return Message.FORMAT_DATE_TIME;
    }

    @Override
    public boolean test(String value) {
        String[] parts = value.split("T|t", 3);
        if (parts.length != 2) {
            return false;
        }
        return date.test(parts[0]) && time.test(parts[1]);
    }
}
