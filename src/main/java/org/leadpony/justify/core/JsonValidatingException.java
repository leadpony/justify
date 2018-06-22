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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonException;

/**
 * <code>JsonValidatingException</code> indicates that some exception happened 
 * during JSON validation.
 * 
 * @author leadpony
 */
@SuppressWarnings("serial")
public class JsonValidatingException extends JsonException {
    
    private final List<Problem> problems;
    
    /**
     * Constructs a new runtime exception.
     * 
     * @param problems the problems found during JSON validating.
     */
    public JsonValidatingException(List<Problem> problems) {
        super(null);
        this.problems = Collections.unmodifiableList(problems);
    }
    
    /**
     * Returns all problems found in validation process.
     * 
     * @return the list of problems, which can not be modified.
     */
    public List<Problem> problems() {
        return problems;
    }
    
    /**
     * Returns the detail message string of this exception.
     * 
     * <p>The message is composed of multiple lines 
     * and each line corresponds to one problem detected.</p>
     * 
     * @return the detail message string of this exception instance.
     */
    @Override
    public String getMessage() {
        return problems().stream()
                .map(Problem::getContextualMessage)
                .collect(Collectors.joining("\n"));
    }
}
