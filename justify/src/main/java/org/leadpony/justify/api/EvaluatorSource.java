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

/**
 * A source of instance evaluators.
 *
 * @author leadpony
 * @since 4.0
 */
public interface EvaluatorSource {

    /**
     * Creates an evaluator of the JSON instance.
     * <p>
     * Note that this method is not intended to be used directly by end users.
     * </p>
     *
     * @param parent the parent evaluator, never be {@code null}.
     * @param type   the type of the target JSON instance to validate, never be
     *               {@code null}.
     * @return the created evaluator to evaluate JSON instances. This cannot be
     *         {@code null}.
     * @throws UnsupportedOperationException if this method is not implemented.
     * @throws NullPointerException          if any of the arguments is
     *                                       {@code null}.
     */
    Evaluator createEvaluator(Evaluator parent, InstanceType type);

    /**
     * Creates a negated evaluator of the JSON instance.
     * <p>
     * Note that this method is not intended to be used directly by end users.
     * </p>
     *
     * @param parent the parent evaluator, never be {@code null}.
     * @param type   the type of the target JSON instance to validate, cannot be
     *               {@code null}.
     * @return the created evaluator to evaluate JSON instances. This cannot be
     *         {@code null}.
     * @throws UnsupportedOperationException if this method is not implemented.
     * @throws NullPointerException          if any of the arguments is
     *                                       {@code null}.
     */
    Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type);
}
