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

import org.leadpony.justify.api.keyword.InvalidKeywordException;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.SubschemaParser;
import org.leadpony.justify.internal.keyword.AbstractKeyword;

import jakarta.json.JsonValue;

/**
 * @author leadpony
 * @since 4.0
 */
public final class RecursiveAnchor extends AbstractKeyword {

    static final String NAME = "$recursiveAnchor";

    public static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            switch (jsonValue.getValueType()) {
            case TRUE:
                return RecursiveAnchor.TRUE;
            case FALSE:
                return RecursiveAnchor.FALSE;
            default:
                throw new InvalidKeywordException("Must be a boolean");
            }
        }
    };

    public static final RecursiveAnchor TRUE = new RecursiveAnchor(JsonValue.TRUE);
    public static final RecursiveAnchor FALSE = new RecursiveAnchor(JsonValue.FALSE);

    private RecursiveAnchor(JsonValue json) {
        super(json);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }
}
