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

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * @author leadpony
 *
 */
public class InstanceBuilder {
    
    private final JsonProvider provider;
    private final RootVisitor rootVisitor = new RootVisitor();
    private JsonBuilderFactory builderFactory;
    private Visitor currentVisitor = rootVisitor;
    
    public InstanceBuilder(JsonProvider provider) {
        this.provider = provider;
    }
    
    public boolean append(Event event, JsonParser parser) {
        this.currentVisitor = this.currentVisitor.visit(event, parser);
        return (this.currentVisitor != this.rootVisitor); 
    }
    
    public JsonValue build() {
        return rootVisitor.rootValue();
    }
    
    private JsonBuilderFactory createBuilderFactory() {
        if (this.builderFactory == null) {
            this.builderFactory = this.provider.createBuilderFactory(null);
        }
        return this.builderFactory;
    }
    
    private static interface Visitor {
        
        Visitor visit(Event event, JsonParser parser);
        
        void append(JsonValue value);
    }
    
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
                append(parser.getValue());
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

    private class ArrayVisitor implements Visitor {
        
        private final Visitor parent;
        private final JsonArrayBuilder builder;
        
        ArrayVisitor(Visitor parent) {
            this.parent = parent;
            this.builder = createBuilderFactory().createArrayBuilder();
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
                builder.add(parser.getValue());
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

    private class ObjectVisitor implements Visitor {
        
        private final Visitor parent;
        private final JsonObjectBuilder builder;
        private String propertyName;
        
        ObjectVisitor(Visitor parent) {
            this.parent = parent;
            this.builder = createBuilderFactory().createObjectBuilder();
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
                builder.add(this.propertyName, parser.getValue());
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
