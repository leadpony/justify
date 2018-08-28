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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Format attribute representing "ipv4" attribute.
 * 
 * @author leadpony
 */
public class Ipv4 implements StringFormatAttribute {
    
    private static final Pattern IPV4_PATTERN = 
            Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    @Override
    public String name() {
        return "ipv4";
    }

    @Override
    public boolean test(String value) {
        Matcher m = IPV4_PATTERN.matcher(value);
        if (!m.matches()) {
            return false;
        }
        for (int i = 1; i < 4; i++) {
            String decbyte = m.group(i);
            if (decbyte.length() >= 2 && decbyte.startsWith("0")) {
                return false;
            }
            if (Integer.parseInt(decbyte) > 255) {
                return false;
            }
        }
        return true;
    }
}
