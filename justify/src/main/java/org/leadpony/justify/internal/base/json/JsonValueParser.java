/*
 * Copyright 2019 the Joy Authors.
 * Copyright 2019 the Justify Authors.
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
 *
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
import javax.json.stream.JsonLocation;

/**
 * A JSON parser type which parses in-memory JSON structures.
 *
 * @author leadpony
 */
public class JsonValueParser extends AbstractJsonParser {

    private static final Scope GLOBAL_SCOPE = new GlobalScope();
    private Scope scope;

    public JsonValueParser(JsonArray value) {
        this.scope = new ArrayScope(value);
    }

    public JsonValueParser(List<JsonValue> value) {
        this.scope = new ArrayScope(value, GLOBAL_SCOPE);
    }

    public JsonValueParser(JsonObject value) {
        this.scope = new ObjectScope(value);
    }

    public JsonValueParser(Map<String, JsonValue> value) {
        this.scope = new ObjectScope(value, GLOBAL_SCOPE);
    }

    @Override
    public boolean hasNext() {
        return scope != GLOBAL_SCOPE;
    }

    @Override
    public Event next() {
        Event event = scope.getEvent(this);
        setCurrentEvent(event);
        return event;
    }

    @Override
    public String getString() {
        switch (getCurrentEvent()) {
        case KEY_NAME:
            return scope.getKey();
        case VALUE_STRING:
            JsonString string = (JsonString) scope.getValue();
            return string.getString();
        case VALUE_NUMBER:
            return scope.getValue().toString();
        default:
            throw newIllegalStateException("getString");
        }
    }

    @Override
    public boolean isIntegralNumber() {
        if (getCurrentEvent() != Event.VALUE_NUMBER) {
            throw newIllegalStateException("isIntegralNumber");
        }
        JsonNumber number = (JsonNumber) scope.getValue();
        return number.isIntegral();
    }

    @Override
    public int getInt() {
        if (getCurrentEvent() != Event.VALUE_NUMBER) {
            throw newIllegalStateException("getInt");
        }
        JsonNumber number = (JsonNumber) scope.getValue();
        return number.intValue();
    }

    @Override
    public long getLong() {
        if (getCurrentEvent() != Event.VALUE_NUMBER) {
            throw newIllegalStateException("getLong");
        }
        JsonNumber number = (JsonNumber) scope.getValue();
        return number.longValue();
    }

    @Override
    public BigDecimal getBigDecimal() {
        if (getCurrentEvent() != Event.VALUE_NUMBER) {
            throw newIllegalStateException("getBigDecimal");
        }
        JsonNumber number = (JsonNumber) scope.getValue();
        return number.bigDecimalValue();
    }

    @Override
    public JsonLocation getLocation() {
        return SimpleJsonLocation.UNKNOWN;
    }

    @Override
    public JsonObject getObject() {
        if (getCurrentEvent() != Event.START_OBJECT) {
            throw newIllegalStateException("getObject");
        }
        return (JsonObject) scope.getValue();
    }

    @Override
    public JsonValue getValue() {
        Event event = getCurrentEvent();
        if (event == Event.END_ARRAY
                || event == Event.END_OBJECT
                || event == null) {
            throw newIllegalStateException("getValue");
        }
        return scope.getValue();
    }

    @Override
    public JsonArray getArray() {
        if (getCurrentEvent() != Event.START_ARRAY) {
            throw newIllegalStateException("getArray");
        }
        return (JsonArray) scope.getValue();
    }

    @Override
    public void close() {
        // Does nothing
    }

    /* As a AbstractJsonParser */

    @Override
    protected JsonLocation getLastCharLocation() {
        return SimpleJsonLocation.UNKNOWN;
    }

    @Override
    protected boolean isInCollection() {
        return getCurrentEvent() != null;
    }

    private final void setScope(Scope scope) {
        this.scope = scope;
    }

    private static Event getEventStarting(JsonValue value) {
        switch (value.getValueType()) {
        case ARRAY:
            return Event.START_ARRAY;
        case OBJECT:
            return Event.START_OBJECT;
        case STRING:
            return Event.VALUE_STRING;
        case NUMBER:
            return Event.VALUE_NUMBER;
        case TRUE:
            return Event.VALUE_TRUE;
        case FALSE:
            return Event.VALUE_FALSE;
        case NULL:
            return Event.VALUE_NULL;
        default:
            throw new IllegalStateException();
        }
    }

    interface Scope {

        Event getEvent(JsonValueParser parser);

        default String getKey() {
            throw new UnsupportedOperationException();
        }

        JsonValue getValue();
    }

    static class GlobalScope implements Scope {

        @Override
        public Event getEvent(JsonValueParser parser) {
            throw new NoSuchElementException();
        }

        @Override
        public JsonValue getValue() {
            throw new IllegalStateException();
        }
    }

    abstract static  class CollectionScope implements Scope {

        private final Scope outerScope;

        CollectionScope(Scope outerScope) {
            this.outerScope = outerScope;
        }

        final Scope getOuterScope() {
            return outerScope;
        }
    }

    static class ArrayScope extends CollectionScope {

        private final Iterator<JsonValue> iterator;
        private ArrayState state;
        private JsonValue currentValue;

        ArrayScope(JsonArray array) {
            super(GLOBAL_SCOPE);
            this.iterator = array.iterator();
            this.state = ArrayState.START;
            this.currentValue = array;
        }

        ArrayScope(List<JsonValue> items, Scope outerScope) {
            super(outerScope);
            this.iterator = items.iterator();
            this.state = ArrayState.ITEM;
            this.currentValue = null;
        }

        @Override
        public Event getEvent(JsonValueParser parser) {
            return state.process(parser, this);
        }

        @Override
        public JsonValue getValue() {
            return currentValue;
        }

        final void setState(ArrayState state) {
            this.state = state;
        }

        final void setValue(JsonValue value) {
            this.currentValue = value;
        }

        enum ArrayState {
            START() {
                @Override
                public Event process(JsonValueParser parser, ArrayScope scope) {
                    scope.setState(ITEM);
                    return Event.START_ARRAY;
                }
            },

            ITEM() {
                @Override
                public Event process(JsonValueParser parser, ArrayScope scope) {
                    Event event;
                    Iterator<JsonValue> iterator = scope.iterator;
                    if (iterator.hasNext()) {
                        JsonValue value = iterator.next();
                        event = getEventStarting(value);
                        switch (event) {
                        case START_ARRAY:
                            parser.setScope(new ArrayScope((JsonArray) value, scope));
                            break;
                        case START_OBJECT:
                            parser.setScope(new ObjectScope((JsonObject) value, scope));
                            break;
                        default:
                            break;
                        }
                        scope.setValue(value);
                    } else {
                        event = Event.END_ARRAY;
                        scope.setValue(null);
                        parser.setScope(scope.getOuterScope());
                    }
                    return event;
                }
            };

            abstract Event process(JsonValueParser parser, ArrayScope scope);
        }
    }

    static class ObjectScope extends CollectionScope {

        private final Iterator<Map.Entry<String, JsonValue>> iterator;
        private ObjectState state;
        private String keyName;
        private JsonValue currentValue;

        ObjectScope(JsonObject object) {
            super(GLOBAL_SCOPE);
            this.iterator = object.entrySet().iterator();
            this.state = ObjectState.START;
            this.currentValue = object;
        }

        ObjectScope(Map<String, JsonValue> properties, Scope outerScope) {
            super(outerScope);
            this.iterator = properties.entrySet().iterator();
            this.state = ObjectState.KEY;
            this.currentValue = null;
        }

        @Override
        public Event getEvent(JsonValueParser parser) {
            return state.process(parser, this);
        }

        @Override
        public final String getKey() {
            return keyName;
        }

        @Override
        public final JsonValue getValue() {
            return currentValue;
        }

        final boolean fetchProperty() {
            if (iterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = iterator.next();
                this.keyName = entry.getKey();
                this.currentValue = entry.getValue();
                return true;
            } else {
                this.keyName = null;
                this.currentValue = null;
                return false;
            }
        }

        final void setState(ObjectState state) {
            this.state = state;
        }

        enum ObjectState {
            START() {
                @Override
                public Event process(JsonValueParser parser, ObjectScope scope) {
                    scope.setState(KEY);
                    return Event.START_OBJECT;
                }
            },

            KEY() {
                @Override
                public Event process(JsonValueParser parser, ObjectScope scope) {
                    Event event;
                    if (scope.fetchProperty()) {
                        event = Event.KEY_NAME;
                        scope.setState(VALUE);
                    } else {
                        event = Event.END_OBJECT;
                        parser.setScope(scope.getOuterScope());
                    }
                    return event;
                }
            },

            VALUE() {
                @Override
                public Event process(JsonValueParser parser, ObjectScope scope) {
                    JsonValue value = scope.getValue();
                    Event event = getEventStarting(value);
                    switch (event) {
                    case START_ARRAY:
                        parser.setScope(new ArrayScope((JsonArray) value, scope));
                        break;
                    case START_OBJECT:
                        parser.setScope(new ObjectScope((JsonObject) value, scope));
                        break;
                    default:
                        break;
                    }
                    scope.setState(KEY);
                    return event;
                }
            };

            abstract Event process(JsonValueParser parser, ObjectScope scope);
        }
    }
}
