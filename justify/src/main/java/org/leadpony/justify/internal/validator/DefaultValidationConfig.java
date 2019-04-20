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
package org.leadpony.justify.internal.validator;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ProblemHandlerFactory;
import org.leadpony.justify.api.ValidationConfig;

/**
 * The default implementation of {@link ValidationConfig}.
 *
 * @author leadpony
 */
@SuppressWarnings("serial")
public class DefaultValidationConfig extends HashMap<String, Object >
    implements ValidationConfig {

    public DefaultValidationConfig() {
    }

    @Override
    public Map<String, Object> getAsMap() {
        return Collections.unmodifiableMap(this);
    }

    @Override
    public Optional<Object> getProperty(String name) {
        return Optional.ofNullable(get(name));
    }

    @Override
    public ValidationConfig setProperty(String name, Object value) {
        requireNonNull(name, "name");
        put(name, value);
        return this;
    }

    @Override
    public ValidationConfig withProperties(Map<String, ?> properties) {
        if (properties != null) {
            putAll(properties);
        }
        return this;
    }

    @Override
    public ValidationConfig withSchema(JsonSchema schema) {
        requireNonNull(schema, "schema");
        return setProperty(SCHEMA, schema);
    }

    @Override
    public ValidationConfig withProblemHandler(ProblemHandler handler) {
        requireNonNull(handler, "handler");
        return withProblemHandlerFactory(p->handler);
    }

    @Override
    public ValidationConfig withProblemHandlerFactory(ProblemHandlerFactory handlerFactory) {
        requireNonNull(handlerFactory, "handlerFactory");
        return setProperty(PROBLEM_HANDLER_FACTORY, handlerFactory);
    }

    @Override
    public ValidationConfig withDefaultValues(boolean usingDefaultValues) {
        return setProperty(DEFAULT_VALUES, usingDefaultValues);
    }
}
