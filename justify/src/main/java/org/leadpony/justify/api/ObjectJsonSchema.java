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

import javax.json.JsonValue.ValueType;

/**
 * A JSON schema type represented by a JSON object.
 *
 * @author leadpony
 */
public interface ObjectJsonSchema extends JsonSchema, Map<String, Keyword> {

    /**
     * {@inheritDoc}
     *
     * @return always {@code false}.
     */
    @Override
    default boolean isBoolean() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default ObjectJsonSchema asObjectJsonSchema() {
        return this;
    }

    /**
     * Returns {@code ValueType.OBJECT} as a value type of JSON.
     *
     * @return {@code ValueType.OBJECT}.
     */
    @Override
    default ValueType getJsonValueType() {
        return ValueType.OBJECT;
    }
}
