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
 * @author leadpony
 */
public interface Evaluator {

    /**
     * Status of evaluation.
     */
    enum Status {
        /** Evaluated to true. */
        TRUE,
        /** Evaluated to false. */
        FALSE,
        /** Evaluation is continued. */
        CONTINUED,
        /** Evaluation is canceled. */
        CANCELED
    };
    
    /**
     * Evaluates JSON schema or its assertion.
     * 
     * @param event the event triggered by JSON parser.
     * @param parser the JSON parser.
     * @param collector the collector of detected problems.
     * @return the status of this evaluator.
     */
    Status evaluate(JsonParser.Event event, JsonParser parser, Consumer<Problem> collector);
}
