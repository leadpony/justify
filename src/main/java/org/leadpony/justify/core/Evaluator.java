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
 * Evaluator that applies a JSON schema to a JSON instance.
 * 
 * <p>This type is not intended for end users.</p>
 * 
 * @author leadpony
 */
public interface Evaluator {

    /**
     * Result of evaluation.
     */
    enum Result {
        /** Evaluated as true. */
        TRUE,
        /** Evaluated as false. */
        FALSE,
        /** Evaluation is not done yet. */
        PENDING,
        /** Evaluation is canceled. */
        CANCELED
        ;
    };
    
    /**
     * Evaluates JSON schema.
     * 
     * @param event the event triggered by JSON parser.
     * @param parser the JSON parser.
     * @param depth the depth where the event occurred.
     * @param consumer the consumer of the found problems.
     * @return the result of the evaluation.
     */
    Result evaluate(JsonParser.Event event, JsonParser parser, int depth, Consumer<Problem> consumer);

    Evaluator and(Evaluator other);

    Evaluator or(Evaluator other);

    Evaluator xor(Evaluator other);
}
