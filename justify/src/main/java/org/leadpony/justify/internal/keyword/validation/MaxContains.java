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

package org.leadpony.justify.internal.keyword.validation;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.keyword.AbstractKeyword;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * The type representing "maxContains" keyword.
 *
 * @author leadpony
 */
public class MaxContains extends AbstractKeyword {

    static final KeywordType TYPE = KeywordTypes.mappingNonNegativeInteger("maxContains", MaxContains::new);

    private final int limit;

    public MaxContains(JsonValue json, int limit) {
        super(json);
        this.limit = limit;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    /**
     * Return the value of this keyword.
     *
     * @return the value of this keyword.
     */
    public final int value() {
        return limit;
    }
}
