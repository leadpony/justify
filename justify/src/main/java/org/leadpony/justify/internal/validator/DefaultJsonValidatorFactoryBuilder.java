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

import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidatorFactoryBuilder;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ProblemHandlerFactory;
import org.leadpony.justify.internal.base.json.DefaultJsonReaderFactory;

/**
 * The default implementation of {@link JsonValidatorFactoryBuilder}.
 *
 * @author leadpony
 */
public class DefaultJsonValidatorFactoryBuilder implements JsonValidatorFactoryBuilder {

    private final JsonSchema schema;
    private final JsonProvider jsonProvider;

    private final Map<String, Object> properties = new HashMap<>();
    private ProblemHandlerFactory handlerFactory = parser -> problems -> {
    };

    public DefaultJsonValidatorFactoryBuilder(JsonSchema schema, JsonProvider jsonProvider) {
        this.schema = schema;
        this.jsonProvider = jsonProvider;
    }

    @Override
    public Map<String, Object> getAsMap() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public Optional<Object> getProperty(String name) {
        requireNonNull(name, "name");
        return Optional.ofNullable(properties.get(name));
    }

    @Override
    public JsonValidatorFactoryBuilder setProperty(String name, Object value) {
        requireNonNull(name, "name");
        properties.put(name, value);
        return this;
    }

    @Override
    public JsonValidatorFactoryBuilder withProperties(Map<String, ?> properties) {
        requireNonNull(properties, "properties");
        this.properties.putAll(properties);
        return this;
    }

    @Override
    public JsonValidatorFactoryBuilder withProblemHandler(ProblemHandler handler) {
        requireNonNull(handler, "handler");
        this.handlerFactory = parser -> handler;
        return this;
    }

    @Override
    public JsonValidatorFactoryBuilder withProblemHandlerFactory(ProblemHandlerFactory handlerFactory) {
        requireNonNull(handlerFactory, "handlerFactory");
        this.handlerFactory = handlerFactory;
        return this;
    }

    @Override
    public JsonValidatorFactoryBuilder withDefaultValues(boolean usingDefaultValues) {
        return setProperty(DEFAULT_VALUES, usingDefaultValues);
    }

    @Override
    public JsonParserFactory buildParserFactory() {
        JsonParserFactory factory = jsonProvider.createParserFactory(getAsMap());
        return new JsonValidatorFactory(
                schema, jsonProvider, factory, handlerFactory, properties);
    }

    @Override
    public JsonReaderFactory buildReaderFactory() {
        return new DefaultJsonReaderFactory(buildParserFactory(), getAsMap());
    }
}
