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

package org.leadpony.justify.internal.assertion;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;

/**
 * Assertion on JSON instances.
 * 
 * @author leadpony
 */
public interface Assertion {
    
    String name();
    
    /**
     * Checks if this assertion can apply to the specified type of JSON instance.
     * 
     * @param type the type of the instance.
     * @return {@code true} if this assertion can apply to the instance, {@code false} otherwise.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    default boolean canApplyTo(InstanceType type) {
        return true;
    }
    
    /**
     * Creates a new evaluator for this assertion.
     * 
     * @param type the type of the instance.
     * @return newly created evaluator, never be {@code null}.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    Evaluator createEvaluator(InstanceType type);
    
    Assertion negate();
    
    void toJson(JsonGenerator generator);
}
