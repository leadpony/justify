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

/**
 * A configuration type for configuring JSON validation.
 *
 * The map generated from this configuration can be passed to the methods like
 * {@link JsonValidationService#createParserFactory(Map)} and
 * {@link JsonValidationService#createReaderFactory(Map)}.
 *
 * <p>
 * Each instance of this type is NOT safe for use by multiple concurrent
 * threads.
 * </p>
 *
 * @author leadpony
 */
public interface ValidationConfig {

    /**
     * The property used to specify whether JSON instances will be filled with
     * default values or not.
     */
    String DEFAULT_VALUES = "org.leadpony.justify.api.ValidationConfig.DEFAULT_VALUES";

    /**
     * The property used to specify the factory of problem handlers.
     */
    String PROBLEM_HANDLER_FACTORY = "org.leadpony.justify.api.ValidationConfig.PROBLEM_HANDLER_FACTORY";

    /**
     * The property used to specify the JSON schema.
     */
    String SCHEMA = "org.leadpony.justify.api.ValidationConfig.SCHEMA";

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
    ValidationConfig setProperty(String name, Object value);

    /**
     * Assigns set of configuration properties.
     *
     * @param properties the configuration properties to assign, can be
     *                   {@code null}.
     * @return this builder.
     */
    ValidationConfig withProperties(Map<String, ?> properties);

    /**
     * Specifies the JSON schema used by the validators.
     *
     * @param schema the JSON schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code schema} is {@code null}.
     */
    ValidationConfig withSchema(JsonSchema schema);

    /**
     * Specifies the handler of problems detected by the JSON validators. This
     * method allows to be used if and only if all parsers and readers can safely
     * share a single problem handler.
     *
     * @param handler the handler of problems, cannot be {@code null}.
     * @return this builder.
     * @throws NullPointerException if the specified {@code handler} is
     *                              {@code null}.
     */
    ValidationConfig withProblemHandler(ProblemHandler handler);

    /**
     * Specifies the factory of problem handlers. Problem handlers will be created
     * for each instance of {@code JsonParser} and {@code JsonReader}.
     *
     * @param handlerFactory the factory of problem handlers, cannot be {@code null}
     * @return this builder.
     * @throws NullPointerException if the specified {@code handlerFactory} is
     *                              {@code null}.
     */
    ValidationConfig withProblemHandlerFactory(ProblemHandlerFactory handlerFactory);

    /**
     * Specifies whether JSON instances will be filled with default values while
     * being validated or not. The default values are provided by {@code default}
     * keywords in the schema. By default, the default values are ignored and the
     * instances never be modified.
     *
     * @param usingDefaultValues {@code true} to fill the instances with default
     *                           values provided by the schema. {@code false} to
     *                           ingore default values.
     * @return this builder.
     */
    ValidationConfig withDefaultValues(boolean usingDefaultValues);
}
