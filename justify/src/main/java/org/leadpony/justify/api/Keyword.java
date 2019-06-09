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

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

/**
 * A schema keyword which composes a JSON schema.
 *
 * @author leadpony
 */
public interface Keyword {

    /**
     * Returns the name of this keyword.
     *
     * @return the name of this keyword, never be {@code null}.
     */
    String name();

    /**
     * Returns the value of this keyword as an instance of {@code JsonValue}.
     *
     * <p>
     * Alternative and more convenient method to retrieve keyword values is
     * {@link JsonSchema#getKeywordValue(String)}.
     * </p>
     *
     * @param jsonProvider the instance of {@code JsonProvider}, which cannot be
     *                     {@code null}. The instance can be obtained via
     *                     {@link JsonValidationService#getJsonProvider()}.
     * @return the value of this keyword, cannot be {@code null}.
     */
    JsonValue getValueAsJson(JsonProvider jsonProvider);
}
