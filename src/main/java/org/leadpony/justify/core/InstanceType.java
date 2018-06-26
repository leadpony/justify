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

package org.leadpony.justify.core;

/**
 * Primitive types of JSON instances.
 * 
 * @author leadpony
 */
public enum InstanceType {
    /** JSON null. */
    NULL,
    /** JSON true of false. */
    BOOLEAN,
    /** JSON object. */
    OBJECT,
    /** JSON array. */
    ARRAY,
    /** JSON number with or without fractional part. */
    NUMBER,
    /** JSON object. */
    STRING,
    /** JSON number without fractional part. */
    INTEGER
    ;
    
    /**
     * Checks if this type is numeric or not.
     * 
     * @return {@code true} if this type is numeric, {@code false} otherwise.
     */
    public boolean isNumeric() {
        return this == NUMBER || this == INTEGER;
    }
    
    /**
     * Checks if this type can contain other types or not.
     * A container is either JSON array or JSON object.
     * 
     * @return {@code true} if this type is container, {@code false} otherwise.
     */
    public boolean isContainer() {
        return this == OBJECT || this == ARRAY;
    }
}
