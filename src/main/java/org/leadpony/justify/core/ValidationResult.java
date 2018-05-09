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

/**
 * Result obtained by JSON validation.
 * 
 * @author leadpony
 */
public interface ValidationResult {

    /**
     * Checks if the validation was completed without any problem.
     * 
     * @return {@code true} if the validation was done without problem, 
     *         {@code false} otherwise.
     */
    boolean wasSuccess();
    
    /**
     * Returns all problems found in the process of validation.
     * 
     * @return the object to iterate problems, never be {@code null}.
     */
    Iterable<Problem> problems();
}
