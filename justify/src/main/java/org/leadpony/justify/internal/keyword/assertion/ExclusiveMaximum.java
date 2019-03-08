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

import org.leadpony.justify.internal.base.Message;

/**
 * Assertion specified with "exclusiveMaximum" validation keyword.
 *
 * @author leadpony
 */
class ExclusiveMaximum extends AbstractNumericBoundAssertion {

    public ExclusiveMaximum(BigDecimal limit) {
        super(limit, "exclusiveMaximum", Message.INSTANCE_PROBLEM_EXCLUSIVEMAXIMUM, Message.INSTANCE_PROBLEM_MINIMUM);
    }

    @Override
    protected boolean testValue(BigDecimal actual, BigDecimal limit) {
        return actual.compareTo(limit) < 0;
    }
}
