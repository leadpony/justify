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

import java.util.Collections;
import java.util.HashMap;
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
    
    private static final MessageFormatter messageFormatter = new MessageFormatter();
    
    /**
     * Creates new instance of this builder.
     * 
     * @param parser the JSON parser.
     * @return newly created instance of this type.
     */
    public static ProblemBuilder newBuilder(JsonParser parser) {
        return new ProblemBuilder(parser);
    }
    
    private ProblemBuilder(JsonParser parser) {
        this.location = parser.getLocation();
    }

    public ProblemBuilder withMessage(String messageKey) {
        this.messageKey = messageKey;
        return this;
    }

    public ProblemBuilder withParameter(String name, Object value) {
        this.parameters.put(name, value);
        return this;
    }
    
    /**
     * Builds a problem.
     * 
     * @return built problem.
     */
    public Problem build() {
        return new ValidationProblem(this);
    }

    /**
     * Problem detected in validation process.
     * 
     * @author leadpony
     */
    private static class ValidationProblem implements Problem {

        private final String messageKey;
        private final Map<String, Object> parameters;
        private final JsonLocation location;
    
        private ValidationProblem(ProblemBuilder builder) {
            this.messageKey = builder.messageKey;
            this.parameters = Collections.unmodifiableMap(builder.parameters);
            this.location = builder.location;
        }
        
        @Override
        public String getMessage(Locale locale) {
            Objects.requireNonNull(locale, "locale must not be null.");
            return messageFormatter.format(messageKey, parametersAsMap(), locale);
        }
        
        @Override
        public String getContextualMessage(Locale locale) {
            Objects.requireNonNull(locale, "locale must not be null.");
            return formatContextualMessage(getMessage(locale), locale);
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
        public String toString() {
            return getContextualMessage(Locale.getDefault());
        }
        
        private String formatContextualMessage(String message, Locale locale) {
            Map<String, Object> params = new HashMap<>();
            params.put("message", message);
            params.put("location", formatLocation(getLocation(), locale));
            return messageFormatter.format("format", params, locale);
        }
        
        private String formatLocation(JsonLocation location, Locale locale) {
            if (location == null) {
                return messageFormatter.format("location.unknown", Collections.emptyMap(), locale);
            } else {
                Map<String, Object> params = new HashMap<>();
                params.put("row", location.getLineNumber());
                params.put("col", location.getColumnNumber());
                return messageFormatter.format("location", params, locale);
            }
        }
    }
}
