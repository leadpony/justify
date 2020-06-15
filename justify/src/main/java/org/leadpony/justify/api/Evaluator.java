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

package org.leadpony.justify.api;

import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

/**
 * An evaluator interface for applying a JSON schema to a JSON instance.
 *
 * <p>
 * Note that this type is not intended to be used directly by end users.
 * </p>
 *
 * @author leadpony
 */
public interface Evaluator {

    /**
     * The results of the evaluation done by {@code Evaluator}.
     */
    enum Result {
        /** Evaluation is not done yet. */
        PENDING,
        /** Evaluated as true, which means valid. */
        TRUE,
        /** Evaluated as false, which means invalid. */
        FALSE
    };

    /**
     * Evaluates a JSON schema against each instance location to which it applies.
     *
     * @param event      the event triggered by the JSON parser, cannot be
     *                   {@code null}.
     * @param depth      the depth where the event occurred.
     * @param dispatcher the dispatcher of the found problems, cannot be
     *                   {@code null}.
     * @return the result of the evaluation, one defined in {@link Result}. This
     *         cannot be {@code null}.
     */
    Result evaluate(JsonParser.Event event, int depth, ProblemDispatcher dispatcher);

    /**
     * Checks whether this evaluator evaluates anything as false or not. This method
     * returns {@code false} by default.
     *
     * @return {@code true} if this evaluator is always false, otherwise
     *         {@code false}.
     */
    default boolean isAlwaysFalse() {
        return false;
    }

    default Evaluator getParent() {
        throw new UnsupportedOperationException();
    }

    default EvaluatorContext getContext() {
        return getParent().getContext();
    }

    default JsonSchema getSchema() {
        return getParent().getSchema();
    }

    /**
     * The evaluator which evaluates anything as true.
     */
    Evaluator ALWAYS_TRUE = new Evaluator() {
        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            return Result.TRUE;
        }
    };
}
