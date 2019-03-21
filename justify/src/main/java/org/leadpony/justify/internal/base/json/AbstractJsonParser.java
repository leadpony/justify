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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

import org.leadpony.justify.internal.base.Message;

/**
 * A skeletal implementation of {@link JsonParser}.
 *
 * @author leadpony
 */
abstract class AbstractJsonParser implements JsonParser {

    private final JsonProvider jsonProvider;
    private final JsonBuilderFactory builderFactory;
    private Event currentEvent;

    /**
     * Constructs this parser.
     *
     * @param jsonProvider the JSON provider.
     */
    protected AbstractJsonParser(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.builderFactory = jsonProvider.createBuilderFactory(null);
    }

    /* JsonParser */

    @Override
    public final Event next() {
        Event event = fetchNextEvent();
        event = process(event);
        this.currentEvent = event;
        postprocess();
        return event;
    }

    @Override
    public JsonArray getArray() {
        if (this.currentEvent != Event.START_ARRAY) {
            throw newIllegalStateException("getArray");
        }
        return buildArray();
    }

    @Override
    public JsonObject getObject() {
        if (this.currentEvent != Event.START_OBJECT) {
            throw newIllegalStateException("getObject");
        }
        return buildObject();
    }

    @Override
    public JsonValue getValue() {
        switch (this.currentEvent) {
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

    @Override
    public void skipArray() {
        if (this.currentEvent != Event.START_ARRAY) {
            return;
        }
        int depth = 1;
        while (hasNext()) {
            Event event = next();
            if (event == Event.END_ARRAY) {
                if (--depth == 0) {
                    break;
                }
            } else if (event == Event.START_ARRAY) {
                ++depth;
            }
        }
    }

    @Override
    public void skipObject() {
        if (this.currentEvent != Event.START_OBJECT) {
            return;
        }
        int depth = 1;
        while (hasNext()) {
            Event event = next();
            if (event == Event.END_OBJECT) {
                if (--depth == 0) {
                    break;
                }
            } else if (event == Event.START_OBJECT) {
                ++depth;
            }
        }
    }

    @Override
    public Stream<JsonValue> getArrayStream() {
        if (this.currentEvent != Event.START_ARRAY) {
            throw newIllegalStateException("getArrayStream");
        }
        return StreamSupport.stream(new JsonArraySpliterator(), false);
    }

    @Override
    public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
        if (this.currentEvent != Event.START_OBJECT) {
            throw newIllegalStateException("getObjectStream");
        }
        return StreamSupport.stream(new JsonObjectSpliterator(), false);
    }

    @Override
    public Stream<JsonValue> getValueStream() {
        if (isInCollection()) {
            throw newIllegalStateException("getValueStream");
        }
        return StreamSupport.stream(new JsonValueSpliterator(), false);
    }

    /* AbstractJsonParser */

    public final JsonProvider getJsonProvider() {
        return jsonProvider;
    }

    public final JsonBuilderFactory getJsonBuilderFactory() {
        return builderFactory;
    }

    /**
     * Returns the location of the last char.
     *
     * @return the location of the last char.
     */
    public JsonLocation getLastCharLocation() {
        return SimpleJsonLocation.before(getLocation());
    }

    /**
     * Return the current parser event.
     *
     * @return the current parser event.
     */
    public final Event getCurrentEvent() {
        return currentEvent;
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

    /**
     * Checks if this parser is in array or object scope.
     *
     * @return {@code true} if this parser is in array or object scope.
     *         {@code false} if this parser is not in array or object scope.
     */
    protected abstract boolean isInCollection();

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

    /**
     * Creates an instance of {@link IllegalStateException} for the specified
     * method.
     *
     * @param method the name of the method.
     * @return the newly created exception.
     */
    protected IllegalStateException newIllegalStateException(String method) {
        Map<String, Object> args = new HashMap<>();
        args.put("method", method);
        args.put("event", getCurrentEvent());
        String message = Message.PARSER_ILLEGAL_STATE.format(args);
        return new IllegalStateException(message);
    }

    private JsonParsingException newParsingException(Event... expected) {
        Map<String, Object> args = new HashMap<>();
        args.put("expected", Arrays.asList(expected));
        String message = Message.PARSER_UNEXPECTED_EOI.format(args);
        return new JsonParsingException(message, getLastCharLocation());
    }

    private JsonException newInternalError() {
        return new JsonException("Internal error");
    }

    private static abstract class AbstractSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        protected AbstractSpliterator() {
            super(Long.MAX_VALUE, Spliterator.ORDERED);
        }

        @Override
        public Spliterator<T> trySplit() {
            // this spliterator cannot be split
            return null;
        }
    }

    private class JsonArraySpliterator extends AbstractSpliterator<JsonValue> {

        @Override
        public boolean tryAdvance(Consumer<? super JsonValue> action) {
            requireNonNull(action, "action");
            if (hasNext() && next() != Event.END_ARRAY) {
                action.accept(getValue());
                return true;
            } else {
                return false;
            }
        }
    }

    private class JsonObjectSpliterator extends AbstractSpliterator<Map.Entry<String, JsonValue>> {

        @Override
        public boolean tryAdvance(Consumer<? super Map.Entry<String, JsonValue>> action) {
            requireNonNull(action, "action");
            if (!hasNext()) {
                return false;
            }
            JsonParser.Event event = next();
            if (event == Event.END_OBJECT) {
                return false;
            } else if (event == Event.KEY_NAME) {
                String key = getString();
                if (!hasNext()) {
                    throw newParsingException(
                            Event.START_OBJECT,
                            Event.START_ARRAY,
                            Event.VALUE_STRING,
                            Event.VALUE_NUMBER,
                            Event.VALUE_TRUE,
                            Event.VALUE_FALSE,
                            Event.VALUE_NULL);
                }
                next();
                JsonValue value = getValue();
                action.accept(new AbstractMap.SimpleImmutableEntry<>(key, value));
                return true;
            } else {
                // This will never happen.
                throw newInternalError();
            }
        }
    }

    private class JsonValueSpliterator extends AbstractSpliterator<JsonValue> {

        @Override
        public boolean tryAdvance(Consumer<? super JsonValue> action) {
            requireNonNull("action", "action");
            if (hasNext()) {
                next();
                action.accept(getValue());
                return true;
            } else {
                return false;
            }
        }
    }
}
