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

import javax.json.JsonException;
import javax.json.JsonValue;
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

    private Event currentEvent;

    /* As a JsonParser */

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

    /* As a AbstractJsonParser */

    /**
     * Returns the current event.
     *
     * @return the current event.
     */
    public final Event getCurrentEvent() {
        return currentEvent;
    }

    protected final void setCurrentEvent(Event event) {
        this.currentEvent = event;
    }

    protected abstract JsonLocation getLastCharLocation();

    /**
     * Checks if this parser is in array or object scope.
     *
     * @return {@code true} if this parser is in array or object scope.
     *         {@code false} if this parser is not in array or object scope.
     */
    protected abstract boolean isInCollection();

    /**
     * Creates an instance of {@link IllegalStateException} for the specified
     * method.
     *
     * @param method the name of the method.
     * @return the newly created exception.
     */
    protected final IllegalStateException newIllegalStateException(String method) {
        Map<String, Object> args = new HashMap<>();
        args.put("method", method);
        args.put("event", getCurrentEvent());
        String message = Message.PARSER_ILLEGAL_STATE.format(args);
        return new IllegalStateException(message);
    }

    protected final JsonParsingException newParsingException(Event... expected) {
        Map<String, Object> args = new HashMap<>();
        args.put("expected", Arrays.asList(expected));
        String message = Message.PARSER_UNEXPECTED_EOI.format(args);
        return new JsonParsingException(message, getLastCharLocation());
    }

    protected final JsonException newInternalError() {
        return new JsonException("Internal error");
    }

    /**
     * A base implementation of spliterators.
     *
     * @author leadpony
     *
     * @param <T> the type of value to be returned.
     */
    private abstract static class AbstractSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        protected AbstractSpliterator() {
            super(Long.MAX_VALUE, Spliterator.ORDERED);
        }

        @Override
        public Spliterator<T> trySplit() {
            // this spliterator cannot be split
            return null;
        }
    }

    /**
     * A spliterator to produce {@code JsonValue}.
     *
     * @author leadpony
     */
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

    /**
     * A spliterator to produce properties of a {@code JsonObject}.
     *
     * @author leadpony
     */
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

    /**
     * A spliterator to produce items of a {@code JsonArray}.
     *
     * @author leadpony
     */
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
