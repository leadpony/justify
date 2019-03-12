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

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * A builder type for building JSON pointers.
 *
 * @author leadpony
 */
public abstract class JsonPointerBuilder {

    private static final JsonPointerBuilder INTIAL_BUILDER = new InitialJsonPointerBuilder();
    private static final JsonPointerBuilder ROOT_BUILDER = new RootJsonPointerBuilder();
    /**
     * Returns the instance corresponding to the entire JSON value.
     *
     * @return the instance corresponding to the entire JSON value.
     */
    public static JsonPointerBuilder newInstance() {
        return INTIAL_BUILDER;
    }

    protected JsonPointerBuilder() {
    }

    /**
     * Appends the next event to this builder.
     *
     * @param event the event of the parser, cannot be {@code null}.
     * @param parser the JSON parser, cannot be {@code null}.
     * @return new builder changed by the specified event.
     */
    public JsonPointerBuilder withEvent(Event event, JsonParser parser) {
        if (event == Event.START_ARRAY) {
            return new ArrayJsonPointerBuilder(this);
        } else if (event == Event.START_OBJECT) {
            return new ObjectJsonPointerBuilder(this);
        }
        return this;
    }

    /**
     * Returns the current JSON pointer as a string.
     *
     * @return the current JSON pointer as a string, may be {@code null}.
     */
    public String toPointer() {
        StringBuilder builder = new StringBuilder();
        appendReferenceTokens(builder);
        return builder.toString();
    }

    /**
     * Appends reference tokens to the builder.
     *
     * @param builder the builder to build a JSON pointer as a stirng.
     */
    protected void appendReferenceTokens(StringBuilder builder) {
    }

    private static class InitialJsonPointerBuilder extends JsonPointerBuilder {

        public JsonPointerBuilder withEvent(Event event, JsonParser parser) {
            if (event == Event.START_ARRAY) {
                return new ArrayJsonPointerBuilder(ROOT_BUILDER);
            } else if (event == Event.START_OBJECT) {
                return new ObjectJsonPointerBuilder(ROOT_BUILDER);
            } else if (event == Event.END_ARRAY || event == Event.END_OBJECT) {
                throw new IllegalStateException();
            }
            return ROOT_BUILDER;
        }

        @Override
        public String toPointer() {
            return null;
        }
    }

    private static class RootJsonPointerBuilder extends JsonPointerBuilder {

        public JsonPointerBuilder withEvent(Event event, JsonParser parser) {
            throw new IllegalStateException();
        }
    }

    private static class ArrayJsonPointerBuilder extends JsonPointerBuilder {

        private final JsonPointerBuilder parent;
        private int index;

        private ArrayJsonPointerBuilder(JsonPointerBuilder parent) {
            this.parent = parent;
            this.index = -1;
        }

        @Override
        public JsonPointerBuilder withEvent(Event event, JsonParser parser) {
            if (event == Event.END_ARRAY) {
                return this.parent;
            }
            this.index++;
            return super.withEvent(event, parser);
        }

        @Override
        protected void appendReferenceTokens(StringBuilder builder) {
            parent.appendReferenceTokens(builder);
            if (index >= 0) {
                builder.append("/").append(index);
            }
        }
    }

    private static class ObjectJsonPointerBuilder extends JsonPointerBuilder {

        private final JsonPointerBuilder parent;
        private String keyName;
        private Event lastEvent;

        private ObjectJsonPointerBuilder(JsonPointerBuilder parent) {
            this.parent = parent;
        }

        @Override
        public JsonPointerBuilder withEvent(Event event, JsonParser parser) {
            this.lastEvent = event;
            if (event == Event.END_OBJECT) {
                return this.parent;
            } else if (event == Event.KEY_NAME) {
                this.keyName = parser.getString();
                return this;
            } else {
                return super.withEvent(event, parser);
            }
        }

        @Override
        protected void appendReferenceTokens(StringBuilder builder) {
            parent.appendReferenceTokens(builder);
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
