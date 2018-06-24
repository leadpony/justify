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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility methods operating on {@link Set}.
 * 
 * @author leadpony
 */
public final class Sets {
    
    @SafeVarargs
    public static <T> Set<T> asSet(T... a) {
        Set<T> set = new LinkedHashSet<>();
        for (T e : a) {
            set.add(e);
        }
        return set;
    }
    
    private Sets() {
    }
}
