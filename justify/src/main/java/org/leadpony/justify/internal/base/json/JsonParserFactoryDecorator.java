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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;

/**
 * Decorator class of {@link JsonParserFactory}.
 *
 * @author leadpony
 */
public class JsonParserFactoryDecorator implements JsonParserFactory {

    private final JsonParserFactory real;

    /**
     * Constructs this object.
     *
     * @param real the underlying JSON parser factory to be decorated.
     */
    public JsonParserFactoryDecorator(JsonParserFactory real) {
        requireNonNull(real, "real");
        this.real = real;
    }

    /**
     * Returns the underlying real JSON parser factory.
     *
     * @return the underlying JSON parser factory, never be {@code null}.
     */
    public JsonParserFactory realFactory() {
        return real;
    }

    @Override
    public JsonParser createParser(Reader reader) {
        return real.createParser(reader);
    }

    @Override
    public JsonParser createParser(InputStream in) {
        return real.createParser(in);
    }

    @Override
    public JsonParser createParser(InputStream in, Charset charset) {
        return real.createParser(in, charset);
    }

    /**
     * {@inheritDoc}
     *
     * Note that this method creates custom parsers instead of underlying parsers.
     */
    @Override
    public JsonParser createParser(JsonObject obj) {
        requireNonNull(obj, "obj");
        return new JsonValueParser(obj);
    }

    /**
     * {@inheritDoc}
     *
     * Note that this method creates custom parsers instead of underlying parsers.
     */
    @Override
    public JsonParser createParser(JsonArray array) {
        requireNonNull(array, "array");
        return new JsonValueParser(array);
    }

    @Override
    public Map<String, ?> getConfigInUse() {
        return real.getConfigInUse();
    }
}
