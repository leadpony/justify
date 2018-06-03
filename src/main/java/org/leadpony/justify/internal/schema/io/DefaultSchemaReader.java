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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.internal.schema.DefaultSchemaBuilder;
import org.leadpony.justify.internal.schema.DefaultSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.schema.SchemaVisitor;

/**
 * Default implementation of {@link JsonSchemaReader}.
 * 
 * @author leadpony
 */
public class DefaultSchemaReader implements JsonSchemaReader {
    
    private static final Logger log = Logger.getLogger(DefaultSchemaReader.class.getName());
    
    private final JsonParser parser;
    private final DefaultSchemaBuilderFactory factory;

    private boolean alreadyRead;
    private boolean alreadyClosed;
    
    private final Map<URI, JsonSchema> externalSchemas = new HashMap<>();
    
    private final Map<URI, JsonSchema> idMap = new HashMap<>();
    private final Map<SchemaReference, URI> references = new HashMap<>();
    
    public DefaultSchemaReader(JsonParser parser, DefaultSchemaBuilderFactory factory) {
        this.parser = parser;
        this.factory = factory;
    }
    
    @Override
    public JsonSchema read() {
        if (alreadyRead || alreadyClosed) {
            throw new IllegalStateException("alreay read");
        }
        JsonSchema rootSchema = rootSchema();
        resolveRelativeURI(rootSchema);
        resolveAllReferences();
        this.alreadyRead = true;
        return rootSchema;
    }
    
    @Override
    public void close() {
        if (this.alreadyClosed) {
            this.parser.close();
            this.alreadyClosed = true;
        }
    }
    
    @Override
    public JsonSchemaReader withExternalSchema(JsonSchema schema) {
        Objects.requireNonNull(schema, "schema must not be null.");
        Map<URI, JsonSchema> idMap = schema.idMap();
        if (idMap != null) {
            externalSchemas.putAll(idMap);
        }
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
        DefaultSchemaBuilder builder = this.factory.createBuilder();
        Event event = null;
        while (parser.hasNext() && (event = parser.next()) != Event.END_OBJECT) {
            if (event == Event.KEY_NAME) {
                String name = parser.getString();
                populateSchema(name, builder);
            }
        }
        if (root) {
            builder.withIdMap(idMap);
        }
        JsonSchema schema = builder.build();
        if (schema.id() != null) {
            idMap.put(schema.id(), schema);
        }
        if (schema instanceof SchemaReference) {
            SchemaReference reference = (SchemaReference)schema;
            references.put(reference, reference.ref());
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
    
    private void populateSchema(String keyName, DefaultSchemaBuilder builder) {
        switch (keyName) {
        case "$id":
            addId(builder);
            break;
        case "$ref":
            addRef(builder);
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
    
    private void addId(DefaultSchemaBuilder builder) {
        if (parser.next() != Event.VALUE_STRING) {
            return;
        }
        builder.withId(URI.create(parser.getString()));
    }

    private void addRef(DefaultSchemaBuilder builder) {
        if (parser.next() != Event.VALUE_STRING) {
            return;
        }
        builder.withRef(URI.create(parser.getString()));
    }
    
    private void addSchema(DefaultSchemaBuilder builder) {
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
    
    private void resolveAllReferences() {
        for (Map.Entry<SchemaReference, URI> entry : this.references.entrySet()) {
            SchemaReference ref = entry.getKey();
            URI uri = entry.getValue();
            if (externalSchemas.containsKey(uri)) {
                ref.setReferencedSchema(externalSchemas.get(uri));
            }
        }
    }
    
    private void resolveRelativeURI(JsonSchema rootSchema) {
        ResolvingVisitor visitor = new ResolvingVisitor();
        visitor.visit(rootSchema);
    }
    
    private class ResolvingVisitor implements SchemaVisitor {
        
        @Override
        public void visit(JsonSchema schema) {
            if (schema.hasId()) {
                log.info("id = " + schema.id().toString());
            }
            if (schema instanceof SchemaReference) {
                visitReference((SchemaReference)schema);
            } 
            visitSubschemas(schema);
        }
        
        private void visitReference(SchemaReference reference) {
            log.info("ref = " + reference.ref().toString());
        }
    }
}
