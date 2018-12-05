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

package org.leadpony.justify.spi;

import javax.json.JsonValue;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Localizable;

/**
 * Service provider interface for providing a format attribute.
 * 
 * @author leadpony
 * 
 * @see <a href="https://json-schema.org/latest/json-schema-validation.html#rfc.section.7">
 *      JSON Schema Validation: A Vocabulary for Structural Validation of JSON, Section 7
 *      </a>
 */
public interface FormatAttribute {
    
    /**
     * Returns the name of this attribute.
     * @return the name of this attribute, never be {@code null}.
     */
    String name();
    
    /**
     * Returns the localized name.
     * @return the localized name of this attribute, never be {@code null}.
     */
    default Localizable localizedName() {
        return l->name();
    }
    
    /**
     * Returns the type of the value expected by this attribute.
     * @return the expected type of the value, never be {@code null}.
     */
    InstanceType valueType();
    
    /**
     * Checks if the value matches this format.
     * @param value the value to check, cannot be {@code null}.
     * @return {@code true} if the value matches this format, or {@code false}.
     * @throws NullPointerException if the specified {@code value} was {@code null}.
     */
    boolean test(JsonValue value);
}
