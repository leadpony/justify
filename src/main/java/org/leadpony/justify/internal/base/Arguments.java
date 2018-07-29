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

package org.leadpony.justify.internal.base;

import java.util.Collection;

/**
 * Provides parameter validation for methods.
 *
 * @author leadpony
 */
public final class Arguments {

    private Arguments() {
    }
    
    public static <T> T[] requireNonEmpty(T[] array, String name) {
        if (array.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        return array;
    }

    public static <T> Collection<T> requireNonEmpty(Collection<T> collection, String name) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        return collection;
    }

    public static int requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative.");
        }
        return value;
    }
}
