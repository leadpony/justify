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

import javax.json.JsonValue;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * @author leadpony
 */
abstract class AbstractStringLengthAssertion extends AbstractStringAssertion {

    private final int limit;
    private final Message message;
    private final Message negatedMessage;

    protected AbstractStringLengthAssertion(
            JsonValue json, int limit, Message message, Message negatedMessage) {
        super(json);
        this.limit = limit;
        this.message = message;
        this.negatedMessage = negatedMessage;
    }

    @Override
    protected boolean testValue(String value) {
        return testLength(countCharsIn(value), this.limit);
    }

    @Override
    protected Object toActualValue(String value) {
        return countCharsIn(value);
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        return builder.withMessage(this.message)
            .withParameter("limit", this.limit)
            .build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        return builder.withMessage(this.negatedMessage)
                .withParameter("limit", this.limit)
                .build();
    }

    private static int countCharsIn(String value) {
        return value.codePointCount(0, value.length());
    }

    protected abstract boolean testLength(int actualLength, int limit);
}
