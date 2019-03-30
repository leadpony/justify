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

import java.util.Map;
import java.util.Optional;

import javax.json.JsonReaderFactory;
import javax.json.stream.JsonParserFactory;

/**
 * A builder interface for building an instance of {@code JsonParserFactory} or
 * {@code JsonReaderFactory}.
 *
 * <p>
 * Each instance of this type is NOT safe for use by multiple concurrent
 * threads.
 * </p>
 *
 * @author leadpony
 */
public interface JsonValidatorFactoryBuilder {

    /**
     * The property used to specify whether JSON instances will be filled with
     * default values or not.
     */
    String DEFAULT_VALUES = "org.leadpony.justify.default-values";

    /**
     * Returns all configuration properties as an unmodifiable map.
     *
     * @return all configuration properties. This may be empty.
     */
    Map<String, Object> getAsMap();

    /**
     * Returns the value of the specified configuration property.
     *
     * @param name the name of the property.
     * @return The value of the requested property.
     * @throws NullPointerException if the specified {@code name} is {@code null}.
     */
    Optional<Object> getProperty(String name);

    /**
     * Assigns the new value to the specified configuration property.
     *
     * @param name  the name of the property.
     * @param value the value of the property.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} is {@code null}.
     */
    JsonValidatorFactoryBuilder setProperty(String name, Object value);

    /**
     * Assigns set of configuration properties.
     *
     * @param properties the configuration properties to assign.
     * @return this builder.
     * @throws NullPointerException if the specified {@code properties} is
     *                              {@code null}.
     */
    JsonValidatorFactoryBuilder withProperties(Map<String, ?> properties);

    /**
     * Assigns the handler of problems detected by the JSON validators. This method
     * allows to be used if all parsers and readers can safely share a single problem handler.
     *
     * @param handler the handler of problems, cannot be {@code null}.
     * @return this builder.
     * @throws NullPointerException if the specified {@code handler} is
     *                              {@code null}.
     */
    JsonValidatorFactoryBuilder withProblemHandler(ProblemHandler handler);

    /**
     * Assigns the factory of problem handlers. Problem handlers will be created for
     * each instance of {@code JsonParser} and {@code JsonReader}.
     *
     * @param handlerFactory the factory of problem handlers, cannot be {@code null}
     * @return this builder.
     * @throws NullPointerException if the specified {@code handlerFactory} is
     *                              {@code null}.
     */
    JsonValidatorFactoryBuilder withProblemHandlerFactory(ProblemHandlerFactory handlerFactory);

    /**
     * Assigns whether JSON instances will be filled with default values while being
     * validated or not. The default values are provided by {@code default} keywords
     * in the schema. By default, the default values are ignored and the instances
     * never be modified.
     *
     * @param usingDefaultValues {@code true} to fill the instances with default
     *                           values provided by the schema. {@code false} to
     *                           ingore default values.
     * @return this builder.
     */
    JsonValidatorFactoryBuilder withDefaultValues(boolean usingDefaultValues);

    /**
     * Builds a new instance of {@code JsonParserFactory}.
     *
     * @return a new instance of {@code JsonParserFactory} configured by this
     *         builder.
     */
    JsonParserFactory buildParserFactory();

    /**
     * Builds a new instance of {@code JsonReaderFactory}.
     *
     * @return a new instance of {@code JsonReaderFactory} configured by this
     *         builder.
     */
    JsonReaderFactory buildReaderFactory();
}
