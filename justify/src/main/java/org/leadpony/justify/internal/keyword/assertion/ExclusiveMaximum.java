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

package org.leadpony.justify.internal.keyword.assertion;

import java.math.BigDecimal;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * Assertion specified with "exclusiveMaximum" validation keyword.
 *
 * @author leadpony
 */
@KeywordClass("exclusiveMaximum")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class ExclusiveMaximum extends AbstractNumericBoundAssertion {

    public static final KeywordType TYPE = KeywordTypes.mappingNumber("exclusiveMaximum", ExclusiveMaximum::new);

    public ExclusiveMaximum(JsonValue json, BigDecimal limit) {
        super(json, limit);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    protected boolean testValue(BigDecimal actual, BigDecimal limit) {
        return actual.compareTo(limit) < 0;
    }

    @Override
    protected Message getMessageForTest() {
        return Message.INSTANCE_PROBLEM_EXCLUSIVEMAXIMUM;
    }

    @Override
    protected Message getMessageForNegatedTest() {
        return Message.INSTANCE_PROBLEM_MINIMUM;
    }
}
