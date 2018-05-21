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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        Event event = parser.next();
        switch (event) {
        case VALUE_TRUE:
        case VALUE_FALSE:
            return literalSchema(event);
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
    
    private JsonSchema literalSchema(Event event) {
        switch (event) {
        case VALUE_TRUE:
            return JsonSchemas.ALWAYS_TRUE;
        case VALUE_FALSE:
            return JsonSchemas.ALWAYS_FALSE;
        default:
            return null;
        }
    }
    
    private void populateSchema(String keyName, JsonSchemaBuilder builder) {
        switch (keyName) {
        case "title":
            appendTitle(builder);
            break;
        case "description":
            appendDescription(builder);
            break;
        case "type":
            appendType(builder);
            break;
        case "required":
            appendRequired(builder);
            break;
        case "properties":
            appendProperties(builder);
            break;
        case "maximum":
            appendMaximum(builder);
            break;
        case "exclusiveMaximum":
            appendExclusiveMaximum(builder);
            break;
        case "minimum":
            appendMinimum(builder);
            break;
        case "exclusiveMinimum":
            appendExclusiveMinimum(builder);
            break;
        case "maxLength":
            appendMaxLength(builder);
            break;
        case "minLength":
            appendMinLength(builder);
            break;
        case "allOf":
            appendAllOf(builder);
            break;
        case "anyOf":
            appendAnyOf(builder);
            break;
        case "oneOf":
            appendOneOf(builder);
            break;
        case "not":
            appendNot(builder);
            break;
        }
    }
    
    private void appendTitle(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_STRING) {
            builder.withTitle(parser.getString());
        }
    }
    
    private void appendDescription(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_STRING) {
            builder.withDescription(parser.getString());
        }
    }
    
    private void appendType(JsonSchemaBuilder builder) {
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
    
    private void appendRequired(JsonSchemaBuilder builder) {
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
    
    private void appendProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            return;
        }
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String keyName = parser.getString();
                event = parser.next();
                switch (event) {
                case START_OBJECT:
                    builder.withProperty(keyName, objectSchema());
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                    builder.withProperty(keyName, literalSchema(event));
                    break;
                default:
                    break;
                }
            } else if (event == Event.END_OBJECT) {
                break;
            }
        }
    }
    
    private void appendMaximum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMaximum(parser.getBigDecimal());
        }
    }

    private void appendExclusiveMaximum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withExclusiveMaximum(parser.getBigDecimal());
        }
    }

    private void appendMinimum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMinimum(parser.getBigDecimal());
        }
    }

    private void appendExclusiveMinimum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withExclusiveMinimum(parser.getBigDecimal());
        }
    }
    
    private void appendMaxLength(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMaxLength(parser.getInt());
        }
    }
    
    private void appendMinLength(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMinLength(parser.getInt());
        }
    }

    private void appendAllOf(JsonSchemaBuilder builder) {
        builder.withAllOf(subschemaArray());
    }

    private void appendAnyOf(JsonSchemaBuilder builder) {
        builder.withAnyOf(subschemaArray());
    }

    private void appendOneOf(JsonSchemaBuilder builder) {
        builder.withOneOf(subschemaArray());
    }

    private void appendNot(JsonSchemaBuilder builder) {
        builder.withNot(subschema());
    }

    private static InstanceType findType(String name) {
        return InstanceType.valueOf(name.toUpperCase());
    }
    
    private JsonSchema subschema() {
        Event event = parser.next();
        switch (event) {
        case START_OBJECT:
            return objectSchema();
        case VALUE_TRUE:
        case VALUE_FALSE:
            return literalSchema(event);
        default:
            return null;
        }
    }
    
    private List<JsonSchema> subschemaArray() {
        List<JsonSchema> subschemas = new ArrayList<>();
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            while ((event = parser.next()) != Event.END_ARRAY) {
                switch (event) {
                case START_OBJECT:
                    subschemas.add(objectSchema());
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                    subschemas.add(literalSchema(event));
                    break;
                default:
                    break;
                }
            }
        }
        return subschemas;
    }
}
