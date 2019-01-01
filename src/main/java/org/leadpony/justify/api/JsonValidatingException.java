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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParsingException;

/**
 * {@code JsonValidatingException} indicates that some exception happened 
 * while validating a JSON document.
 * 
 * @author leadpony
 */
@SuppressWarnings("serial")
public class JsonValidatingException extends JsonParsingException {
    
    private final List<Problem> problems;
    
    /**
     * Constructs a new runtime exception.
     * 
     * @param problems the problems found while validating the JSON document.
     */
    public JsonValidatingException(List<Problem> problems) {
        super(null, extractFirstLocation(problems));
        this.problems = Collections.unmodifiableList(problems);
    }
    
    /**
     * Returns all problems found in the validation process.
     * 
     * @return unmodifiable collection of problems, which never be {@code null}.
     */
    public List<Problem> getProblems() {
        return problems;
    }
    
    /**
     * Returns the detail message string of this exception.
     * <p>
     * The message is composed of multiple lines 
     * and each line corresponds to a problem found in the validation process.
     * </p>
     * 
     * @return the detail message string of this exception instance.
     */
    @Override
    public String getMessage() {
        return getProblems().stream()
                .map(Problem::getContextualMessage)
                .collect(Collectors.joining("\n"));
    }
    
    private static JsonLocation extractFirstLocation(List<Problem> problems) {
        if (problems.isEmpty()) {
            return null;
        } else {
            return problems.get(0).getLocation();
        }
    }
}
