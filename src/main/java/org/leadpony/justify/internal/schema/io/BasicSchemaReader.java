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
import org.leadpony.justify.internal.base.SimpleJsonPointer;
import org.leadpony.justify.internal.base.URIs;
import org.leadpony.justify.internal.schema.BasicSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.LeafSchema;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.schema.SchemaReferenceBuilder;

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
    
    protected JsonParser getParser() {
        return parser;
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
        SchemaReferenceBuilder builder = this.factory.createBuilder();
        JsonLocation refLocation = null;
        Event event = null;
        while (parser.hasNext() && (event = parser.next()) != Event.END_OBJECT) {
            if (event == Event.KEY_NAME) {
                String keyName = parser.getString();
                if (keyName.equals("$ref")) {
                    addRef(builder);
                    refLocation = parser.getLocation();
                } else {
                    populateSchema(keyName, builder);
                }
            }
        }
        JsonSchema schema = builder.build();
        if (schema instanceof SchemaReference) {
            SchemaReference reference = (SchemaReference)schema;
            references.put(reference, refLocation);
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
            addUnknown(keyName, builder);
            break;
        }
    }
    
    private void addRef(SchemaReferenceBuilder builder) {
        if (parser.next() == Event.VALUE_STRING) {
            URI uri = URI.create(parser.getString());
            builder.withRef(uri);
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
    
    private void addUnknown(String name, JsonSchemaBuilder builder) {
        if (parser.hasNext()) {
            processUnknown(parser.next(), SimpleJsonPointer.of(name), builder);
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
    
    private void processUnknown(Event event, SimpleJsonPointer where, JsonSchemaBuilder builder) {
        switch (event) {
        case START_OBJECT:
            builder.withSubschema(where.toString(), objectSchema(false));
            break;
        case START_ARRAY:
            for (int i = 0; parser.hasNext(); i++) {
                if ((event = parser.next()) == Event.END_ARRAY) {
                    break;
                }
                processUnknown(event, where.concat(i), builder);
            }
            break;
        case KEY_NAME:
            if (parser.hasNext()) {
                processUnknown(parser.next(), where.concat(parser.getString()), builder);
            }
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
            ((LeafSchema)schema).setAbsoluteId(baseURI);
            addIdentifiedSchema(baseURI, schema);
        }
        if (schema instanceof SchemaReference) {
            SchemaReference ref = (SchemaReference)schema;
            ref.setRef(baseURI.resolve(ref.getRef()));
        }
        for (JsonSchema subschema : schema.getSubschemas()) {
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
                return schema.findSubschema(fragment);
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
