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

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * Event-driven builder of JSON instance.
 *
 * @author leadpony
 */
public class JsonInstanceBuilder {

    private final RootVisitor rootVisitor = new RootVisitor();
    private final JsonBuilderFactory builderFactory;
    private Visitor currentVisitor = rootVisitor;

    public JsonInstanceBuilder(JsonBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    /**
     * Appends a new parser event.
     *
     * @param event  the event to append.
     * @param parser the JSON parser.
     * @return {@code true} if this builder should be continued, or {@code false} if
     *         this builder is completed.
     */
    public boolean append(Event event, JsonParser parser) {
        this.currentVisitor = this.currentVisitor.visit(event, parser);
        return (this.currentVisitor != this.rootVisitor);
    }

    /**
     * Builds a JSON value.
     *
     * @return the built JSON value.
     */
    public JsonValue build() {
        return rootVisitor.rootValue();
    }

    private static JsonValue getLiteral(Event event, JsonParser parser) {
        switch (event) {
        case VALUE_TRUE:
            return JsonValue.TRUE;
        case VALUE_FALSE:
            return JsonValue.FALSE;
        case VALUE_NULL:
            return JsonValue.NULL;
        case VALUE_STRING:
            return parser.getValue();
        case VALUE_NUMBER:
            return new BigDecimalJsonNumber(parser.getBigDecimal());
        default:
            assert false;
            return null;
        }
    }

    /**
     * A visitor of JSON values.
     *
     * @author leadpony
     *
     */
    private interface Visitor {

        Visitor visit(Event event, JsonParser parser);

        void append(JsonValue value);
    }

    /**
     * A visitor of root values.
     *
     * @author leadpony
     */
    private class RootVisitor implements Visitor {

        private JsonValue value;

        @Override
        public Visitor visit(Event event, JsonParser parser) {
            switch (event) {
            case START_ARRAY:
                return new ArrayVisitor(this);
            case START_OBJECT:
                return new ObjectVisitor(this);
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_NUMBER:
            case VALUE_STRING:
                append(getLiteral(event, parser));
                break;
            default:
                assert false;
                break;
            }
            return this;
        }

        @Override
        public void append(JsonValue value) {
            this.value = value;
        }

        public JsonValue rootValue() {
            return value;
        }
    }

    /**
     * A visitor of JSON arrays.
     *
     * @author leadpony
     */
    private class ArrayVisitor implements Visitor {

        private final Visitor parent;
        private final JsonArrayBuilder builder;

        ArrayVisitor(Visitor parent) {
            this.parent = parent;
            this.builder = builderFactory.createArrayBuilder();
        }

        @Override
        public Visitor visit(Event event, JsonParser parser) {
            switch (event) {
            case START_ARRAY:
                return new ArrayVisitor(this);
            case START_OBJECT:
                return new ObjectVisitor(this);
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_NUMBER:
            case VALUE_STRING:
                append(getLiteral(event, parser));
                break;
            case END_ARRAY:
                parent.append(builder.build());
                return parent;
            default:
                assert false;
            }
            return this;
        }

        @Override
        public void append(JsonValue value) {
            builder.add(value);
        }
    }

    /**
     * A visitor of JSON objects.
     *
     * @author leadpony
     */
    private class ObjectVisitor implements Visitor {

        private final Visitor parent;
        private final JsonObjectBuilder builder;
        private String propertyName;

        ObjectVisitor(Visitor parent) {
            this.parent = parent;
            this.builder = builderFactory.createObjectBuilder();
        }

        @Override
        public Visitor visit(Event event, JsonParser parser) {
            switch (event) {
            case START_ARRAY:
                return new ArrayVisitor(this);
            case START_OBJECT:
                return new ObjectVisitor(this);
            case KEY_NAME:
                this.propertyName = parser.getString();
                break;
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_NUMBER:
            case VALUE_STRING:
                builder.add(this.propertyName, getLiteral(event, parser));
                this.propertyName = null;
                break;
            case END_OBJECT:
                parent.append(builder.build());
                return parent;
            default:
                assert false;
            }
            return this;
        }

        @Override
        public void append(JsonValue value) {
            assert propertyName != null;
            builder.add(propertyName, value);
        }
    }
}
