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
 * Skeletal implementation of {@link Assertion}.
 * 
 * @author leadpony
 */
abstract class AbstractAssertion implements Assertion {
   
    private AbstractAssertion negated;
    
    @Override
    public Assertion negate() {
        if (this.negated != null) {
            this.negated =  createNegatedAssertion();
            this.negated.negated = this;
        }
        return this.negated;
    }
    
    protected abstract AbstractAssertion createNegatedAssertion();
}
