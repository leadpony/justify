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
package org.leadpony.justify.internal.keyword.core;

import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.SimpleValueKeyword;
import org.leadpony.justify.internal.keyword.AbstractKeyword;
import org.leadpony.justify.internal.keyword.KeywordTypes;

import jakarta.json.JsonValue;

/**
 * @author leadpony
 */
public class Anchor extends AbstractKeyword implements SimpleValueKeyword<String> {

    static final KeywordType TYPE = KeywordTypes.mappingString("$anchor", Anchor::new);

    private final String value;

    public Anchor(JsonValue json, String value) {
        super(json);
        this.value = value;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public String value() {
        return value;
    }
}
