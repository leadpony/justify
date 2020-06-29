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
import org.leadpony.justify.api.EvaluatorSource;
import org.leadpony.justify.api.InstanceType;

/**
 * A keyword which is also an {@link EvaluatorSource}.
 *
 * @author leadpony
 */
public interface EvaluationKeyword extends Keyword, EvaluatorSource {

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
     * Checks whether this keyword is exclusive one or not.
     *
     * @return {@code true} if this keyword is exclusive one, {@code false} otherwise.
     */
    default boolean isExclusive() {
        return false;
    }

    /**
     * {@inheritDoc}}
     */
    @Override
    default Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        throw new UnsupportedOperationException(getClass().getName() + " does not support evaluation.");
    }

    /**
     * {@inheritDoc}}
     */
    @Override
    default Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        throw new UnsupportedOperationException(getClass().getName() + " does not support evaluation.");
    }
}
