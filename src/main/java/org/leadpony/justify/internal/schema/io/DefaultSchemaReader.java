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

package org.leadpony.justify.internal.schema.io;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.internal.schema.SchemaReference;

/**
 * Default implementation of {@link JsonSchemaReader}.
 * 
 * @author leadpony
 */
public class DefaultSchemaReader implements JsonSchemaReader {
    
    private static final Logger log = Logger.getLogger(DefaultSchemaReader.class.getName());
    
    private final JsonParser parser;
    private final JsonSchemaBuilderFactory factory;

    private boolean alreadyRead;
    private boolean alreadyClosed;
    
    private URI initialURI;
    private final SchemaResolver resolver = new SchemaResolver();
    
    public DefaultSchemaReader(JsonParser parser, JsonSchemaBuilderFactory factory) {
        this.parser = parser;
        this.factory = factory;
    }
    
    @Override
    public JsonSchema read() {
        if (alreadyRead || alreadyClosed) {
            throw new IllegalStateException("already read");
        }
        JsonSchema rootSchema = rootSchema();
        resolver.resolveAll(initialURI).linkAll();
        this.alreadyRead = true;
        return rootSchema;
    }
    
    @Override
    public void close() {
        if (!this.alreadyClosed) {
            this.parser.close();
            this.alreadyClosed = true;
        }
    }
    
    @Override
    public JsonSchemaReader withExternalSchema(URI id, JsonSchema schema) {
        Objects.requireNonNull(id, "id must not be null.");
        Objects.requireNonNull(schema, "schema must not be null.");
        resolver.addExternalSchema(id, schema);
        return this;
    }
    
    private JsonSchema rootSchema() {
        Event event = parser.next();
        switch (event) {
        case VALUE_TRUE:
        case VALUE_FALSE:
            return literalSchema(event);
        case START_OBJECT:
            return objectSchema(true);
        default:
            return null;
        }
    }
    
    private JsonSchema objectSchema(boolean root) {
        SchemaResolver.Entry entry = resolver.lastEntry();
        JsonSchemaBuilder builder = this.factory.createBuilder();
        Event event = null;
        while (parser.hasNext() && (event = parser.next()) != Event.END_OBJECT) {
            if (event == Event.KEY_NAME) {
                String name = parser.getString();
                if ("$ref".equals(name)) {
                    return schemaReference();
                } else {
                    populateSchema(name, builder);
                }
            }
        }
        JsonSchema schema = builder.build();
        if (schema.hasId()) {
            resolver.addIdentifiedSchema(schema, entry);
        }
        return schema;
    }
    
    private JsonSchema literalSchema(Event event) {
        switch (event) {
        case VALUE_TRUE:
            return JsonSchema.TRUE;
        case VALUE_FALSE:
            return JsonSchema.FALSE;
        default:
            return null;
        }
    }
    
    private SchemaReference schemaReference() {
        if (parser.next() != Event.VALUE_STRING) {
            return null;
        }
        URI uri = URI.create(parser.getString());
        SchemaReference reference = new SchemaReference(uri);
        resolver.addReference(reference, parser.getLocation());
        while (parser.hasNext() && parser.next() != Event.END_OBJECT)
            ;
        return reference;
    }
    
    private void populateSchema(String keyName, JsonSchemaBuilder builder) {
        switch (keyName) {
        case "$id":
            addId(builder);
            break;
        case "$schema":
            addSchema(builder);
            break;
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
        case "definitions":
            addDefinitions(builder);
            break;
        case "description":
            addDescription(builder);
            break;
        case "else":
            addElse(builder);
            break;
        case "enum":
            addEnum(builder);
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
        default:
            skipCurrentValue(keyName);
            break;
        }
    }
    
    private void addId(JsonSchemaBuilder builder) {
        if (parser.next() != Event.VALUE_STRING) {
            return;
        }
        builder.withId(URI.create(parser.getString()));
    }

    private void addSchema(JsonSchemaBuilder builder) {
        if (parser.next() != Event.VALUE_STRING) {
            return;
        }
        builder.withSchema(URI.create(parser.getString()));
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
    
    private void addDefinitions(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            return;
        }
        while (parser.hasNext() && (event = parser.next()) != Event.END_OBJECT) {
            if (event == Event.KEY_NAME) {
                builder.withDefinition(parser.getString(), subschema());
            }
        }
    }

    private void addDescription(JsonSchemaBuilder builder) {
        if (parser.next() == Event.VALUE_STRING) {
            builder.withDescription(parser.getString());
        }
    }
    
    private void addElse(JsonSchemaBuilder builder) {
        builder.withElse(subschema());
    }
    
    private void addEnum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            Set<JsonValue> values = new LinkedHashSet<>();
            while ((event = parser.next()) != Event.END_ARRAY) {
                values.add(parser.getValue());
            }
            builder.withEnum(values);
        }
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
                String name = parser.getString(); 
                builder.withProperty(name, subschema());
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
            Set<InstanceType> types = EnumSet.noneOf(InstanceType.class);
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
            return objectSchema(false);
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
    
    private void skipCurrentValue(String propertyName) {
        log.fine("Skipping unknown property: " + propertyName);
        if (parser.hasNext()) {
            switch (parser.next()) {
            case START_ARRAY:
                parser.skipArray();
                break;
            case START_OBJECT:
                parser.skipObject();
                break;
            default:
                break;
            }
        }
    }
}
