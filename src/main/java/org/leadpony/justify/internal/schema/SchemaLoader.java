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

package org.leadpony.justify.internal.schema;

import java.util.HashSet;
import java.util.Set;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;

/**
 * @author leadpony
 */
public class SchemaLoader {
    
    private final JsonParser parser;
    private final JsonSchemaBuilderFactory factory;
    @SuppressWarnings("unused")
    private final JsonProvider provider;
    
    public SchemaLoader(JsonParser parser, JsonSchemaBuilderFactory factory, JsonProvider provider) {
        this.parser = parser;
        this.factory = factory;
        this.provider = provider;
    }
    
    public JsonSchema load() {
        return rootSchema();
    }
    
    private JsonSchema rootSchema() {
        switch (parser.next()) {
        case VALUE_TRUE:
            return BooleanSchema.valueOf(true);
        case VALUE_FALSE:
            return BooleanSchema.valueOf(false);
        case START_OBJECT:
            return objectSchema();
        default:
            return null;
        }
    }
    
    private JsonSchema objectSchema() {
        JsonSchemaBuilder builder = this.factory.createBuilder();
        while (parser.hasNext()) {
            switch (parser.next()) {
            case KEY_NAME:
                populateSchema(parser.getString(), builder);
                break;
            case END_OBJECT:
                return builder.build();
            default:
                break;
            }
        }
        return null;
    }
    
    private void populateSchema(String keyName, JsonSchemaBuilder builder) {
        switch (keyName) {
        case "title":
            addTitle(builder);
            break;
        case "description":
            addDescription(builder);
            break;
        case "type":
            addType(builder);
            break;
        case "required":
            addRequired(builder);
            break;
        case "properties":
            addProperties(builder);
            break;
        case "maximum":
            addMaximum(builder);
            break;
        case "exclusiveMaximum":
            addExclusiveMaximum(builder);
            break;
        case "minimum":
            addMinimum(builder);
            break;
        case "exclusiveMinimum":
            addExclusiveMinimum(builder);
            break;
        }
    }
    
    private void addTitle(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_STRING) {
            builder.withTitle(parser.getString());
        }
    }
    
    private void addDescription(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_STRING) {
            builder.withDescription(parser.getString());
        }
    }
    
    private void addType(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withType(findType(parser.getString()));
        } else if (event == Event.START_ARRAY) {
            Set<InstanceType> types = new HashSet<>();
            while ((event = parser.next()) != Event.END_ARRAY) {
                if (event == Event.VALUE_STRING) {
                    types.add(findType(parser.getString()));
                }
            }
            builder.withType(types);
        }
    }
    
    private void addRequired(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            Set<String> names = new HashSet<>();
            while ((event = parser.next()) != Event.END_ARRAY) {
                if (event == Event.VALUE_STRING) {
                    names.add(parser.getString());
                }
            }
            builder.withRequired(names);
        }
    }
    
    private void addProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            return;
        }
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String keyName = parser.getString();
                switch (parser.next()) {
                case START_OBJECT:
                    builder.withProperty(keyName, objectSchema());
                    break;
                case VALUE_TRUE:
                    builder.withProperty(keyName, BooleanSchema.valueOf(true));
                    break;
                case VALUE_FALSE:
                    builder.withProperty(keyName, BooleanSchema.valueOf(false));
                    break;
                default:
                    break;
                }
            } else if (event == Event.END_OBJECT) {
                break;
            }
        }
    }
    
    private void addMaximum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMaximum(parser.getBigDecimal());
        }
    }

    private void addExclusiveMaximum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withExclusiveMaximum(parser.getBigDecimal());
        }
    }

    private void addMinimum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMinimum(parser.getBigDecimal());
        }
    }

    private void addExclusiveMinimum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withExclusiveMinimum(parser.getBigDecimal());
        }
    }

    private static InstanceType findType(String name) {
        return InstanceType.valueOf(name.toUpperCase());
    }
}
