/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.api.keyword;

import java.util.EnumSet;
import java.util.Set;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;

/**
 * A source of instance evaluators.
 *
 * @author leadpony
 * @since 4.0
 */
public interface EvaluatorSource {

    /**
     * Returns the keyword that provides the evaluators.
     *
     * @return the keyword that provides the evaluators.
     */
    Keyword getSourceKeyword();

    /**
     * Checks if the evaluator supports the specified instance type. All instance
     * types are supported by default.
     *
     * @param type the type to check, never be {@code null}.
     * @return {@code true} if the evaluator supports the instance type,
     *         {@code null} otherwise.
     */
    default boolean supportsType(InstanceType type) {
        return true;
    }

    /**
     * Returns the types supported by the evaluator. All instance types are
     * supported by default.
     *
     * @return the supported types.
     */
    default Set<InstanceType> getSupportedTypes() {
        return EnumSet.allOf(InstanceType.class);
    }

    /**
     * Creates an evaluator of the JSON instance.
     *
     * @param parent the parent evaluator, never be {@code null}.
     * @param type   the type of the target JSON instance to validate, never be
     *               {@code null}.
     * @return the created evaluator to evaluate JSON instances. This cannot be
     *         {@code null}.
     * @throws UnsupportedOperationException if this method is not implemented.
     */
    default Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        throw new UnsupportedOperationException(getClass().getName() + " does not support evaluation.");
    }

    /**
     * Creates a negated evaluator of the JSON instance.
     *
     * @param parent the parent evaluator, never be {@code null}.
     * @param type   the type of the target JSON instance to validate, cannot be
     *               {@code null}.
     * @return the created evaluator to evaluate JSON instances. This cannot be
     *         {@code null}.
     * @throws UnsupportedOperationException if this method is not implemented.
     */
    default Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        throw new UnsupportedOperationException(getClass().getName() + " does not support evaluation.");
    }
}