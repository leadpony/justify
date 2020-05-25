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
 * An assertion keyword representing "maximum" for Draft-04.
 *
 * @author leadpony
 */
@KeywordClass("maximum")
@Spec(SpecVersion.DRAFT_04)
public class Draft04Maximum extends Maximum {

    public static final KeywordType TYPE = KeywordTypes.mappingNumber("maximum", Draft04Maximum::new);

    private boolean exclusive = false;

    public Draft04Maximum(JsonValue json, BigDecimal limit) {
        super(json, limit);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
        if (siblings.containsKey("exclusiveMaximum")) {
            ExclusiveMaximum keyword = (ExclusiveMaximum) siblings.get("exclusiveMaximum");
            exclusive = keyword.value;
        }
    }

    @Override
    protected boolean testValue(BigDecimal actual, BigDecimal limit) {
        if (this.exclusive) {
            return actual.compareTo(limit) < 0;
        } else {
            return actual.compareTo(limit) <= 0;
        }
    }

    @Override
    protected Message getMessageForTest() {
        return exclusive
                ? Message.INSTANCE_PROBLEM_EXCLUSIVEMAXIMUM
                : Message.INSTANCE_PROBLEM_MAXIMUM;
    }

    @Override
    protected Message getMessageForNegatedTest() {
        return exclusive
                ? Message.INSTANCE_PROBLEM_MINIMUM
                : Message.INSTANCE_PROBLEM_EXCLUSIVEMINIMUM;
    }

    /**
     * A keyword of "exclusiveMaximum" in the Draft-04.
     *
     * @author leadpony
     */
    @KeywordClass("exclusiveMaximum")
    @Spec(SpecVersion.DRAFT_04)
    public static class ExclusiveMaximum extends AbstractKeyword {

        public static final KeywordType TYPE = KeywordTypes.mappingBoolean("exclusiveMaximum", ExclusiveMaximum::new);

        private final boolean value;

        public ExclusiveMaximum(JsonValue json, boolean value) {
            super(json);
            this.value = value;
        }

        @Override
        public KeywordType getType() {
            return TYPE;
        }
    }
}
