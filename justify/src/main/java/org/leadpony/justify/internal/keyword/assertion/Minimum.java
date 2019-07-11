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

import javax.json.JsonValue;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * Assertion specified with "minimum" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("minimum")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Minimum extends AbstractNumericBoundAssertion {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromNumber mapper = Minimum::new;
        return mapper;
    }

    public Minimum(JsonValue json, BigDecimal limit) {
        super(json, limit);
    }

    @Override
    protected boolean testValue(BigDecimal actual, BigDecimal limit) {
        return actual.compareTo(limit) >= 0;
    }

    @Override
    protected Message getMessageForTest() {
        return Message.INSTANCE_PROBLEM_MINIMUM;
    }

    @Override
    protected Message getMessageForNegatedTest() {
        return Message.INSTANCE_PROBLEM_EXCLUSIVEMAXIMUM;
    }
}
