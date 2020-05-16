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

import jakarta.json.JsonValue;

import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.internal.annotation.KeywordType;

/**
 * A skeletal implementation of {@link Keyword}.
 *
 * @author leadpony
 */
public abstract class AbstractKeyword implements Keyword {

    /*
     * the name of this keyword.
     */
    private final String name;

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
        this.name = guessOwnName();
        this.json = json;
    }

    /**
     * Constructs this keyword.
     *
     * @param name the name of this keyword.
     * @param json the JSON representation of this keyword.
     */
    protected AbstractKeyword(String name, JsonValue json) {
        this.name = name;
        this.json = json;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final JsonValue getValueAsJson() {
        return json;
    }

    private String guessOwnName() {
        KeywordType keywordType = getClass().getAnnotation(KeywordType.class);
        return keywordType.value();
    }
}
