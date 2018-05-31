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
        case "additionalItems":
            addAdditionalItems(builder);
            break;
        case "additionalProperties":
            addAdditionalProperties(builder);
            break;
        case "allOf":
            addAllOf(builder);
            break;
        case "anyOf":
            addAnyOf(builder);
            break;
        case "const":
            addConst(builder);
            break;
        case "description":
            addDescription(builder);
            break;
        case "else":
            addElse(builder);
            break;
        case "exclusiveMaximum":
            addExclusiveMaximum(builder);
            break;
        case "exclusiveMinimum":
            addExclusiveMinimum(builder);
            break;
        case "if":
            addIf(builder);
            break;
        case "items":
            addItems(builder);
            break;
        case "maximum":
            addMaximum(builder);
            break;
        case "maxItems":
            addMaxItems(builder);
            break;
        case "maxLength":
            addMaxLength(builder);
            break;
        case "minimum":
            addMinimum(builder);
            break;
        case "minItems":
            addMinItems(builder);
            break;
        case "minLength":
            addMinLength(builder);
            break;
        case "multipleOf":
            addMultipleOf(builder);
            break;
        case "not":
            addNot(builder);
            break;
        case "oneOf":
            addOneOf(builder);
            break;
        case "patternProperties":
            addPatternProperties(builder);
            break;
        case "properties":
            addProperties(builder);
            break;
        case "required":
            addRequired(builder);
            break;
        case "then":
            addThen(builder);
            break;
        case "title":
            addTitle(builder);
            break;
        case "type":
            addType(builder);
            break;
        }
    }
    
    private void addAdditionalItems(JsonSchemaBuilder builder) {
        builder.withAdditionalItems(subschema());
    }
    
    private void addAdditionalProperties(JsonSchemaBuilder builder) {
        builder.withAdditionalProperties(subschema());
    }

    private void addAllOf(JsonSchemaBuilder builder) {
        if (parser.next() == Event.START_ARRAY) {
            builder.withAllOf(arrayOfSubschemas());
        }
    }

    private void addAnyOf(JsonSchemaBuilder builder) {
        if (parser.next() == Event.START_ARRAY) {
            builder.withAnyOf(arrayOfSubschemas());
        }
    }
    
    private void addConst(JsonSchemaBuilder builder) {
        parser.next();
        builder.withConst(parser.getValue());
    }

    private void addDescription(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_STRING) {
            builder.withDescription(parser.getString());
        }
    }
    
    private void addElse(JsonSchemaBuilder builder) {
        builder.withElse(subschema());
    }
    
    private void addExclusiveMaximum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withExclusiveMaximum(parser.getBigDecimal());
        }
    }

    private void addExclusiveMinimum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withExclusiveMinimum(parser.getBigDecimal());
        }
    }
    
    private void addIf(JsonSchemaBuilder builder) {
        builder.withIf(subschema());
    }
    
    private void addItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            builder.withItems(arrayOfSubschemas());
        } else {
            builder.withItem(subschema(event));
        }
    }

    private void addMaximum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMaximum(parser.getBigDecimal());
        }
    }
    
    private void addMaxItems(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMaxItems(parser.getInt());
        }
    }

    private void addMaxLength(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMaxLength(parser.getInt());
        }
    }

    private void addMinimum(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMinimum(parser.getBigDecimal());
        }
    }

    private void addMinItems(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMinItems(parser.getInt());
        }
    }

    private void addMinLength(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMinLength(parser.getInt());
        }
    }

    private void addMultipleOf(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_NUMBER) {
            builder.withMultipleOf(parser.getBigDecimal());
        }
    }

    private void addNot(JsonSchemaBuilder builder) {
        builder.withNot(subschema());
    }

    private void addOneOf(JsonSchemaBuilder builder) {
        if (parser.next() == Event.START_ARRAY) {
            builder.withOneOf(arrayOfSubschemas());
        }
    }

    private void addPatternProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            return;
        }
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                builder.withPatternProperty(parser.getString(), subschema());
            } else if (event == Event.END_OBJECT) {
                break;
            }
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
                builder.withProperty(parser.getString(), subschema());
            } else if (event == Event.END_OBJECT) {
                break;
            }
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
    
    private void addThen(JsonSchemaBuilder builder) {
        builder.withThen(subschema());
    }
    
    private void addTitle(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_STRING) {
            builder.withTitle(parser.getString());
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
    
    private static InstanceType findType(String name) {
        return InstanceType.valueOf(name.toUpperCase());
    }
    
    private JsonSchema subschema() {
        return subschema(parser.next());
    }
    
    private JsonSchema subschema(Event event) {
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
    
    private List<JsonSchema> arrayOfSubschemas() {
        List<JsonSchema> subschemas = new ArrayList<>();
        Event event = null;
        while ((event = parser.next()) != Event.END_ARRAY) {
            subschemas.add(subschema(event));
        }
        return subschemas;
    }
}
