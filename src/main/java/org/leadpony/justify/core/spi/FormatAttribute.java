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

package org.leadpony.justify.core.spi;

import java.util.Locale;

import javax.json.JsonValue;

import org.leadpony.justify.core.InstanceType;

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
     * 
     * @return the name of this attribute, cannot be {@code null}.
     */
    String name();
    
    /**
     * Returns the name of this attribute for presentation purposes.
     * 
     * @param locale the language in which the name will be rendered.
     * @return the name of this attribute, cannot be {@code null}.
     * @throws NullPointerException if the specified {@code locale} was {@code null}.
     */
    default String displayName(Locale locale) {
        return name();
    }
    
    /**
     * Returns the expected type of the value.
     * 
     * @return the expected type of the value, cannot be {@code null}.
     */
    InstanceType valueType();
    
    /**
     * Checks if the value matches the expected format.
     * 
     * @param value the value to test, never be {@code null}.
     * @return {@code true} if the value matches, or {@code false}.
     * @throws NullPointerException if the specified {@code value} was {@code null}.
     */
    boolean test(JsonValue value);
}
