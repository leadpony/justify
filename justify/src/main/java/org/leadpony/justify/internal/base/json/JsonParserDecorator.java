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

import java.math.BigDecimal;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

/**
 * A decorator class of {@link JsonParser}.
 *
 * @author leadpony
 */
public abstract class JsonParserDecorator extends AbstractStreamJsonParser {

    private JsonParser parser;

    /**
     * Constructs this object.
     *
     * @param jsonProvider the JSON provicer.
     */
    public JsonParserDecorator(JsonProvider jsonProvider) {
        super(jsonProvider);
    }

    protected final JsonParser getCurrentParser() {
        return parser;
    }

    /**
     * Assigns the underlying JSON parser.
     *
     * @param parser the underlying JSON parser, cannot be {@code null}.
     */
    protected void setCurrentParser(JsonParser parser) {
        this.parser = parser;
    }

    /* JsonParser */

    @Override
    public void close() {
        parser.close();
    }

    @Override
    public BigDecimal getBigDecimal() {
        return parser.getBigDecimal();
    }

    @Override
    public int getInt() {
        return parser.getInt();
    }

    @Override
    public JsonLocation getLocation() {
        return parser.getLocation();
    }

    @Override
    public long getLong() {
        return parser.getLong();
    }

    @Override
    public String getString() {
        return parser.getString();
    }

    @Override
    public boolean hasNext() {
        return parser.hasNext();
    }

    @Override
    public boolean isIntegralNumber() {
        return parser.isIntegralNumber();
    }

    /* AbstractJsonParser */

    @Override
    protected Event fetchNextEvent() {
        return parser.next();
    }

    @Override
    public JsonValue getJsonString() {
        return parser.getValue();
    }

    @Override
    public JsonValue getJsonNumber() {
        return parser.getValue();
    }
}
