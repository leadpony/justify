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

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

/**
 * A parser interface for parsing a JSON value.
 *
 * @author leadpony
 */
public class JsonValueParser implements JsonParser {

    private final Deque<Scope> stack = new ArrayDeque<>();
    private Event event;

    public JsonValueParser(List<JsonValue> items) {
        stack.push(new ArrayScope(items));
    }

    public JsonValueParser(Map<String, JsonValue> properties) {
        stack.push(new ObjectScope(properties));
    }

    @Override
    public boolean hasNext() {
        if (stack.isEmpty()) {
            return false;
        }
        if (!stack.peek().hasNext()) {
            stack.pop();
            if (stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Event next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        this.event = currentScope().next();
        return this.event;
    }

    @Override
    public String getString() {
        Scope scope = currentScope();
        if (this.event == Event.KEY_NAME) {
            return scope.getKeyName();
        } else if (this.event == Event.VALUE_STRING) {
            return ((JsonString) scope.getValue()).getString();
        } else if (this.event == Event.VALUE_NUMBER) {
            return ((JsonNumber) scope.getValue()).toString();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isIntegralNumber() {
        if (this.event == Event.VALUE_NUMBER) {
            JsonNumber value = (JsonNumber) currentScope().getValue();
            return value.isIntegral();
        }
        throw new IllegalStateException();
    }

    @Override
    public int getInt() {
        if (this.event == Event.VALUE_NUMBER) {
            JsonNumber value = (JsonNumber) currentScope().getValue();
            return value.intValue();
        }
        throw new IllegalStateException();
    }

    @Override
    public long getLong() {
        if (this.event == Event.VALUE_NUMBER) {
            JsonNumber value = (JsonNumber) currentScope().getValue();
            return value.longValue();
        }
        throw new IllegalStateException();
    }

    @Override
    public BigDecimal getBigDecimal() {
        if (this.event == Event.VALUE_NUMBER) {
            JsonNumber value = (JsonNumber) currentScope().getValue();
            return value.bigDecimalValue();
        }
        throw new IllegalStateException();
    }

    @Override
    public JsonLocation getLocation() {
        return SimpleJsonLocation.UNKNOWN;
    }

    @Override
    public void close() {
    }

    private Scope currentScope() {
        return stack.peek();
    }

    private void enterScope(Scope scope) {
        stack.push(scope);
    }

    /**
     * A scope interface.
     *
     * @author leadpony
     */
    private static interface Scope {

        boolean hasNext();

        Event next();

        default String getKeyName() {
            throw new UnsupportedOperationException();
        }

        JsonValue getValue();
    }

    private class ArrayScope implements Scope {

        private final Iterator<JsonValue> iterator;
        private Event event;
        private JsonValue item;

        private ArrayScope(List<JsonValue> items) {
            this.iterator = items.iterator();
            this.event = Event.START_ARRAY;
        }

        @Override
        public boolean hasNext() {
            return event != Event.END_ARRAY;
        }

        @Override
        public Event next() {
            if (iterator.hasNext()) {
                item = iterator.next();
                event = ParserEvents.fromValue(item);
                if (event == Event.START_ARRAY) {
                    enterScope(new ArrayScope((JsonArray) item));
                } else if (event == Event.START_OBJECT) {
                    enterScope(new ObjectScope((JsonObject) item));
                }
            } else {
                item = null;
                event = Event.END_ARRAY;
            }
            return event;
        }

        @Override
        public JsonValue getValue() {
            switch (event) {
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_STRING:
            case VALUE_NUMBER:
                return item;
            default:
                throw new IllegalStateException();
            }
        }
    }

    /**
     * An object scope.
     *
     * @author leadpony
     */
    private class ObjectScope implements Scope {

        private final Iterator<Map.Entry<String, JsonValue>> iterator;
        private Event event;
        private Map.Entry<String, JsonValue> property;

        private ObjectScope(Map<String, JsonValue> properties) {
            this.iterator = properties.entrySet().iterator();
            this.event = Event.START_OBJECT;
        }

        @Override
        public boolean hasNext() {
            return event != Event.END_OBJECT;
        }

        @Override
        public Event next() {
            Event previous = this.event;
            if (previous == Event.KEY_NAME) {
                this.event = ParserEvents.fromValue(property.getValue());
                if (this.event == Event.START_ARRAY) {
                    enterScope(new ArrayScope((JsonArray) property.getValue()));
                } else if (this.event == Event.START_OBJECT) {
                    enterScope(new ObjectScope((JsonObject) property.getValue()));
                }
            } else {
                this.event = fetchProperty();
            }
            return this.event;
        }

        @Override
        public String getKeyName() {
            return property.getKey();
        }

        @Override
        public JsonValue getValue() {
            switch (event) {
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_STRING:
            case VALUE_NUMBER:
                return property.getValue();
            default:
                throw new IllegalStateException();
            }
        }

        private Event fetchProperty() {
            if (iterator.hasNext()) {
                this.property = iterator.next();
                return Event.KEY_NAME;
            } else {
                this.property = null;
                return Event.END_OBJECT;
            }
        }
    }
}
