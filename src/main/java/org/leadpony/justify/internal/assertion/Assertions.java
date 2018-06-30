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

package org.leadpony.justify.internal.assertion;

import java.math.BigDecimal;
import java.util.Set;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import org.leadpony.justify.core.InstanceType;

/**
 * @author leadpony
 */
public final class Assertions {
    
    /* Validation Keywords for Any Instance Type */
    
    public static Assertion type(Set<InstanceType> types) {
        return new Type(types);
    }

    public static Assertion enum_(Set<JsonValue> expected, JsonProvider jsonProvider) {
        return new Enum(expected, jsonProvider);
    }
    
    public static Assertion const_(JsonValue expected, JsonProvider jsonProvider) {
        return new Const(expected, jsonProvider);
    }

    /* Validation Keywords for Numeric Instances (number and integer) */
    
    public static Assertion multipleOf(BigDecimal divisor) {
        return new MultipleOf(divisor);
    }

    public static Assertion maximum(BigDecimal bound) {
        return new Maximum(bound);
    }

    public static Assertion exclusiveMaximum(BigDecimal bound) {
        return new ExclusiveMaximum(bound);
    }
    
    public static Assertion minimum(BigDecimal bound) {
        return new Minimum(bound);
    }

    public static Assertion exclusiveMinimum(BigDecimal bound) {
        return new ExclusiveMinimum(bound);
    }
    
    /* Validation Keywords for Strings */
    
    public static Assertion maxLength(int bound) {
        return new MaxLength(bound);
    }

    public static Assertion minLength(int bound) {
        return new MinLength(bound);
    }
    
    public static Assertion pattern(java.util.regex.Pattern pattern) {
        return new Pattern(pattern);
    }

    /* Validation Keywords for Arrays */
    
    public static Assertion maxItems(int bound) {
        return new MaxItems(bound);
    }

    public static Assertion minItems(int bound) {
        return new MinItems(bound);
    }
    
    public static Assertion uniqueItems(boolean unique, JsonProvider jsonProvider) {
        return new UniqueItems(unique, jsonProvider);
    }
    
    /* Validation Keywords for Objects */

    public static Assertion maxProperties(int bound) {
        return new MaxProperties(bound);
    }

    public static Assertion minProperties(int bound) {
        return new MinProperties(bound);
    }

    public static Assertion required(Set<String> names) {
        return new Required(names);
    }

    private Assertions() {
    }
}
