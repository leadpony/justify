/*
 * Copyright 2018-2020 the Justify authors.
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

import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;

import jakarta.json.JsonValue;

/**
 * A skeletal implementation of {@link Keyword}.
 *
 * @author leadpony
 */
public abstract class AbstractKeyword implements Keyword {

    /*
     * JSON representation of this keyword.
     */
    private final JsonValue json;

    /**
     * Constructs this keyword.
     *
     * @param json the JSON representation of this keyword.
     */
    protected AbstractKeyword(JsonValue json) {
        this.json = json;
    }

    @Override
    public final JsonValue getValueAsJson() {
        return json;
    }

    @Override
    public KeywordType getType() {
        throw new UnsupportedOperationException();
    }
}
