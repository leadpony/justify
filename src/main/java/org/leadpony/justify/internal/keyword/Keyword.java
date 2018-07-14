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

package org.leadpony.justify.internal.keyword;

import java.util.Map;

import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * Super type of Keywords which can compose a JSON schema.
 * 
 * @author leadpony
 */
public interface Keyword {

    /**
     * Returns the name of this keyword.
     * 
     * @return the name of this keyword, never be {@code null}.
     */
    String name();
    
    /**
     * Checks if this keyword can be evaluated.
     * 
     * @return {@code true} if this keyword can be evaluated, 
     *         {@code false} otherwise.
     */
    boolean canEvaluate();
    
    /**
     * Creates a new evaluator for this keyword.
     * 
     * @param type the type of the instance, cannot be {@code null}.
     * @param appender the type for appending evaluators, cannot be {@code null}.
     * @param jsonProvider the provider of the JSON service.
     */
    void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonProvider jsonProvider);

    /**
     * Returns the negated version of this keyword.
     * 
     * @return the negated version of this keyword.
     */
    default Keyword negate() {
        throw new UnsupportedOperationException(
                name() + " does not support negation.");
    }
    
    /**
     * Adds this keyword to the specified JSON object.
     * 
     * @param builder the builder for building a JSON object, cannot be {@code null}.
     */
    void addToJson(JsonObjectBuilder builder);

    /**
     * Configures this keyword.
     *  
     * @param keywords other keywords in the containing schema.
     */
    default void configure(Map<String, Keyword> others) {
    }
}
