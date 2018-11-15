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

package org.leadpony.justify.internal.keyword.assertion;

import java.math.BigDecimal;
import java.util.Set;

import javax.json.JsonValue;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * Provides various kinds of assertions.
 * 
 * @author leadpony
 */
public final class Assertions {
    
    /* Validation Keywords for Any Instance Type */
    
    public static Assertion type(Set<InstanceType> types) {
        if (types.size() == 1) {
            InstanceType first = types.iterator().next();
            return new Type.Single(first);
        } else {
            return new Type.Multiple(types);
        }
    }

    public static Assertion enum_(Set<JsonValue> expected) {
        return new Enum(expected);
    }
    
    public static Assertion const_(JsonValue expected) {
        return new Const(expected);
    }

    /* Validation Keywords for Numeric Instances (number and integer) */
    
    public static Assertion multipleOf(BigDecimal factor) {
        return new MultipleOf(factor);
    }

    public static Assertion maximum(BigDecimal limit) {
        return new Maximum(limit);
    }

    public static Assertion exclusiveMaximum(BigDecimal limit) {
        return new ExclusiveMaximum(limit);
    }
    
    public static Assertion minimum(BigDecimal limit) {
        return new Minimum(limit);
    }

    public static Assertion exclusiveMinimum(BigDecimal limit) {
        return new ExclusiveMinimum(limit);
    }
    
    /* Validation Keywords for Strings */
    
    public static Assertion maxLength(int limit) {
        return new MaxLength(limit);
    }

    public static Assertion minLength(int limit) {
        return new MinLength(limit);
    }
    
    public static Assertion pattern(java.util.regex.Pattern pattern) {
        return new Pattern(pattern);
    }

    /* Validation Keywords for Arrays */
    
    public static Assertion maxItems(int limit) {
        return new MaxItems(limit);
    }

    public static Assertion minItems(int limit) {
        return new MinItems(limit);
    }
    
    public static Assertion uniqueItems(boolean unique) {
        return new UniqueItems(unique);
    }
   
    public static Keyword maxContains(int limit) {
        return new MaxContains(limit);
    }

    public static Keyword minContains(int limit) {
        return new MinContains(limit);
    }
    
    /* Validation Keywords for Objects */

    public static Assertion maxProperties(int limit) {
        return new MaxProperties(limit);
    }

    public static Assertion minProperties(int limit) {
        return new MinProperties(limit);
    }

    public static Assertion required(Set<String> names) {
        return new Required(names);
    }

    private Assertions() {
    }
}
