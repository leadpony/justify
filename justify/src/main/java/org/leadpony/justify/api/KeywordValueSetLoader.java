/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.api;

import java.util.Collections;
import java.util.Set;

/**
 * A loader of the limited keyword value set.
 *
 * @author leadpony
 */
public interface KeywordValueSetLoader {

    /**
     * Loads the value set of the specified type.
     *
     * @param <T> the type of the value.
     * @param type the class of the value.
     * @return loaded set of values.
     */
    <T> Set<T> loadKeywordValueSet(Class<T> type);

    /**
     * A loader which will loads nothing.
     */
    KeywordValueSetLoader NEVER = new KeywordValueSetLoader() {
        @Override
        public <T> Set<T> loadKeywordValueSet(Class<T> type) {
            return Collections.emptySet();
        }
    };
}
