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
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * @author leadpony
 */
abstract class AbstractNumericBoundAssertion extends AbstractNumericAssertion {

    private final BigDecimal limit;

    /**
     * Constructs this assertion.
     *
     * @param limit the lower or upper limit.
     */
    protected AbstractNumericBoundAssertion(JsonValue json, BigDecimal limit) {
        super(json);
        this.limit = limit;
    }

    @Override
    protected boolean testValue(BigDecimal value) {
        return testValue(value, this.limit);
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        return builder.withMessage(getMessageForTest())
                .withParameter("limit", this.limit)
                .build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        return builder.withMessage(getMessageForNegatedTest())
                .withParameter("limit", this.limit)
                .build();
    }

    /**
     * Tests a value against the boundary.
     *
     * @param actual the value to test.
     * @param limit the limit of the boundary.
     * @return {@code true} if the value valid, {@code false} otherwise.
     */
    protected abstract boolean testValue(BigDecimal actual, BigDecimal limit);

    protected abstract Message getMessageForTest();

    protected abstract Message getMessageForNegatedTest();
}
