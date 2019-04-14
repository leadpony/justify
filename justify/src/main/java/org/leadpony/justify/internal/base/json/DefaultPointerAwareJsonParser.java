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

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

/**
 * A default implementation of {@link PointerAwareJsonParser}.
 *
 * @author leadpony
 */
public class DefaultPointerAwareJsonParser extends JsonParserDecorator implements PointerAwareJsonParser {

    private static final PointerBuilder INITIAL_BUILDER = new InitialPointerBuilder();
    private static final PointerBuilder ROOT_BUILDER = new RootPointerBuilder();

    private PointerBuilder pointerBuilder;
    private String cachedPointer;

    /**
     * Constructs this parser.
     *
     * @param realParser   the underlying JSON parser.
     * @param jsonProvider the JSON provider.
     */
    public DefaultPointerAwareJsonParser(JsonParser parser, JsonProvider jsonProvider) {
        super(jsonProvider);
        this.pointerBuilder = INITIAL_BUILDER;
        setCurrentParser(parser);
    }

    @Override
    public String getPointer() {
        if (cachedPointer != null) {
            return cachedPointer;
        }
        cachedPointer = pointerBuilder.toPointer();
        return cachedPointer;
    }

    @Override
    protected Event process(Event event) {
        pointerBuilder = pointerBuilder.withEvent(event, getCurrentParser());
        cachedPointer = null;
        return event;
    }

    @Override
    protected boolean isInCollection() {
        Event event = getCurrentEvent();
        if (event == Event.END_ARRAY || event == Event.END_OBJECT) {
            return true;
        }
        return pointerBuilder.isScoped();
    }

    private static interface PointerBuilder {

        default boolean isScoped() {
            return false;
        }

        PointerBuilder withEvent(Event event, JsonParser parser);

        default String toPointer() {
            StringBuilder builder = new StringBuilder();
            build(builder);
            return builder.toString();
        }

        default void build(StringBuilder builder) {
        }
    }

    private static class InitialPointerBuilder implements PointerBuilder {

        @Override
        public PointerBuilder withEvent(Event event, JsonParser parser) {
            switch (event) {
            case START_ARRAY:
                return new ArrayPointerBuilder(ROOT_BUILDER);
            case START_OBJECT:
                return new ObjectPointerBuilder(ROOT_BUILDER);
            case END_ARRAY:
            case END_OBJECT:
                throw new IllegalStateException();
            default:
                return ROOT_BUILDER;
            }
        }

        @Override
        public String toPointer() {
            return null;
        }
    }

    private static class RootPointerBuilder implements PointerBuilder {

        @Override
        public PointerBuilder withEvent(Event event, JsonParser parser) {
            throw new IllegalStateException();
        }
    }

    private static class ArrayPointerBuilder implements PointerBuilder {

        private final PointerBuilder parent;
        private int index = -1;

        ArrayPointerBuilder(PointerBuilder parent) {
            this.parent = parent;
        }

        @Override
        public boolean isScoped() {
            return true;
        }

        @Override
        public PointerBuilder withEvent(Event event, JsonParser parser) {
            switch (event) {
            case START_ARRAY:
                this.index++;
                return new ArrayPointerBuilder(this);
            case START_OBJECT:
                this.index++;
                return new ObjectPointerBuilder(this);
            case END_ARRAY:
                return this.parent;
            case END_OBJECT:
                throw new IllegalStateException();
            default:
                this.index++;
                return this;
            }
        }

        @Override
        public void build(StringBuilder builder) {
            parent.build(builder);
            if (index >= 0) {
                builder.append("/").append(index);
            }
        }
    }

    private static class ObjectPointerBuilder implements PointerBuilder {

        private final PointerBuilder parent;
        private String keyName;
        private Event lastEvent;

        ObjectPointerBuilder(PointerBuilder parent) {
            this.parent = parent;
        }

        @Override
        public boolean isScoped() {
            return true;
        }

        @Override
        public PointerBuilder withEvent(Event event, JsonParser parser) {
            this.lastEvent = event;
            switch (event) {
            case START_ARRAY:
                return new ArrayPointerBuilder(this);
            case START_OBJECT:
                return new ObjectPointerBuilder(this);
            case END_ARRAY:
                throw new IllegalStateException();
            case END_OBJECT:
                return this.parent;
            case KEY_NAME:
                this.keyName = parser.getString();
            default:
                return this;
            }
        }

        @Override
        public void build(StringBuilder builder) {
            parent.build(builder);
            if (keyName != null && lastEvent != Event.KEY_NAME) {
                builder.append('/');
                int lastIndex = 0;
                final int length = keyName.length();
                for (int i = 0; i < length; i++) {
                    char c = keyName.charAt(i);
                    if (c == '~') {
                        builder.append(keyName, lastIndex, i).append("~0");
                        lastIndex = i + 1;
                    } else if (c == '/') {
                        builder.append(keyName, lastIndex, i).append("~1");
                        lastIndex = i + 1;
                    }
                }
                if (lastIndex < length) {
                    builder.append(keyName, lastIndex, length);
                }
            }
        }
    }
}
