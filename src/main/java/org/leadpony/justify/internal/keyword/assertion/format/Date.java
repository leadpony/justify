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

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Format attribute representing "date" attribute.
 *
 * @author leadpony
 */
class Date extends AbstractFormatAttribute {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Override
    public String name() {
        return "date";
    }

    @Override
    public boolean test(String value) {
        try {
            formatter.parse(value);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
