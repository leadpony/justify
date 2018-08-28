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

import javax.json.JsonString;
import javax.json.JsonValue;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.spi.FormatAttribute;

/**
 * Format attribute for string type.
 * 
 * @author leadpony
 */
public interface StringFormatAttribute extends FormatAttribute {

    /**
     * {@inheritDoc}
     */
    @Override
    default InstanceType valueType() {
        return InstanceType.STRING;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    default boolean test(JsonValue value) {
        String string = ((JsonString)value).getString(); 
        return test(string);
    }
    
    boolean test(String value);
}
