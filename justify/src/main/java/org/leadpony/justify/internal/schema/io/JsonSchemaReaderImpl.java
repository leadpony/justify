/*
 * Copyright 2018-2020 the Justify authors.
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.Sets;
import org.leadpony.justify.internal.base.URIs;
import org.leadpony.justify.internal.base.json.DefaultPointerAwareJsonParser;
import org.leadpony.justify.internal.base.json.JsonService;
import org.leadpony.justify.internal.base.json.JsonValueParser;
import org.leadpony.justify.internal.keyword.IdKeyword;
import org.leadpony.justify.internal.keyword.RefKeyword;
import org.leadpony.justify.internal.keyword.UnknownKeyword;
import org.leadpony.justify.internal.keyword.format.UnknownFormatAttributeException;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.schema.BasicJsonSchema;
import org.leadpony.justify.internal.schema.Resolvable;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.schema.SchemaSpec;
import org.leadpony.justify.internal.validator.JsonValidator;

/**
 * @author leadpony
 */
public class JsonSchemaReaderImpl extends AbstractJsonSchemaReader implements ProblemHandler {

    private final SchemaKeywordParser parser;
    private final JsonService jsonService;
    private final Map<String, KeywordType> keywordTypeMap;
    private final JsonSchema metaschema;

    // schemas having $id keyword.
    private final Set<JsonSchema> identifiedSchemas = Sets.newIdentitySet();
    private final LinkedList<Reference> referencesToResolve = new LinkedList<>();
    private final List<Reference> referenceResolved = new ArrayList<>();

    private URI initialBaseUri = DEFAULT_INITIAL_BASE_URI;

    public JsonSchemaReaderImpl(
            JsonParser parser,
            JsonService jsonService,
            SchemaSpec spec,
            Map<String, Object> config) {
        this(parser, jsonService, spec.getBareKeywordTypes(), config, null);
    }

    /**
     * Constructs this reader.
     *
     * @param parser
     * @param jsonService
     * @param keywordTypeMap
     * @param config
     * @param metaschema     the metaschema of the schema to read. This can be
     *                       {@code null}.
     */
    public JsonSchemaReaderImpl(
            JsonParser parser,
            JsonService jsonService,
            Map<String, KeywordType> keywordTypeMap,
            Map<String, Object> config,
            JsonSchema metaschema) {
        super(config);

        this.parser = wrapJsonParser(parser, jsonService, metaschema);
        this.jsonService = jsonService;
        this.keywordTypeMap = keywordTypeMap;
        this.metaschema = metaschema;

        if (parser instanceof JsonValidator) {
            ((JsonValidator) parser).withHandler(this);
        }
    }

    /* As a AbstractSchemaReader */

    @Override
    protected JsonSchema readSchema() {
        JsonSchema schema = readRootSchema();
        if (schema != null) {
            postprocess(schema);
        }
        dispatchProblems();
        return schema;
    }

    @Override
    protected JsonLocation getLocation() {
        return parser.getLocation();
    }

    @Override
    protected void closeParser() {
        parser.close();
    }

    /* As a ProblemHandler */

    @Override
    public void handleProblems(List<Problem> problems) {
        addProblems(problems);
    }

    /* */

    /**
     * Reads the schema at the root.
     *
     * @return the schema if it exists, {@code null} otherwise.
     */
    private JsonSchema readRootSchema() {
        if (parser.hasNext()) {
            parser.next();
            return parseSchema();
        } else {
            addProblem(Message.SCHEMA_PROBLEM_EMPTY);
        }
        return null;
    }

    private ProblemBuilder createProblemBuilder(Message message) {
        JsonLocation location = parser.getLocation();
        String pointer = parser.getPointer();
        return createProblemBuilder(location, pointer).withMessage(message);
    }

    private void addProblem(Message message) {
        addProblem(createProblemBuilder(message));
    }

    private void postprocess(JsonSchema schema) {
        Map<URI, JsonSchema> schemaMap = generateSchemaMap(
                schema, this.initialBaseUri);
        resolveAllReferences(schemaMap);
        checkInfiniteRecursiveLoop();
    }

    private Map<URI, JsonSchema> generateSchemaMap(JsonSchema root, URI baseUri) {
        Map<URI, JsonSchema> schemaMap = new HashMap<>();

        if (root instanceof Resolvable) {
            ((Resolvable) root).resolve(baseUri);
        }

        for (JsonSchema schema : this.identifiedSchemas) {
            schemaMap.put(URIs.withFragment(schema.id()), schema);
        }

        if (!this.identifiedSchemas.contains(root)) {
            schemaMap.put(URIs.withFragment(baseUri), root);
        }

        return schemaMap;
    }

    private void resolveAllReferences(Map<URI, JsonSchema> schemaMap) {
        while (!this.referencesToResolve.isEmpty()) {
            Reference reference = this.referencesToResolve.removeFirst();
            SchemaReference referencingSchema = reference.reference;
            URI targetId = referencingSchema.getTargetId();
            JsonSchema schema = dereferenceSchema(targetId, schemaMap);
            if (schema != null) {
                referencingSchema.setReferencedSchema(schema);
                this.referenceResolved.add(reference);
            } else {
                addProblem(createProblemBuilder(reference.location, reference.pointer)
                        .withMessage(Message.SCHEMA_PROBLEM_REFERENCE)
                        .withParameter("ref", referencingSchema.ref())
                        .withParameter("targetId", targetId));
            }
        }
    }

    private JsonSchema dereferenceSchema(URI ref, Map<URI, JsonSchema> schemaMap) {
        ref = URIs.withFragment(ref);
        String fragment = ref.getFragment();
        if (fragment.startsWith("/")) {
            URI id = URIs.withEmptyFragment(ref);
            JsonSchema schema = resolveSchema(id, schemaMap);
            if (schema != null) {
                return findSubschema(schema, id, fragment);
            }
            return null;
        } else {
            return resolveSchema(ref, schemaMap);
        }
    }

    private JsonSchema resolveSchema(URI id, Map<URI, JsonSchema> schemaMap) {
        JsonSchema schema = schemaMap.get(id);
        if (schema != null) {
            return schema;
        }
        for (JsonSchemaResolver resolver : getResolvers()) {
            schema = resolver.resolveSchema(id);
            if (schema != null) {
                return schema;
            }
        }
        return null;
    }

    private JsonSchema findSubschema(JsonSchema schema, URI id, String jsonPointer) {
        JsonSchema subschema = schema.getSubschemaAt(jsonPointer);
        if (subschema == null) {
            subschema = findSubschema(schema.toJson(), jsonPointer);
            if (subschema instanceof Resolvable) {
                ((Resolvable) subschema).resolve(id);
            }
        }
        return subschema;
    }

    private JsonSchema findSubschema(JsonValue jsonValue, String jsonPointer) {
        ValueType type = jsonValue.getValueType();
        if (type == ValueType.ARRAY || type == ValueType.OBJECT) {
            JsonStructure structure = (JsonStructure) jsonValue;
            JsonValue referenced;
            try {
                referenced = structure.getValue(jsonPointer);
            } catch (JsonException e) {
                return null;
            }
            return parseValueAsSchema(referenced);
        }
        return null;
    }

    private JsonSchema parseValueAsSchema(JsonValue jsonValue) {
        switch (jsonValue.getValueType()) {
        case TRUE:
            return JsonSchema.TRUE;
        case FALSE:
            return JsonSchema.FALSE;
        case OBJECT:
            return parseObjectAsSchema(jsonValue.asJsonObject());
        default:
            return null;
        }
    }

    private JsonSchema parseObjectAsSchema(JsonObject object) {
        JsonParser valueParser = new JsonValueParser(object);
        SchemaKeywordParser parser = wrapJsonParser(valueParser, jsonService, metaschema);
        // Skips the first event START_OBJECT
        parser.next();
        return parseObjectSchema(parser);
    }

    private void checkInfiniteRecursiveLoop() {
        InfiniteLoopDetector detector = new InfiniteLoopDetector();
        for (Reference reference : this.referenceResolved) {
            SchemaReference schema = reference.reference;
            if (detector.detectInfiniteLoop(schema)) {
                addProblem(createProblemBuilder(reference.location, reference.pointer)
                        .withMessage(Message.SCHEMA_PROBLEM_REFERENCE_LOOP));
            }
        }
    }

    private SchemaKeywordParser wrapJsonParser(JsonParser realParser, JsonService jsonService,
            JsonSchema metaschema) {
        if (metaschema != null) {
            return new ValidatingKeywordParser(realParser, metaschema, jsonService.getJsonProvider());
        } else {
            return new BasicKeywordParser(realParser, jsonService.getJsonProvider());
        }
    }

    private JsonSchema parseSchema() {
        switch (parser.getCurrentEvent()) {
        case VALUE_TRUE:
            return JsonSchema.TRUE;
        case VALUE_FALSE:
            return JsonSchema.FALSE;
        case START_OBJECT:
            return parseObjectSchema();
        case START_ARRAY:
            parser.skipArray();
            return null;
        default:
            return null;
        }
    }

    private JsonSchema parseObjectSchema() {
        return parseObjectSchema(this.parser);
    }

    private JsonSchema parseObjectSchema(SchemaKeywordParser parser) {
        return new JsonSchemaParser(parser, isStrictWithKeywords()).parse();
    }

    class BasicKeywordParser extends DefaultPointerAwareJsonParser implements SchemaKeywordParser {

        BasicKeywordParser(JsonParser realParser, JsonProvider jsonProvider) {
            super(realParser, jsonProvider);
        }

        @Override
        public JsonSchema getSchema() {
            return parseSchema();
        }
    }

    class ValidatingKeywordParser extends JsonValidator implements SchemaKeywordParser {

        ValidatingKeywordParser(JsonParser realParser, JsonSchema rootSchema, JsonProvider jsonProvider) {
            super(realParser, rootSchema, jsonProvider);
        }

        @Override
        public JsonSchema getSchema() {
            return parseSchema();
        }
    }

    class JsonSchemaParser {

        private final SchemaKeywordParser parser;
        private final boolean strict;
        private final Map<String, Keyword> keywords = new LinkedHashMap<>();
        private final JsonObjectBuilder objectBuilder;
        private URI id;

        private URI ref;
        // location of ref value
        private JsonLocation location;
        // pointer of ref value
        private String pointer;

        JsonSchemaParser(SchemaKeywordParser parser, boolean strict) {
            this.parser = parser;
            this.strict = strict;
            this.objectBuilder = jsonService.createObjectBuilder();
        }

        JsonSchema parse() {
            while (parser.hasNext()) {
                Event event = parser.next();
                if (event == Event.END_OBJECT) {
                    break;
                }
                assert event == Event.KEY_NAME;

                String name = parser.getString();
                if (parser.hasNext()) {
                    KeywordType type = findKeywordType(name);

                    Keyword keyword;
                    if (type != null) {
                        keyword = parseKeyword(type);
                    } else {
                        keyword = parseUnknownKeyword(name);
                    }

                    if (keyword != null) {
                        addKeyword(name, keyword);
                    }
                }
            }

            return build();
        }

        private KeywordType findKeywordType(String name) {
            return keywordTypeMap.get(name);
        }

        private Keyword parseKeyword(KeywordType type) {
            try {
                return type.parse(parser, jsonService.getJsonBuilderFactory());
            } catch (JsonException e) {
                throw e;
            } catch (UnknownFormatAttributeException e) {
                addProblem(createProblemBuilder(Message.SCHEMA_PROBLEM_FORMAT_UNKNOWN)
                        .withParameter("attribute", e.getAttributeName()));
            } catch (Exception e) {
                // Ignores other type of exception
            }
            return null;
        }

        private Keyword parseUnknownKeyword(String name) {
            if (strict) {
                reportUnknownKeyword(name);
            }
            parser.next();
            return new UnknownKeyword(name, parser.getValue());
        }

        private void reportUnknownKeyword(String name) {
            ProblemBuilder builder = createProblemBuilder(Message.SCHEMA_PROBLEM_KEYWORD_UNKNOWN)
                    .withParameter("keyword", name);
            addProblem(builder);
        }

        private void addKeyword(String name, Keyword keyword) {
            this.keywords.put(name, keyword);
            this.objectBuilder.add(name, keyword.getValueAsJson());

            if (keyword instanceof IdKeyword) {
                this.id = ((IdKeyword) keyword).value();
            }
            if (keyword instanceof RefKeyword) {
                this.ref = ((RefKeyword) keyword).value();
                this.location = parser.getLocation();
                this.pointer = parser.getPointer();
            }
        }

        private JsonSchema build() {
            JsonSchema schema = createSchema();
            if (this.id != null) {
                identifiedSchemas.add(schema);
            }
            return schema;
        }

        private JsonSchema createSchema() {
            if (keywords.isEmpty()) {
                return JsonSchema.EMPTY;
            } else if (ref != null) {
                return createSchemaReference();
            } else {
                return BasicJsonSchema.of(this.id, objectBuilder.build(), keywords);
            }
        }

        private JsonSchema createSchemaReference() {
            SchemaReference schema = new SchemaReference(this.id, objectBuilder.build(), keywords, this.ref);
            Reference reference = createReference(schema);
            referencesToResolve.add(reference);
            return schema;
        }

        private Reference createReference(SchemaReference schema) {
            return new Reference(schema, location, pointer);
        }
    }

    /**
     * A reference to be resolved.
     *
     * @author leadpony
     */
    private static class Reference {

        final SchemaReference reference;
        final JsonLocation location;
        final String pointer;

        Reference(SchemaReference reference, JsonLocation location, String pointer) {
            this.reference = reference;
            this.location = location;
            this.pointer = pointer;
        }
    }
}
