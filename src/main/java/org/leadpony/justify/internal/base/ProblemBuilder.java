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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.stream.JsonLocation;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;

/**
 * This class builds problems detected in validation process.
 * 
 * @author leadpony
 */
public class ProblemBuilder {

    private final JsonLocation location;
    private JsonSchema schema;
    private String keyword;
    private String messageKey;
    private final Map<String, Object> parameters = new HashMap<>();
    private List<List<Problem>> childLists;
    
    /**
     * Constructs this builder.
     * 
     * @param location the location where problem occurred, cannot be {@code null}.
     */
    ProblemBuilder(JsonLocation location) {
        this.location = location;
    }
    
    /**
     * Specifies the keyword which supplies the constraint the problem violated.
     * 
     * @param keyword the keyword supplying the constraint.
     * @return this builder.
     */
    public ProblemBuilder withKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }
    
    /**
     * Specifies the schema whose evaluation caused this problem.
     * 
     * @param schema the schema whose evaluation caused this problem.
     * @return this builder.
     */
    public ProblemBuilder withSchema(JsonSchema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Specifies the key name of the message used for the problem.
     * 
     * @param messageKey the key name of the message.
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

        private final JsonLocation location;
        private final JsonSchema schema;
        private final String keyword;
        private final String messageKey;
        private final Map<String, Object> parameters;
    
        /**
         * Constructs this problem.
         * 
         * @param builder the builder of the problem.
         */
        protected SimpleProblem(ProblemBuilder builder) {
            this.location = builder.location;
            this.schema = builder.schema;
            this.keyword = builder.keyword;
            this.messageKey = builder.messageKey;
            this.parameters = Collections.unmodifiableMap(builder.parameters);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getMessage(Locale locale) {
            requireNonNull(locale, "locale");
            return buildMessage(locale).toString();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getContextualMessage(Locale locale) {
            requireNonNull(locale, "locale");
            Message message = buildMessage(locale);
            return buildContextualMessage(message, locale).toString();
        }
    
        /**
         * {@inheritDoc}
         */
        @Override
        public JsonLocation getLocation() {
            return location;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public JsonSchema getSchema() {
            return schema;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getKeyword() {
            return keyword;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, ?> parametersAsMap() {
            return parameters;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasSubproblem() {
            return false;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public List<List<Problem>> getSubproblems() {
            return Collections.emptyList();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void printAll(Consumer<String> lineConsumer) {
            requireNonNull(lineConsumer, "lineConsumer");
            ProblemPrinter.printProblem(this, lineConsumer);
        }

        /**
         * {@inheritDoc}
         */
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
        
        /**
         * Builds a message containing the location at which this problem occurred.
         * 
         * @param location the location at which this problem occurred.
         * @param locale the locale for which the message will be localized. 
         * @return the built message.
         */
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
