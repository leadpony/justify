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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.leadpony.justify.api.Localizable;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;

/**
 * Format attribute representing "time" attribute.
 *
 * @author leadpony
 */
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
class Time extends AbstractFormatAttribute {

    private static final Pattern LOCAL_TIME_PATTERN = Pattern.compile("(\\d{2})\\:(\\d{2})\\:(\\d{2})(\\.\\d+)?");
    private static final Pattern TIME_OFFSET_PATTERN = Pattern.compile("(\\d{2})\\:(\\d{2})");

    private static final int MAX_HOURS = 23;
    private static final int MAX_MINUTES = 59;
    // Including leadp second.
    private static final int MAX_SECONDS = 60;

    @Override
    public String name() {
        return "time";
    }

    @Override
    public Localizable localizedName() {
        return Message.FORMAT_TIME;
    }

    @Override
    public boolean test(String value) {
        if (value.endsWith("Z") || value.endsWith("z")) {
            String part = value.substring(0, value.length() - 1);
            return testLocalTimePart(part);
        } else {
            String[] parts = value.split("\\+|\\-");
            if (parts.length != 2) {
                return false;
            }
            return testLocalTimePart(parts[0]) && testTimeOffsetPart(parts[1]);
        }
    }

    private static boolean testLocalTimePart(String value) {
        Matcher m = LOCAL_TIME_PATTERN.matcher(value);
        if (!m.matches()) {
            return false;
        }
        int hours = Integer.parseInt(m.group(1));
        if (hours > MAX_HOURS) {
            return false;
        }
        int minutes = Integer.parseInt(m.group(2));
        if (minutes > MAX_MINUTES) {
            return false;
        }
        int seconds = Integer.parseInt(m.group(3));
        if (seconds > MAX_SECONDS) {
            return false;
        }
        return true;
    }

    private static boolean testTimeOffsetPart(String value) {
        Matcher m = TIME_OFFSET_PATTERN.matcher(value);
        if (!m.matches()) {
            return false;
        }
        int hours = Integer.parseInt(m.group(1));
        if (hours > MAX_HOURS) {
            return false;
        }
        int minutes = Integer.parseInt(m.group(2));
        if (minutes > MAX_MINUTES) {
            return false;
        }
        return true;
    }
}
