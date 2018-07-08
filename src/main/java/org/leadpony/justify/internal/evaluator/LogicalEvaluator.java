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

package org.leadpony.justify.internal.evaluator;

import org.leadpony.justify.core.Evaluator;

/**
 * Evaluator to be instantiated by boolean logic.
 * 
 * @author leadpony
 */
public interface LogicalEvaluator extends Evaluator {

    /**
     * The type for building an instance of {@link LogicalEvaluator}.
     * 
     * @author leadpony
     */
    interface Builder {
        
        /**
         * Appends an evaluator of a subschema which composes the boolean logic.
         * 
         * @param evaluator the evaluator for the subschema, which cannot be {@code null}.
         */
        void append(Evaluator evaluator);
        
        /**
         * Builds an evaluator.
         * 
         * @return the built evaluator, may be {@code null}. 
         */
        Evaluator build();
    }
}
