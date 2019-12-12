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
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * Assertion specified with "multipleOf" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("multipleOf")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
public class MultipleOf extends AbstractNumericAssertion {

    private final BigDecimal factor;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromNumber mapper = MultipleOf::new;
        return mapper;
    }

    public MultipleOf(JsonValue json, BigDecimal factor) {
        super(json);
        this.factor = factor;
    }

    @Override
    protected boolean testValue(BigDecimal value) {
        BigDecimal remainder = value.remainder(factor);
        return remainder.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_MULTIPLEOF)
            .withParameter("factor", factor)
            .build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_NOT_MULTIPLEOF)
            .withParameter("factor", factor)
            .build();
    }
}
