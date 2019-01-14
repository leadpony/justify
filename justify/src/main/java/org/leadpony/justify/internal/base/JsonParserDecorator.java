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

package org.leadpony.justify.internal.base;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.math.BigDecimal;
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
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

/**
 * Decorator class of {@link JsonParser}.
 * 
 * @author leadpony
 */
public class JsonParserDecorator implements JsonParser {
    
    private final JsonParser real;
    private final JsonBuilderFactory builderFactory;
    private Event currentEvent;
    private int depth;
    
    /**
     * Constructs this object.
     * 
     * @param real the underlying real JSON parser.
     * @param builderFactory the factory of builders to build JSON arrays and objects.
     */
    public JsonParserDecorator(JsonParser real, JsonBuilderFactory builderFactory) {
        requireNonNull(real, "real");
        requireNonNull(builderFactory, "builderFactory");
        this.real = real;
        this.builderFactory = builderFactory;
    }
    
    /**
     * Returns the underlying real JSON parser.
     * 
     * @return the underlying JSON parser, never be {@code null}.
     */
    public JsonParser realParser() {
        return real;
    }
    
    @Override
    public void close() {
        real.close();
    }

    @Override
    public BigDecimal getBigDecimal() {
        return real.getBigDecimal();
    }

    @Override
    public int getInt() {
        return real.getInt();
    }

    @Override
    public JsonLocation getLocation() {
        return real.getLocation();
    }

    @Override
    public long getLong() {
        return real.getLong();
    }

    @Override
    public String getString() {
        return real.getString();
    }

    @Override
    public boolean hasNext() {
        return real.hasNext();
    }

    @Override
    public boolean isIntegralNumber() {
        return real.isIntegralNumber();
    }

    @Override
    public Event next() {
        Event event = real.next();
        if (event == Event.START_ARRAY || event == Event.START_OBJECT) {
            ++depth;
        } else if (event == Event.END_ARRAY || event == Event.END_OBJECT) {
            --depth;
        }
        currentEvent = event;
        return event;
    }
    
    @Override
    public JsonObject getObject() {
        if (this.currentEvent != Event.START_OBJECT) {
            // This throws IllegalStateException.
            return real.getObject();
        }
        return buildObject();
    }

    @Override
    public JsonArray getArray() {
        if (this.currentEvent != Event.START_ARRAY) {
            // This throws IllegalStateException.
            return real.getArray();
        }
        return buildArray();
    }

    @Override
    public JsonValue getValue() {
        switch (this.currentEvent) {
        case START_ARRAY:
            return buildArray();
        case START_OBJECT:
            return buildObject();
        default:
            // This may throw IllegalStateException.
            return real.getValue();
        }
    }

    @Override
    public void skipArray() {
        if (currentEvent == Event.START_ARRAY) {
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
    }

    @Override
    public void skipObject() {
        if (currentEvent == Event.START_OBJECT) {
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
    }
    
    @Override
    public Stream<JsonValue> getArrayStream() {
        if (currentEvent != Event.START_ARRAY) {
            return JsonParser.super.getArrayStream();
        }
        return StreamSupport.stream(new JsonArraySpliterator(), false);
    }
    
    @Override
    public Stream<Map.Entry<String, JsonValue>> getObjectStream() {
        if (currentEvent != Event.START_OBJECT) {
            return JsonParser.super.getObjectStream();
        }
        return StreamSupport.stream(new JsonObjectSpliterator(), false);
    }
    
    @Override
    public Stream<JsonValue> getValueStream() {
        if (depth > 0) {
            return JsonParser.super.getValueStream();
        }
        return StreamSupport.stream(new JsonValueSpliterator(), false);
    }
    
    /**
     * Returns the location of the last char.
     * 
     * @return the location of the last char.
     */
    public JsonLocation getLastCharLocation() {
        return SimpleJsonLocation.before(getLocation());
    }
    
    private JsonArray buildArray() {
        JsonArrayBuilder builder = builderFactory.createArrayBuilder();
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
        JsonObjectBuilder builder = builderFactory.createObjectBuilder();
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
    
    private JsonParsingException newParsingException(Event... expectedEvents) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("expected", Arrays.asList(expectedEvents));
        String message = Message.get("parser.unexpected.eoi").format(parameters);
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
            requireNonNull("action","action");
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
