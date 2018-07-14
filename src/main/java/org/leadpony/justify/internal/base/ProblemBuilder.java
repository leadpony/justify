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

package org.leadpony.justify.internal.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Problem;

/**
 * This class builds problems detected in validation process.
 * 
 * @author leadpony
 */
public class ProblemBuilder {

    private final JsonLocation location;
    private String messageKey;
    private final Map<String, Object> parameters = new HashMap<>();
    private List<List<Problem>> childLists;
    
    /**
     * Creates new instance of this builder.
     * 
     * @param location the location where problem occurred, cannot be {@code null}.
     * @return newly created instance of this type.
     */
    public static ProblemBuilder newBuilder(JsonLocation location) {
        return new ProblemBuilder(location);
    }

    /**
     * Creates new instance of this builder.
     * 
     * @param parser the JSON parser, cannot be {@code null}.
     * @return newly created instance of this type.
     */
    public static ProblemBuilder newBuilder(JsonParser parser) {
        return newBuilder(parser.getLocation());
    }

    /**
     * Constructs this builder.
     * 
     * @param location the location where problem occurred, cannot be {@code null}.
     */
    private ProblemBuilder(JsonLocation location) {
        this.location = location;
    }

    /**
     * Specifies the key name of the message used for the problem.
     * 
     * @param messageKey the key name of the message
     * @return this builder.
     */
    public ProblemBuilder withMessage(String messageKey) {
        this.messageKey = messageKey;
        return this;
    }

    /**
     * Specifies the parameter which will be added to the problem.
     * 
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @return this builder.
     */
    public ProblemBuilder withParameter(String name, Object value) {
        this.parameters.put(name, value);
        return this;
    }
    
    /**
     * Specifies the child problems of the problem to be built.
     * 
     * @param problems the list of problems which are children of the problem to be built.
     * @return this builder.
     */
    public ProblemBuilder withSubproblems(List<Problem> problems) {
        if (this.childLists == null) {
            this.childLists = new ArrayList<>();
        }
        this.childLists.add(Collections.unmodifiableList(problems));
        return this;
    }
    
    /**
     * Builds a problem.
     * 
     * @return built problem.
     */
    public Problem build() {
        if (this.childLists == null || this.childLists.isEmpty()) {
            return new SimpleProblem(this);
        } else {
            return new CompositexProblem(this);
        }
    }

    /**
     * Problem without child problems.
     * 
     * @author leadpony
     */
    private static class SimpleProblem implements Problem {

        private final String messageKey;
        private final Map<String, Object> parameters;
        private final JsonLocation location;
    
        /**
         * Constructs this problem.
         * 
         * @param builder the builder of the problem.
         */
        protected SimpleProblem(ProblemBuilder builder) {
            this.messageKey = builder.messageKey;
            this.parameters = Collections.unmodifiableMap(builder.parameters);
            this.location = builder.location;
        }
        
        @Override
        public String getMessage(Locale locale) {
            Objects.requireNonNull(locale, "locale must not be null.");
            return buildMessage(locale).toString();
        }
        
        @Override
        public String getContextualMessage(Locale locale) {
            Objects.requireNonNull(locale, "locale must not be null.");
            Message message = buildMessage(locale);
            return buildContextualMessage(message, locale).toString();
        }
    
        @Override
        public JsonLocation getLocation() {
            return location;
        }
        
        @Override
        public Map<String, ?> parametersAsMap() {
            return parameters;
        }
        
        @Override
        public boolean hasSubproblem() {
            return false;
        }
        
        @Override
        public List<List<Problem>> getSubproblems() {
            return Collections.emptyList();
        }
        
        @Override
        public String toString() {
            return getContextualMessage();
        }
        
        /**
         * Builds a message for the specified locale.
         * 
         * @param locale the locale for which the message will be localized. 
         * @return the built message.
         */
        private Message buildMessage(Locale locale) {
            return Message.get(messageKey, locale).withParameters(parameters);
        }
        
        /**
         * Builds a message including the location at which this problem occurred.
         * 
         * @param message the original message.
         * @param locale the locale for which the message will be localized. 
         * @return the built message.
         */
        private Message buildContextualMessage(Message message, Locale locale) {
            return Message.get("format", locale)
                    .withParameter("message", message)
                    .withParameter("location", buildLocation(getLocation(), locale));
        }
        
        private Message buildLocation(JsonLocation location, Locale locale) {
            if (location == null) {
                return Message.get("location.unknown", locale);
            } else {
                return Message.get("location", locale)
                        .withParameter("row", location.getLineNumber())
                        .withParameter("col", location.getColumnNumber());
            }
        }
    }
    
    /**
     * Problem with child problems.
     * 
     * @author leadpony
     */
    private static class CompositexProblem extends SimpleProblem {
        
        /**
         * Lists of subproblems.
         */
        private final List<List<Problem>> subproblems;

        /**
         * Constructs this problem.
         * 
         * @param builder the builder of the problem.
         */
        CompositexProblem(ProblemBuilder builder) {
            super(builder);
            this.subproblems = Collections.unmodifiableList(builder.childLists);
        }

        @Override
        public boolean hasSubproblem() {
            return true;
        }
        
        @Override
        public List<List<Problem>> getSubproblems() {
            return subproblems;
        }
    }
}
