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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

/**
 * A parser interface for parsing a JSON value.
 *
 * @author leadpony
 */
public class DefaultValueParser extends AbstractJsonParser {

    private Scope scope;
    private Scope nextScope;

    private static final Scope ROOT_SCOPE = new RootScope();

    public static JsonParser fillingWith(List<JsonValue> items, JsonProvider jsonProvider) {
        return new DefaultValueParser(jsonProvider, items);
    }

    public static JsonParser fillingWith(Map<String, JsonValue> properties, JsonProvider jsonProvider) {
        return new DefaultValueParser(jsonProvider, properties);
    }

    private DefaultValueParser(JsonProvider jsonProvider, List<JsonValue> items) {
        super(jsonProvider);
        nextScope = new ArrayScope(items, ROOT_SCOPE);
    }

    private DefaultValueParser(JsonProvider jsonProvider, Map<String, JsonValue> properties) {
        super(jsonProvider);
        nextScope = new ObjectScope(properties, ROOT_SCOPE);
    }

    @Override
    public boolean hasNext() {
        return nextScope != ROOT_SCOPE;
    }

    @Override
    public String getString() {
        switch (getCurrentEvent()) {
        case KEY_NAME:
            return scope.getKeyName();
        case VALUE_STRING:
            return ((JsonString) scope.getValue()).getString();
        case VALUE_NUMBER:
            return ((JsonNumber) scope.getValue()).toString();
        default:
            throw newIllegalStateException("getString");
        }
    }

    @Override
    public boolean isIntegralNumber() {
        if (getCurrentEvent() != Event.VALUE_NUMBER) {
            throw newIllegalStateException("isIntegralNumber");
        }
        JsonNumber value = (JsonNumber) scope.getValue();
        return value.isIntegral();
    }

    @Override
    public int getInt() {
        if (getCurrentEvent() != Event.VALUE_NUMBER) {
            throw newIllegalStateException("getInt");
        }
        JsonNumber value = (JsonNumber) scope.getValue();
        return value.intValue();
    }

    @Override
    public long getLong() {
        if (getCurrentEvent() != Event.VALUE_NUMBER) {
            throw newIllegalStateException("getLong");
        }
        JsonNumber value = (JsonNumber) scope.getValue();
        return value.longValue();
    }

    @Override
    public BigDecimal getBigDecimal() {
        if (getCurrentEvent() != Event.VALUE_NUMBER) {
            throw newIllegalStateException("getBigDecimal");
        }
        JsonNumber value = (JsonNumber) scope.getValue();
        return value.bigDecimalValue();
    }

    @Override
    public JsonLocation getLocation() {
        return SimpleJsonLocation.UNKNOWN;
    }

    @Override
    public void close() {
    }

    /* AbstractJsonParser */

    @Override
    protected Event fetchNextEvent() {
        if (nextScope == ROOT_SCOPE) {
            throw new NoSuchElementException();
        }
        scope = nextScope;
        Event event = scope.fetchNextEvent();
        nextScope = scope.nextScope();
        return event;
    }

    @Override
    protected boolean isInCollection() {
        return true;
    }

    @Override
    public JsonValue getJsonString() {
        return scope.getValue();
    }

    @Override
    public JsonValue getJsonNumber() {
        return scope.getValue();
    }

    /**
     * A scope interface.
     *
     * @author leadpony
     */
    private static interface Scope {

        default Event fetchNextEvent() {
            throw new UnsupportedOperationException();
        }

        Scope nextScope();

        default String getKeyName() {
            throw new UnsupportedOperationException();
        }

        default JsonValue getValue() {
            throw new UnsupportedOperationException();
        }
    }

    private static class RootScope implements Scope {

        @Override
        public Scope nextScope() {
            return this;
        }
    }

    /**
     * An array scope.
     *
     * @author leadpony
     */
    private static class ArrayScope implements Scope {

        private final Scope outerScope;
        private final Iterator<JsonValue> iterator;
        private Event event;
        private JsonValue item;

        private ArrayScope(List<JsonValue> items, Scope outerScope) {
            this.outerScope = outerScope;
            this.iterator = items.iterator();
        }

        @Override
        public Event fetchNextEvent() {
            if (iterator.hasNext()) {
                item = iterator.next();
                this.event = ParserEvents.fromValue(item);
            } else {
                this.event = Event.END_ARRAY;
            }
            return this.event;
        }

        @Override
        public Scope nextScope() {
            switch (event) {
            case START_ARRAY:
                return new ArrayScope((JsonArray) item, this);
            case START_OBJECT:
                return new ObjectScope((JsonObject) item, this);
            case END_ARRAY:
                return outerScope;
            case END_OBJECT:
                throw new IllegalStateException();
            default:
                return this;
            }
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
    private static class ObjectScope implements Scope {

        private final Scope outerScope;
        private final Iterator<Map.Entry<String, JsonValue>> iterator;
        private Event event;
        private Map.Entry<String, JsonValue> property;

        private ObjectScope(Map<String, JsonValue> properties, Scope outerScope) {
            this.outerScope = outerScope;
            this.iterator = properties.entrySet().iterator();
            this.event = Event.START_OBJECT;
        }

        @Override
        public Event fetchNextEvent() {
            Event previousEvent = this.event;
            if (previousEvent == Event.KEY_NAME) {
                this.event = ParserEvents.fromValue(property.getValue());
            } else {
                this.event = fetchProperty();
            }
            return this.event;
        }

        @Override
        public Scope nextScope() {
            switch (event) {
            case START_ARRAY:
                return new ArrayScope((JsonArray) property.getValue(), this);
            case START_OBJECT:
                return new ObjectScope((JsonObject) property.getValue(), this);
            case END_ARRAY:
                throw new IllegalStateException();
            case END_OBJECT:
                return outerScope;
            default:
                return this;
            }
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
                return Event.END_OBJECT;
            }
        }
    }
}
