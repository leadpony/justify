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

import javax.json.JsonString;
import javax.json.JsonValue;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * Skeletal implementation for {@link FormatAttribute}.
 *
 * @author leadpony
 */
public abstract class AbstractFormatAttribute implements FormatAttribute {

    /**
     * {@inheritDoc}
     */
    @Override
    public InstanceType valueType() {
        return InstanceType.STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(JsonValue value) {
        String string = ((JsonString)value).getString();
        return test(string);
    }

    /**
     * Checks if the string value conforms to this format.
     *
     * @param value the string value to check, which cannot be {@code null}.
     * @return {@code true} if the value conforms to the format, or {@code false}.
     */
    abstract boolean test(String value);
}
