/*
 * Copyright 2018 the Justify authors.
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

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
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
    private final JsonProvider jsonProvider;
    private Event currentEvent;
    private int depth;
    
    public JsonParserDecorator(JsonParser real, JsonProvider jsonProvider) {
        this.real = real;
        this.jsonProvider = jsonProvider;
    }
    
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
    
    private JsonArray buildArray() {
        JsonArrayBuilder builder = jsonProvider.createArrayBuilder();
        while (hasNext()) {
            Event event = next();
            if (event == Event.END_ARRAY) {
                return builder.build();
            }
            builder.add(getValue());
        }
        throw parsingException("EOF", "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL, SQUARECLOSE]");
    }

    private JsonObject buildObject() {
        JsonObjectBuilder builder = jsonProvider.createObjectBuilder();
        while (hasNext()) {
            Event event = next();
            if (event == Event.END_OBJECT) {
                return builder.build();
            }
            String name = getString();
            next();
            builder.add(name, getValue());
        }
        throw parsingException("EOF", "[STRING, CURLYCLOSE]");
    }
    
    private JsonParsingException parsingException(String actualToken, String expectedTokens) {
        JsonLocation location = SimpleJsonLocation.before(getLocation());
        String message = Message.get("parser.invalid.token")
                .withParameter("actual", actualToken)
                .withParameter("expected", expectedTokens)
                .withParameter("location", location)
                .toString();
        return new JsonParsingException(message, location);
    }
    
    private JsonException internalError() {
        return new JsonException(Message.getAsString("internal.error"));
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
            Objects.requireNonNull("action","action must not be null.");
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
            Objects.requireNonNull("action","action must not be null.");
            if (!hasNext()) {
                return false;
            }
            JsonParser.Event event = next();
            if (event == Event.END_OBJECT) {
                return false;
            } else if (event != Event.KEY_NAME) {
                // TODO:
                throw internalError(); 
            } else {
                String key = getString();
                if (!hasNext()) {
                    throw parsingException("EOF", "[CURLYOPEN, SQUAREOPEN, STRING, NUMBER, TRUE, FALSE, NULL]");
                }
                next();
                JsonValue value = getValue();
                action.accept(new AbstractMap.SimpleImmutableEntry<>(key, value));
                return true;
            }
        }
    }
    
    private class JsonValueSpliterator extends AbstractSpliterator<JsonValue> {

        @Override
        public boolean tryAdvance(Consumer<? super JsonValue> action) {
            Objects.requireNonNull("action","action must not be null.");
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
