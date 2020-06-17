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

import java.util.List;
import java.util.Map;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParser;

/**
 * A context shared by all evaluators participating in the current validation.
 *
 * <p>
 * Note that this type is not intended to be used directly by end users.
 * </p>
 *
 * @author leadpony
 */
public interface EvaluatorContext {

    /**
     * Returns the parser being used while validating the instance.
     *
     * @return the current parser, never be {@code null}.
     */
    JsonParser getParser();

    /**
     * Returns the current location in the instance as a JSON pointer.
     *
     * @return the JSON pointer which points to the current location in the
     *         instance.
     */
    String getPointer();

    /**
     * Returns the instance of JSON provider.
     *
     * @return the instance of JSON provider.
     */
    JsonProvider getJsonProvider();

    /**
     * Returns the instance of JSON builder factory.
     *
     * @return the instance of JSON builder factory.
     */
    JsonBuilderFactory getJsonBuilderFactory();

    /**
     * Checks if the current validator accepts default values or not.
     *
     * @return {@code true} if the validator accepts default values, {@code false}
     *         if it does not.
     */
    boolean acceptsDefaultValues();

    /**
     * Inserts default values at the end of the object.
     *
     * @param properties the pairs each of which consists of key and default value.
     */
    void putDefaultProperties(Map<String, JsonValue> properties);

    /**
     * Inserts default values at the end of the array.
     *
     * @param items the list of default values to insert.
     */
    void putDefaultItems(List<JsonValue> items);

    /**
     * Creates an evaluator which always evaluates the specified schema as
     * {@code false}.
     *
     * @param schema the schema against which the instance will be evaluated, cannot be {@code null}.
     * @return newly created evaluator. It must not be {@code null}.
     */
    Evaluator createAlwaysFalseEvaluator(Evaluator parent, JsonSchema schema);
}
