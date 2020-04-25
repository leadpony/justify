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
package org.leadpony.justify.tests.spi;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * Format attribute representing "palindrome".
 *
 * @author leadpony
 */
public class PalindromeFormatAttribute implements FormatAttribute {

    @Override
    public String name() {
        return "palindrome";
    }

    @Override
    public InstanceType valueType() {
        return InstanceType.STRING;
    }

    @Override
    public boolean test(JsonValue value) {
        String string = ((JsonString) value).getString();
        int i = 0;
        int j = string.length() - 1;
        while (i < j) {
            if (string.charAt(i++) != string.charAt(j--)) {
                return false;
            }
        }
        return true;
    }
}
