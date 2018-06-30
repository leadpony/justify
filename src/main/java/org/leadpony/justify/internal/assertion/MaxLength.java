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

package org.leadpony.justify.internal.assertion;

/**
 * Assertion specified with "maxLength" keyword.
 * 
 * @author leadpony
 */
class MaxLength extends AbstractStringLengthAssertion {

    public MaxLength(int bound) {
        super(bound, "maxLength", "instance.problem.max.length");
    }

    @Override
    protected boolean test(int actualLength, int bound) {
        return actualLength <= bound;
    }

    @Override
    protected AbstractAssertion createNegatedAssertion() {
        return new ExclusiveMinLength(bound);
    }
}
