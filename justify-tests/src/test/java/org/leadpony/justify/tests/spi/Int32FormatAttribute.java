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

import javax.json.JsonNumber;
import javax.json.JsonValue;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * Format attribute representing "int32".
 *
 * @author leadpony
 */
public class Int32FormatAttribute implements FormatAttribute {

    @Override
    public String name() {
        return "int32";
    }

    @Override
    public InstanceType valueType() {
        return InstanceType.NUMBER;
    }

    @Override
    public boolean test(JsonValue value) {
        JsonNumber number = (JsonNumber) value;
        if (!number.isIntegral()) {
            return false;
        }
        long longValue = number.longValue();
        return Integer.MIN_VALUE <= longValue && longValue <= Integer.MAX_VALUE;
    }
}
