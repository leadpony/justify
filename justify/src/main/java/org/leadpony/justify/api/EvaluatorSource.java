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
package org.leadpony.justify.api;

import java.util.EnumSet;
import java.util.Set;

/**
 * A source of instance evaluators.
 *
 * @author leadpony
 */
public interface EvaluatorSource extends Keyword {

    /**
     * Checks if the evaluator supports the specified instance type.
     * All instance types are supported by default.
     *
     * @param type the type to check, never be {@code null}.
     * @return {@code true} if the evaluator supports the instance type, {@code null} otherwise.
     */
    default boolean supportsType(InstanceType type) {
        return true;
    }

    /**
     * Returns the types supported by the evaluator.
     * All instance types are supported by default.
     *
     * @return the supported types.
     */
    default Set<InstanceType> getSupportedTypes() {
        return EnumSet.allOf(InstanceType.class);
    }

    /**
     * Creates an evaluator of the JSON instance.
     *
     * @param context the context shared by all evaluators in the current
     *                validation, never be {@code null}.
     * @param schema  the owning schema of this keyword, never be {@code null}.
     * @param type    the type of the target JSON instance to validate, never be
     *                {@code null}.
     * @return the created evaluator to evaluate JSON instances. This cannot be
     *         {@code null}.
     */
    default Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        if (!supportsType(type)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return doCreateEvaluator(context, schema, type);
    }

    /**
     * Creates a negated evaluator of the JSON instance.
     *
     * @param context the context shared by all evaluators in the current
     *                validation, never be {@code null}.
     * @param schema  the owning schema of this keyword, cannot be {@code null}.
     * @param type    the type of the target JSON instance to validate, cannot be
     *                {@code null}.
     * @return the created evaluator to evaluate JSON instances. This cannot be
     *         {@code null}.
     */
    default Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        if (!supportsType(type)) {
            return context.createMismatchedTypeEvaluator(schema, this, getSupportedTypes(), type);
        }
        return doCreateNegatedEvaluator(context, schema, type);
    }

    /**
     * Creates an evaluator of the target type from this keyword.
     *
     * @param context the context shared by all evaluators in the current
     *                validation, never be {@code null}.
     * @param schema  the owning schema of this keyword, never be {@code null}.
     * @param type    the type of the instance, cannot be {@code null}.
     * @return the created evaluator to evaluate JSON instances. This cannot be
     *         {@code null}.
     */
    default Evaluator doCreateEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        throw new UnsupportedOperationException(name() + " does not support evaluation.");
    }

    /**
     * Creates an evaluator of the target type from the negation of this keyword.
     *
     * @param context the context shared by all evaluators in the current
     *                validation, never be {@code null}.
     * @param schema  the owning schema of this keyword, never be {@code null}.
     * @param type    the type of the instance, cannot be {@code null}.
     * @return the created evaluator to evaluate JSON instances. This cannot be
     *         {@code null}.
     */
    default Evaluator doCreateNegatedEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        throw new UnsupportedOperationException(name() + " does not support evaluation.");
    }
}
