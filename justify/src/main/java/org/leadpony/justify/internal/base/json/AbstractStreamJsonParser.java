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

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

/**
 * A skeletal implementation of streaming {@link JsonParser}.
 *
 * @author leadpony
 */
abstract class AbstractStreamJsonParser extends AbstractJsonParser {

    private final JsonProvider jsonProvider;
    private final JsonBuilderFactory builderFactory;

    /**
     * Constructs this parser.
     *
     * @param jsonProvider the JSON provider.
     */
    protected AbstractStreamJsonParser(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.builderFactory = jsonProvider.createBuilderFactory(null);
    }

    /* As a JsonParser */

    @Override
    public final Event next() {
        Event event = fetchNextEvent();
        event = process(event);
        setCurrentEvent(event);
        postprocess();
        return event;
    }

    @Override
    public JsonArray getArray() {
        if (getCurrentEvent() != Event.START_ARRAY) {
            throw newIllegalStateException("getArray");
        }
        return buildArray();
    }

    @Override
    public JsonObject getObject() {
        if (getCurrentEvent() != Event.START_OBJECT) {
            throw newIllegalStateException("getObject");
        }
        return buildObject();
    }

    @Override
    public JsonValue getValue() {
        final Event event = getCurrentEvent();
        if (event == null) {
            throw newIllegalStateException("getValue");
        }
        switch (event) {
        case START_ARRAY:
            return buildArray();
        case START_OBJECT:
            return buildObject();
        case VALUE_TRUE:
            return JsonValue.TRUE;
        case VALUE_FALSE:
            return JsonValue.FALSE;
        case VALUE_NULL:
            return JsonValue.NULL;
        case KEY_NAME:
        case VALUE_STRING:
            return getJsonString();
        case VALUE_NUMBER:
            return getJsonNumber();
        case END_ARRAY:
        case END_OBJECT:
        default:
            throw newIllegalStateException("getValue");
        }
    }

    /* As a AbstractJsonParser */

    /**
     * Returns the location of the last char.
     *
     * @return the location of the last char.
     */
    @Override
    protected JsonLocation getLastCharLocation() {
        return SimpleJsonLocation.before(getLocation());
    }

    /* As a AbstractStreamJsonParser */

    public final JsonProvider getJsonProvider() {
        return jsonProvider;
    }

    public final JsonBuilderFactory getJsonBuilderFactory() {
        return builderFactory;
    }

    public JsonValue getJsonString() {
        return jsonProvider.createValue(getString());
    }

    public JsonValue getJsonNumber() {
        if (isIntegralNumber()) {
            return jsonProvider.createValue(getLong());
        } else {
            return jsonProvider.createValue(getBigDecimal());
        }
    }

    protected abstract Event fetchNextEvent();

    /**
     * Processes the currrent parser event.
     *
     * @param event the current parser event.
     * @return the event to be delivered to the client.
     */
    protected Event process(Event event) {
        return event;
    }

    protected void postprocess() {
    }

    private JsonArray buildArray() {
        JsonArrayBuilder builder = getJsonBuilderFactory().createArrayBuilder();
        while (hasNext()) {
            Event event = next();
            if (event == Event.END_ARRAY) {
                return builder.build();
            }
            builder.add(getValue());
        }
        throw newParsingException(
                Event.START_OBJECT,
                Event.START_ARRAY,
                Event.VALUE_STRING,
                Event.VALUE_NUMBER,
                Event.VALUE_TRUE,
                Event.VALUE_FALSE,
                Event.VALUE_NULL,
                Event.END_ARRAY);
    }

    private JsonObject buildObject() {
        JsonObjectBuilder builder = getJsonBuilderFactory().createObjectBuilder();
        while (hasNext()) {
            Event event = next();
            if (event == Event.END_OBJECT) {
                return builder.build();
            }
            String name = getString();
            next();
            builder.add(name, getValue());
        }
        throw newParsingException(Event.KEY_NAME, Event.END_OBJECT);
    }
}
