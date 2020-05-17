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

import jakarta.json.JsonValue;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * Assertion specified with "minLength" validation keyword.
 *
 * @author leadpony
 */
@KeywordClass("minLength")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class MinLength extends AbstractStringLengthAssertion {

    public static final KeywordType TYPE = KeywordTypes.mappingNonNegativeInteger("minLength", MinLength::new);

    public MinLength(JsonValue json, int limit) {
        super(json, limit, Message.INSTANCE_PROBLEM_MINLENGTH, Message.INSTANCE_PROBLEM_NOT_MINLENGTH);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    protected boolean testLength(int actualLength, int limit) {
        return actualLength >= limit;
    }
}
