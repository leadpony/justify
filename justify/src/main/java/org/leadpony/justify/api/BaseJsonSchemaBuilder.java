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

import org.leadpony.justify.api.keyword.Keyword;

/**
 * A base type of {@link JsonSchemaBuilder}.
 *
 * @author leadpony
 */
public interface BaseJsonSchemaBuilder {

    /**
     * Adds a schema keyword to this builder.
     *
     * @param keyword the schema keyword to add, cannot be {@code null}.
     */
    void addKeyword(Keyword keyword);
}
