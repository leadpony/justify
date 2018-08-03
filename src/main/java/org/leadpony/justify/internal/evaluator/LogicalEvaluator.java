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
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

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
    interface Builder extends EvaluatorAppender {
        
        /**
         * Specifies the instance of {@link ProblemBuilderFactory}.
         * 
         * @param problemBuilderFactory the problem builder factory to be used by the evaluator.
         * @return this builder.
         */
        Builder withProblemBuilderFactory(ProblemBuilderFactory problemBuilderFactory);
        
        /**
         * Builds an evaluator.
         * 
         * @return the built evaluator, may be {@code null}. 
         */
        Evaluator build();
    }
}
