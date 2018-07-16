/*
 * Copyright 2018 the Justify authors.
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

/**
 * Assertion specified with "exclusiveMinimum" validation keyword.
 * 
 * @author leadpony
 */
class ExclusiveMinimum extends AbstractNumericBoundAssertion {

    public ExclusiveMinimum(BigDecimal bound) {
        super(bound, "exclusiveMinimum", "instance.problem.exclusiveMinimum");
    }

    @Override
    public String name() {
        return "exclusiveMinimum";
    }

    @Override
    public Assertion negate() {
        return new Maximum(this.bound);
    }

    @Override
    protected boolean test(BigDecimal actual, BigDecimal bound) {
        return actual.compareTo(bound) > 0;
    }
}