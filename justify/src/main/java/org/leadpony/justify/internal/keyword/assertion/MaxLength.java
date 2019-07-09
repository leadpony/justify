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

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * Assertion specified with "maxLength" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("maxLength")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class MaxLength extends AbstractStringLengthAssertion {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromNonNegativeInteger mapper = MaxLength::new;
        return mapper;
    }

    public MaxLength(int limit) {
        super(limit, Message.INSTANCE_PROBLEM_MAXLENGTH, Message.INSTANCE_PROBLEM_NOT_MAXLENGTH);
    }

    @Override
    protected boolean testLength(int actualLength, int limit) {
        return actualLength <= limit;
    }
}
