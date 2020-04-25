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
package org.leadpony.justify.internal.base.json;

import java.util.Collections;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonParserFactory;

/**
 * A JSON service type which wraps {@code JsonProvider}.
 *
 * @author leadpony
 */
public class JsonService {

    private final JsonProvider jsonProvider;
    private final JsonBuilderFactory builderFactory;
    private final JsonParserFactory parserFactory;

    public JsonService(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.builderFactory = jsonProvider.createBuilderFactory(Collections.emptyMap());
        this.parserFactory = jsonProvider.createParserFactory(Collections.emptyMap());
    }

    /**
     * Returns the instance of JSON provider.
     *
     * @return the instance of JSON provider.
     */
    public final JsonProvider getJsonProvider() {
        return jsonProvider;
    }

    /**
     * Returns the default instance of {@code JsonBuilderFactory}.
     *
     * @return the default instance of {@code JsonBuilderFactory}.
     */
    public final JsonBuilderFactory getJsonBuilderFactory() {
        return builderFactory;
    }

    /**
     * Returns the default instance of {@code JsonParserFactory}.
     *
     * @return the default instance of {@code JsonParserFactory}.
     */
    public final JsonParserFactory getJsonParserFactory() {
        return parserFactory;
    }

    /**
     * Creates a new instance of {@code JsonArrayBuilder}.
     *
     * @return newly created instance of {@code JsonArrayBuilder}.
     */
    public final JsonArrayBuilder createArrayBuilder() {
        return getJsonBuilderFactory().createArrayBuilder();
    }

    /**
     * Creates a new instance of {@code JsonObjectBuilder}.
     *
     * @return newly created instance of {@code JsonObjectBuilder}.
     */
    public final JsonObjectBuilder createObjectBuilder() {
        return getJsonBuilderFactory().createObjectBuilder();
    }
}
