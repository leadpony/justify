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
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.core.JsonSchemaResolver;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.SimpleJsonLocation;
import org.leadpony.justify.internal.base.URIs;
import org.leadpony.justify.internal.schema.BasicSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.BasicSchema;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.schema.EnhancedSchemaBuilder;

/**
 * Basic implementation of {@link JsonSchemaReader}.
 * 
 * @author leadpony
 */
public class BasicSchemaReader implements JsonSchemaReader {
    
    private static final URI DEFAULT_INITIAL_BASE_URI = URI.create("");
    
    private final JsonParser parser;
    private final BasicSchemaBuilderFactory factory;

    private boolean alreadyRead;
    private boolean alreadyClosed;
    
    private URI initialBaseURI = DEFAULT_INITIAL_BASE_URI;
    
    private final Map<URI, JsonSchema> identified = new HashMap<>();
    private final Map<SchemaReference, JsonLocation> references = new IdentityHashMap<>();

    private final List<JsonSchemaResolver> resolvers = new ArrayList<>();
    
    /**
     * Constructs this schema reader.
     * 
     * @param parser the parser of JSON document.
     * @param factory the factory for producing schema builders.
     */
    public BasicSchemaReader(JsonParser parser, BasicSchemaBuilderFactory factory) {
        this.parser = parser;
        this.factory = factory;
    }
    
    @Override
    public JsonSchema read() {
        if (alreadyRead || alreadyClosed) {
            throw new IllegalStateException("already read");
        }
        JsonSchema rootSchema = rootSchema();
        postprocess(rootSchema);
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
    public JsonSchemaReader withSchemaResolver(JsonSchemaResolver resolver) {
        Objects.requireNonNull(resolver, "resolver must not be null.");
        resolvers.add(resolver);
        return this;
    }
    
    protected void postprocess(JsonSchema rootSchema) {
        if (rootSchema != null) {
            makeIdentifiersAbsoluteFromRoot(rootSchema, initialBaseURI);
            resolveAllReferences();
        }
    }
    
    protected void addProblem(Problem problem) {
    }
    
    private JsonSchema rootSchema() {
        if (!parser.hasNext()) {
            return null;
        }
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
        EnhancedSchemaBuilder builder = this.factory.createBuilder();
        JsonLocation refLocation = null;
        Event event = null;
        while (parser.hasNext() && (event = parser.next()) != Event.END_OBJECT) {
            if (event == Event.KEY_NAME) {
                String keyName = parser.getString();
                populateSchema(keyName, builder);
                if (keyName.equals("$ref")) {
                    refLocation = parser.getLocation();
                }
            }
        }
        JsonSchema schema = builder.build();
        if (schema instanceof SchemaReference) {
            SchemaReference reference = (SchemaReference)schema;
            references.put(reference, SimpleJsonLocation.before(refLocation));
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
    
    private void populateSchema(String keyName, EnhancedSchemaBuilder builder) {
        switch (keyName) {
        case "$id":
            addId(builder);
            break;
        case "$schema":
            addSchema(builder);
            break;
        case "$ref":
            addRef(builder);
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
        case "contains":
            addContains(builder);
            break;
        case "default":
            addDefault(builder);
            break;
        case "definitions":
            addDefinitions(builder);
            break;
        case "dependencies":
            addDependencies(builder);
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
        case "maxContains":
            addMaxContains(builder);
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
        case "maxProperties":
            addMaxProperties(builder);
            break;
        case "minContains":
            addMinContains(builder);
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
        case "minProperties":
            addMinProperties(builder);
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
        case "pattern":
            addPattern(builder);
            break;
        case "patternProperties":
            addPatternProperties(builder);
            break;
        case "properties":
            addProperties(builder);
            break;
        case "propertyNames":
            addPropertyNames(builder);
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
        case "uniqueItems":
            addUniqueItems(builder);
            break;
        default:
            addUnknown(keyName, builder);
            break;
        }
    }
    
    private void addRef(EnhancedSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            URI uri = URI.create(parser.getString());
            builder.withRef(uri);
        } else {
            skipValue(event);
        }
    }
    
    private void addId(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withId(URI.create(parser.getString()));
        } else {
            skipValue(event);
        }
    }

    private void addSchema(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withSchema(URI.create(parser.getString()));
        } else {
            skipValue(event);
        }
    }

    private void addType(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            consumeType(parser.getString(), builder::withType);
        } else if (event == Event.START_ARRAY) {
            Set<InstanceType> types = EnumSet.noneOf(InstanceType.class);
            while ((event = parser.next()) != Event.END_ARRAY) {
                if (event == Event.VALUE_STRING) {
                    consumeType(parser.getString(), types::add);
                } else {
                    skipValue(event);
                }
            }
            if (!types.isEmpty()) {
                builder.withType(types);
            }
        } else {
            skipValue(event);
        }
    }
 
    private void addEnum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            Set<JsonValue> values = new LinkedHashSet<>();
            while ((event = parser.next()) != Event.END_ARRAY) {
                values.add(parser.getValue());
            }
            builder.withEnum(values);
        } else {
            skipValue(event);
        }
    }
    
    private void addConst(JsonSchemaBuilder builder) {
        parser.next();
        builder.withConst(parser.getValue());
    }
    
    private void addMaximum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMaximum(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }
    
    private void addExclusiveMaximum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withExclusiveMaximum(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }

    private void addMinimum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMinimum(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }

    private void addExclusiveMinimum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withExclusiveMinimum(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }
    
    private void addMultipleOf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMultipleOf(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }

    private void addMaxLength(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMaxLength(parser.getInt());
        } else {
            skipValue(event);
        }
    }
    
    private void addMinLength(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMinLength(parser.getInt());
        } else {
            skipValue(event);
        }
    }
    
    private void addPattern(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withPattern(parser.getString());
        } else {
            skipValue(event);
        }
    }
   
    private void addItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            builder.withItems(arrayOfSubschemas());
        } else if (canStartSchema(event)) {
            builder.withItem(subschema(event));
        } else {
            skipValue(event);
        }
    }
   
    private void addAdditionalItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canStartSchema(event)) {
            builder.withAdditionalItems(subschema(event));
        } else {
            skipValue(event);
        }
    }

    private void addMaxItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMaxItems(parser.getInt());
        } else {
            skipValue(event);
        }
    }

    private void addMinItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMinItems(parser.getInt());
        } else {
            skipValue(event);
        }
    }
 
    private void addUniqueItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        switch (event) {
        case VALUE_TRUE:
            builder.withUniqueItems(true);
            break;
        case VALUE_FALSE:
            builder.withUniqueItems(false);
            break;
        default:
            skipValue(event);
            break;
        }
    }
    
    private void addContains(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canStartSchema(event)) {
            builder.withContains(subschema(event));
        } else {
            skipValue(event);
        }
    }
   
    private void addMaxContains(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMaxContains(parser.getInt());
        } else {
            skipValue(event);
        }
    }
    
    private void addMinContains(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMinContains(parser.getInt());
        } else {
            skipValue(event);
        }
    }
    
    private void addMaxProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMaxProperties(parser.getInt());
        } else {
            skipValue(event);
        }
    }
 
    private void addMinProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMinProperties(parser.getInt());
        } else {
            skipValue(event);
        }
    }
    
    private void addRequired(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            Set<String> names = new HashSet<>();
            while ((event = parser.next()) != Event.END_ARRAY) {
                if (event == Event.VALUE_STRING) {
                    names.add(parser.getString());
                } else {
                    skipValue(event);
                }
            }
            builder.withRequired(names);
        } else {
            skipValue(event);
        }
    }
  
    private void addProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            skipValue(event);
            return;
        }
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String name = parser.getString(); 
                event = parser.next();
                if (canStartSchema(event)) {
                    builder.withProperty(name, subschema(event));
                } else {
                    skipValue(event);
                }
            } else if (event == Event.END_OBJECT) {
                break;
            }
        }
    }
    
    private void addPatternProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            skipValue(event);
            return;
        }
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String pattern = parser.getString();
                event = parser.next();
                if (canStartSchema(event)) {
                    builder.withPatternProperty(pattern, subschema(event));
                } else {
                    skipValue(event);
                }
            } else if (event == Event.END_OBJECT) {
                break;
            }
        }
    }
    
    private void addAdditionalProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canStartSchema(event)) {
            builder.withAdditionalProperties(subschema(event));
        } else {
            skipValue(event);
        }
    }
    
    private void addDependencies(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            skipValue(event);
            return;
        }
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String property = parser.getString();
                if (parser.hasNext()) {
                    event = parser.next();
                    if (canStartSchema(event)) {
                        builder.withDependency(property, subschema(event));
                    } else if (event == Event.START_ARRAY) {
                        Set<String> required = new HashSet<>();
                        while (parser.hasNext()) {
                            event = parser.next();
                            if (event == Event.VALUE_STRING) {
                                required.add(parser.getString());
                            } else if (event == Event.END_ARRAY) {
                                builder.withDependency(property, required);
                                break;
                            } 
                        }
                    }
                 }
            } else if (event == Event.END_OBJECT) {
                break;
            }
        }
    }

    private void addPropertyNames(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canStartSchema(event)) {
            builder.withPropertyNames(subschema(event));
        } else {
            skipValue(event);
        }
    }
   
    private void addAllOf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            builder.withAllOf(arrayOfSubschemas());
        } else {
            skipValue(event);
        }
    }

    private void addAnyOf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            builder.withAnyOf(arrayOfSubschemas());
        } else {
            skipValue(event);
        }
    }
    
    private void addOneOf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            builder.withOneOf(arrayOfSubschemas());
        } else {
            skipValue(event);
        }
    }

    private void addNot(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canStartSchema(event) ) {
            builder.withNot(subschema(event));
        } else {
            skipValue(event);
        }
    }
    
    private void addIf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canStartSchema(event) ) {
            builder.withIf(subschema(event));
        } else {
            skipValue(event);
        }
    }
    
    private void addThen(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canStartSchema(event) ) {
            builder.withThen(subschema(event));
        } else {
            skipValue(event);
        }
    }
    
    private void addElse(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canStartSchema(event) ) {
            builder.withElse(subschema(event));
        } else {
            skipValue(event);
        }
    }
    
    private void addDefinitions(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            skipValue(event);
            return;
        }
        while (parser.hasNext() && (event = parser.next()) != Event.END_OBJECT) {
            if (event == Event.KEY_NAME) {
                String name = parser.getString();
                event = parser.next();
                if (canStartSchema(event)) {
                    builder.withDefinition(name, subschema(event));
                } else {
                    skipValue(event);
                }
            }
        }
    }
    
    private void addTitle(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withTitle(parser.getString());
        } else {
            skipValue(event);
        }
    }
  
    private void addDescription(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withDescription(parser.getString());
        } else {
            skipValue(event);
        }
    }
    
    private void addDefault(JsonSchemaBuilder builder) {
        parser.next();
        builder.withDefault(parser.getValue());
    }
    
    private void addUnknown(String name, EnhancedSchemaBuilder builder) {
        if (parser.hasNext()) {
            Event event = parser.next();
            if (canStartSchema(event)) {
                builder.withUnknown(name, subschema(event));
            } else {
                skipValue(event);
            }
        }
    }
    
    private static void consumeType(String name, Consumer<InstanceType> consumer) {
        try {
            InstanceType type = InstanceType.valueOf(name.toUpperCase());
            consumer.accept(type);
        } catch (IllegalArgumentException e) {
        }
    }
    
    private static boolean canStartSchema(Event event) {
        return event == Event.START_OBJECT || 
               event == Event.VALUE_TRUE ||
               event == Event.VALUE_FALSE;
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
            if (canStartSchema(event)) {
                subschemas.add(subschema(event));
            } else {
                skipValue(event);
            }
        }
        return subschemas;
    }
    
    private void skipValue(Event event) {
        switch (event) {
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
    
    private void makeIdentifiersAbsoluteFromRoot(JsonSchema root, URI baseURI) {
        if (!root.hasId()) {
            addIdentifiedSchema(baseURI, root);
        }
        makeIdentifiersAbsolute(root, baseURI);
    }
    
    private void makeIdentifiersAbsolute(JsonSchema schema, URI baseURI) {
        if (schema.hasId()) {
            baseURI = baseURI.resolve(schema.id());
            ((BasicSchema)schema).setAbsoluteId(baseURI);
            addIdentifiedSchema(baseURI, schema);
        }
        if (schema instanceof SchemaReference) {
            SchemaReference ref = (SchemaReference)schema;
            ref.setRef(baseURI.resolve(ref.getRef()));
        }
        for (JsonSchema subschema : schema.getAllSubschemas()) {
            makeIdentifiersAbsolute(subschema, baseURI);
        }
    }
    
    private void addIdentifiedSchema(URI id, JsonSchema schema) {
        this.identified.put(URIs.withFragment(id), schema);
    }
    
    private void resolveAllReferences() {
        for (SchemaReference reference : this.references.keySet()) {
            URI ref = reference.getRef();
            JsonSchema schema = dereferenceSchema(ref);
            if (schema != null) {
                reference.setReferencedSchema(schema);
            } else {
                JsonLocation location = this.references.get(reference);
                Problem p = ProblemBuilder.newBuilder(location)
                        .withMessage("schema.problem.dereference")
                        .withParameter("ref", ref)
                        .build();
                addProblem(p);
            }
        }
    }

    private JsonSchema dereferenceSchema(URI ref) {
        ref = URIs.withFragment(ref);
        String fragment = ref.getFragment();
        if (fragment.startsWith("/")) {
            JsonSchema schema = resolveSchema(URIs.withEmptyFragment(ref));
            if (schema != null) {
                return schema.getSubschema(fragment);
            }
            return null;
        } else {
            return resolveSchema(ref);
        }
    }
    
    private JsonSchema resolveSchema(URI id) {
        JsonSchema schema = this.identified.get(id);
        if (schema != null) {
            return schema;
        }
        for (JsonSchemaResolver resolver : this.resolvers) {
            schema = resolver.resolveSchema(id);
            if (schema != null) {
                return schema;
            }
        }
        return null;
    }
}
