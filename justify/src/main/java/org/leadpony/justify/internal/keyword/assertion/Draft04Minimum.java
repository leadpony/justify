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
package org.leadpony.justify.internal.keyword.assertion;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.AbstractKeyword;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * An assertion keyword representing "minimum" for Draft-04.
 *
 * @author leadpony
 */
@KeywordClass("minimum")
@Spec(SpecVersion.DRAFT_04)
public class Draft04Minimum extends Minimum {

    public static final KeywordType TYPE = KeywordTypes.mappingNumber("minimum", Draft04Minimum::new);

    private boolean exclusive = false;

    public Draft04Minimum(JsonValue json, BigDecimal limit) {
        super(json, limit);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Keyword link(Map<String, Keyword> siblings) {
        if (siblings.containsKey("exclusiveMinimum")) {
            ExclusiveMinimum keyword = (ExclusiveMinimum) siblings.get("exclusiveMinimum");
            exclusive = keyword.value;
        }
        return this;
    }

    @Override
    protected boolean testValue(BigDecimal actual, BigDecimal limit) {
        if (this.exclusive) {
            return actual.compareTo(limit) > 0;
        } else {
            return actual.compareTo(limit) >= 0;
        }
    }

    @Override
    protected Message getMessageForTest() {
        return exclusive
                ? Message.INSTANCE_PROBLEM_EXCLUSIVEMINIMUM
                : Message.INSTANCE_PROBLEM_MINIMUM;
    }

    @Override
    protected Message getMessageForNegatedTest() {
        return exclusive
                ? Message.INSTANCE_PROBLEM_MAXIMUM
                : Message.INSTANCE_PROBLEM_EXCLUSIVEMAXIMUM;
    }

    /**
     * A keyword of "exclusiveMinimum" in the Draft-04.
     *
     * @author leadpony
     */
    @KeywordClass("exclusiveMinimum")
    @Spec(SpecVersion.DRAFT_04)
    public static class ExclusiveMinimum extends AbstractKeyword {

        public static final KeywordType TYPE = KeywordTypes.mappingBoolean("exclusiveMinimum", ExclusiveMinimum::new);

        private final boolean value;

        public ExclusiveMinimum(JsonValue json, boolean value) {
            super(json);
            this.value = value;
        }

        @Override
        public KeywordType getType() {
            return TYPE;
        }
    }
}
