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
package org.leadpony.justify.internal.keyword;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType.CreationContext;

/**
 * A factory of keywords.
 *
 * @author leadpony
 */
public interface KeywordFactory {

    /**
     * Creates a keyword.
     *
     * @param name the name of the keyword to create never be {@code null}.
     * @param value the value of the keyword, never be {@code null}.
     * @param context the creation context never be {@code null}.
     * @return newly created keyword, or {@code null}.
     */
    Keyword createKeyword(String name, JsonValue value, CreationContext context);
}
