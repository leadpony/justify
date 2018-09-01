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

/**
 * Format attribute representing "ipv6" attribute.
 * 
 * @author leadpony
 * 
 * @see <a href="https://tools.ietf.org/html/rfc4291">RFC 4291</a>
 */
class Ipv6 implements StringFormatAttribute {
    
    private static final int MAX_GROUPS = 8;
    private static final int MAX_GROUP_DIGITS = 4;
    
    private static final Ipv4 ipv4 = new Ipv4();
    
    @Override
    public String name() {
        return "ipv6";
    }

    @Override
    public boolean test(String value) {
        String[] groups = value.split(":", MAX_GROUPS + 1);
        final int length = groups.length;
        if (length < 3 || length > MAX_GROUPS) {
            return false;
        }

        if (groups[0].isEmpty()) {
            if (!groups[1].isEmpty()) {
                return false;
            }
        } else if (!isHexadecimalGroup(groups[0])) {
            return false;
        }
        
        int i = 1;
        boolean compressed = false;
        while (i < length - 1) {
            String group = groups[i++];
            if (group.isEmpty()) {
                if (compressed) {
                    return false;
                }
                compressed = true;
            } else if (!isHexadecimalGroup(group)) {
                return false;
            }
        }

        String lastGroup = groups[i];
        if (lastGroup.isEmpty()) {
            if (!groups[i - 1].isEmpty()) {
                return false;
            }
        } else {
            if (!isHexadecimalGroup(lastGroup)) {
                return ipv4.test(lastGroup);
            }
        }
        
        return true;
    }
    
    private static boolean isHexadecimalGroup(String group) {
        final int length = group.length();
        if (length == 0 || length > MAX_GROUP_DIGITS) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!isHexadecimalDigit(group.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexadecimalDigit(char c) {
        return (c >= '0' && c <= '9') || 
               (c >= 'A' && c <= 'F') || 
               (c >= 'a' && c <= 'f');
    }
}
