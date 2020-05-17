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

import jakarta.json.JsonValue;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.KeywordTypes;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * An assertion specified with "const" validation keyword.
 *
 * @author leadpony
 */
@KeywordClass("const")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Const extends AbstractEqualityAssertion {

    public static final KeywordType TYPE = KeywordTypes.mappingJson("const", Const::new);

    public Const(JsonValue expected) {
        super(expected);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    protected boolean testValue(JsonValue value) {
        return value.equals(getValueAsJson());
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        final JsonValue expected = getValueAsJson();
        return builder.withMessage(Message.INSTANCE_PROBLEM_CONST)
            .withParameter("expected", expected)
            .withParameter("expectedType", InstanceType.of(expected))
            .build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        final JsonValue expected = getValueAsJson();
        return builder.withMessage(Message.INSTANCE_PROBLEM_NOT_CONST)
            .withParameter("expected", expected)
            .withParameter("expectedType", InstanceType.of(expected))
            .build();
    }
}
