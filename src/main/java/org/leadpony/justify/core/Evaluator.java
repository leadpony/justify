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

package org.leadpony.justify.core;

import java.util.function.Consumer;

import javax.json.stream.JsonParser;

/**
 * Evaluator which applies a JSON schema to a location in a JSON document.
 * 
 * <p>This type is not intended to be used directly by end users.</p>
 * 
 * @author leadpony
 */
@FunctionalInterface
public interface Evaluator {

    /**
     * Result of evaluation done by {@link Evaluator}.
     */
    enum Result {
        /** Evaluation is not done yet. */
        PENDING,
        /** Evaluated as true, which means valid. */
        TRUE,
        /** Evaluated as false, which means invalid. */
        FALSE,
        /** Result of evaluation should be ignored. */
        IGNORED
        ;
    };
    
    /**
     * Evaluates a JSON schema against each instance location to which it applies.
     * 
     * @param event the event triggered by the JSON parser, cannot be {@code null}.
     * @param parser the JSON parser, cannot be {@code null}.
     * @param depth the depth where the event occurred.
     * @param reporter the reporter of the found problems, cannot be {@code null}.
     * @return the result of the evaluation, one defined in {@link Result}. 
     *         This cannot be {@code null}.
     */
    Result evaluate(JsonParser.Event event, JsonParser parser, int depth, Consumer<Problem> reporter);
}
