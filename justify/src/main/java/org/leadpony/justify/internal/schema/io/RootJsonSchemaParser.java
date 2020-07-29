/*
 * Copyright 2020 the Justify authors.
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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.IdKeyword;
import org.leadpony.justify.api.keyword.InvalidKeywordException;
import org.leadpony.justify.api.keyword.JsonSchemaReference;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.URIs;
import org.leadpony.justify.internal.base.json.JsonPointers;
import org.leadpony.justify.internal.keyword.UnrecognizedKeyword;
import org.leadpony.justify.internal.keyword.format.InvalidFormatException;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.schema.BasicJsonSchema;

import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * A parser of in-memory JSON schema.
 *
 * @author leadpony
 */
class RootJsonSchemaParser extends AbstractSubschemaParser {

    private final Map<String, KeywordType> keywordTypeMap;
    private final List<JsonSchemaResolver> resolvers;
    private final ProblemDispatcher dispatcher;

    private final boolean strictWithKeywords;
    private final boolean strictWithFormats;

    private final KeywordType idKeywordType;
    private final String idKeywordName;

    private Scope currentScope;

    private final Map<URI, JsonSchema> identifiedSchemas = new HashMap<>();
    private final LinkedList<DeferredJsonSchemaReference> references = new LinkedList<>();
    private final List<DeferredJsonSchemaReference> resolvedReferences = new ArrayList<>();

    RootJsonSchemaParser(
            Map<String, KeywordType> keywordTypeMap,
            List<JsonSchemaResolver> resolvers,
            ProblemDispatcher dispatcher,
            Map<String, Object> config
            ) {

        this.keywordTypeMap = keywordTypeMap;
        this.resolvers = resolvers;
        this.dispatcher = dispatcher;

        this.strictWithKeywords = config.get(JsonSchemaReader.STRICT_KEYWORDS) == Boolean.TRUE;
        this.strictWithFormats = config.get(JsonSchemaReader.STRICT_FORMATS) == Boolean.TRUE;

        this.idKeywordType = selectIdKeywordType(keywordTypeMap);
        this.idKeywordName = this.idKeywordType.name();
    }

    /**
     * Parses the schema at the root of the document.
     *
     * @param jsonValue the JSON value at the root of the JSON document.
     * @param baseUri the initial base URI.
     * @return parsed schema.
     */
    JsonSchema parseRoot(JsonValue jsonValue, URI baseUri) {
        JsonSchema schema = parseValueOrNull(jsonValue, baseUri, "");
        if (schema != null) {
            addRootSchemaAsIdentified(baseUri, schema);
            resolveAllReferences();
            checkInfiniteRecursiveLoop();
        }
        return schema;
    }

    /**
     * {@inheritDoc}}
     */
    @Override
    public JsonSchema parseSubschemaAt(String jsonPointer, JsonValue jsonValue) {
        requireNonNull(jsonPointer, "jsonPointer");
        requireNonNull(jsonValue, "jsonValue");
        String schemaPointer = getAbsolutePointer(jsonPointer);
        JsonSchema schema = parseValueOrNull(jsonValue, getCurrentBaseUri(), schemaPointer);
        if (schema == null) {
            throw new InvalidKeywordException("Not a schema");
        }
        return schema;
    }

    @Override
    public JsonSchemaReference parseSchemaReference(JsonValue jsonValue) {
        requireNonNull(jsonValue, "jsonValue");
        if (jsonValue.getValueType() != ValueType.STRING) {
            throw new InvalidKeywordException("Not a reference");
        }

        JsonString string = (JsonString) jsonValue;
        try {
            return createSchemaReference(new URI(string.getString()));
        } catch (URISyntaxException e) {
            throw new InvalidKeywordException("Invalid reference", e);
        }
    }

    /* */

    private JsonSchema parseValueOrNull(JsonValue jsonValue, URI baseUri, String jsonPointer) {
        switch (jsonValue.getValueType()) {
        case TRUE:
            return JsonSchema.TRUE;
        case FALSE:
            return JsonSchema.FALSE;
        case OBJECT:
            return parseObject(jsonValue.asJsonObject(), baseUri, jsonPointer);
        default:
            return null;
        }
    }

    private JsonSchema parseObject(JsonObject object, URI baseUri, String jsonPointer) {
        if (object.isEmpty()) {
            return JsonSchema.EMPTY;
        }

        Scope scope = beginScope(object, baseUri, jsonPointer);
        parseObjectInScope(object, scope);
        endScope();

        return identifySchema(baseUri, scope.buildSchema(object));
    }

    private void parseObjectInScope(JsonObject object, Scope scope) {
        final URI baseUri = scope.getBaseUri();
        for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
            String name = entry.getKey();
            if (name.equals(this.idKeywordName)) {
                continue;
            }
            scope.setCurrentKeyword(name);
            Keyword keyword = createKeyword(name, entry.getValue(), baseUri);
            scope.addKeyword(keyword);
        }
    }

    private Scope beginScope(JsonObject object, URI baseUri, String jsonPointer) {
        IdKeyword id = fetchId(object);
        Scope scope;
        if (id != null) {
            scope = new Scope(this.currentScope, baseUri, jsonPointer, id);
        } else {
            scope = new Scope(this.currentScope, baseUri, jsonPointer);
        }
        this.currentScope = scope;
        return scope;
    }

    private void endScope() {
        this.currentScope = this.currentScope.getParent();
    }

    private URI getCurrentBaseUri() {
        return this.currentScope.getBaseUri();
    }

    private String getCurrentKeywordPointer() {
        return this.currentScope.getKeywordPointer();
    }

    private String getAbsolutePointer(String jsonPointer) {
        return this.currentScope.getKeywordPointer() + jsonPointer;
    }

    private Keyword createKeyword(String name, JsonValue value, URI baseUri) {
        KeywordType type = keywordTypeMap.get(name);
        if (type != null) {
            return createKeyword(type, value);
        } else {
            return createUnrecognizedKeyword(name, value);
        }
    }

    private Keyword createKeyword(KeywordType type, JsonValue value) {
        try {
            return type.createKeyword(value, this);
        } catch (InvalidFormatException e) {
            if (this.strictWithFormats) {
                reportUnknownFormat(e.getAttributeName());
            }
            return createUnrecognizedKeyword(type.name(), value);
        } catch (InvalidKeywordException e) {
            return createUnrecognizedKeyword(type.name(), value);
        }
    }

    private Keyword createUnrecognizedKeyword(String name, JsonValue value) {
        if (this.strictWithKeywords) {
            reportUnknownKeyword(name);
        }
        return new UnrecognizedKeyword(name, value);
    }

    private IdKeyword fetchId(JsonObject object) {
        if (object.containsKey(this.idKeywordName)) {
            JsonValue value = object.get(this.idKeywordName);
            try {
                return (IdKeyword) this.idKeywordType.createKeyword(value, this);
            } catch (InvalidKeywordException e) {
                return null;
            }
        }
        return null;
    }

    private JsonSchema identifySchema(URI baseUri, JsonSchema schema) {
        final URI newBaseUri = updateBaseUri(baseUri, schema);
        if (schema.hasId()) {
            addSchemaAsIdentified(newBaseUri, schema);
        }

        schema.getAnchor().ifPresent(anchor -> {
            URI idWithAnchor = newBaseUri.resolve("#" + anchor);
            addSchemaAsIdentified(idWithAnchor, schema);
        });

        return schema;
    }

    private static URI updateBaseUri(URI baseUri, JsonSchema schema) {
        if (schema.hasId()) {
            URI id = schema.id();
            if (id.isAbsolute()) {
                return id;
            } else {
                return baseUri.resolve(id);
            }
        }
        return baseUri;
    }

    private void addSchemaAsIdentified(URI id, JsonSchema schema) {
        this.identifiedSchemas.put(URIs.removeEmptyFragment(id), schema);
    }

    private void addRootSchemaAsIdentified(URI baseUri, JsonSchema schema) {
        if (!schema.hasId()) {
            addSchemaAsIdentified(baseUri, schema);
        }
    }

    private JsonSchemaReference createSchemaReference(URI targetId) {
        DeferredJsonSchemaReference reference = new DeferredJsonSchemaReference(
                getCurrentBaseUri(), targetId, getCurrentKeywordPointer());
        this.references.add(reference);
        return reference;
    }

    private void resolveAllReferences() {
        while (!this.references.isEmpty()) {
            DeferredJsonSchemaReference reference = this.references.removeFirst();
            JsonSchema schema = resolveReference(reference);
            if (schema != null) {
                reference.setReferencedSchema(schema);
                this.resolvedReferences.add(reference);
            } else {
                reportUnresolvedReference(reference);
            }
        }
    }

    private JsonSchema resolveReference(DeferredJsonSchemaReference reference) {
        URI targetId = reference.getResolvedTargetId();
        String fragment = targetId.getFragment();
        if (fragment != null && fragment.startsWith("/")) {
            URI id = URIs.removeFragment(targetId);
            JsonSchema schema = lookUpSchema(id);
            if (schema == null) {
                return null;
            }
            return findSuschemaAt(fragment, schema, id);
        } else {
            return lookUpSchema(targetId);
        }
    }

    private JsonSchema lookUpSchema(URI id) {
        id = URIs.removeEmptyFragment(id);
        if (this.identifiedSchemas.containsKey(id)) {
            return this.identifiedSchemas.get(id);
        }
        for (JsonSchemaResolver resolver : this.resolvers) {
            JsonSchema schema = resolver.resolveSchema(id);
            if (schema != null) {
                return schema;
            }
        }
        return null;
    }

    private JsonSchema findSuschemaAt(String jsonPointer, JsonSchema schema, URI baseUri) {
        return schema.findSchema(jsonPointer).orElseGet(() -> {
            return findSuschemaAt(jsonPointer, schema.toJson(), baseUri);
        });
    }

    private JsonSchema findSuschemaAt(String jsonPointer, JsonValue jsonValue, URI baseUri) {
        ValueType type = jsonValue.getValueType();
        if (type == ValueType.ARRAY || type == ValueType.OBJECT) {
            JsonStructure structure = (JsonStructure) jsonValue;
            try {
                JsonValue targetValue = structure.getValue(jsonPointer);
                return parseValueOrNull(targetValue, baseUri, jsonPointer);
            } catch (JsonException e) {
                return null;
            }
        }
        return null;
    }

    private void checkInfiniteRecursiveLoop() {
        InfiniteLoopDetector detector = new InfiniteLoopDetector();
        for (DeferredJsonSchemaReference ref: this.resolvedReferences) {
            if (detector.detectInfiniteLoop(ref)) {
                reportInfiniteLoop(ref);
            }
        }
    }

    private void reportUnknownKeyword(String name) {
        Problem problem = new ProblemBuilder(null, null)
                .withMessage(Message.SCHEMA_PROBLEM_KEYWORD_UNKNOWN)
                .withParameter("keyword", name)
                .build();
        reportProblem(problem);
    }

    private void reportUnknownFormat(String attribute) {
        Problem problem = new ProblemBuilder(null, null)
                .withMessage(Message.SCHEMA_PROBLEM_FORMAT_UNKNOWN)
                .withParameter("attribute", attribute)
                .build();
        reportProblem(problem);
    }

    private void reportUnresolvedReference(DeferredJsonSchemaReference reference) {
        Problem problem = new ProblemBuilder(null, reference.getPointer())
                .withMessage(Message.SCHEMA_PROBLEM_REFERENCE)
                .withParameter("ref", reference.getTargetId())
                .withParameter("targetId", reference.getResolvedTargetId())
                .build();
        reportProblem(problem);
    }

    private void reportInfiniteLoop(DeferredJsonSchemaReference reference) {
        Problem problem = new ProblemBuilder(null, reference.getPointer())
                .withMessage(Message.SCHEMA_PROBLEM_REFERENCE_LOOP)
                .build();
        reportProblem(problem);
    }

    private void reportProblem(Problem problem) {
        this.dispatcher.dispatchProblem(problem);
    }

    private static KeywordType selectIdKeywordType(Map<String, KeywordType> keywordTypeMap) {
        if (keywordTypeMap.containsKey("$id")) {
            return keywordTypeMap.get("$id");
        } else {
            return keywordTypeMap.get("id");
        }
    }

    private static final class Scope {

        private final Scope parent;
        private final URI baseUri;
        private final String jsonPointer;
        private final Map<String, Keyword> keywords = new LinkedHashMap<>();
        private final IdKeyword id;

        private String currentKeyword;

        Scope(Scope parent, URI baseUri, String jsonPointer) {
            this.parent = parent;
            this.baseUri = baseUri;
            this.jsonPointer = jsonPointer;
            this.id = null;
        }

        Scope(Scope parent, URI baseUri, String jsonPointer, IdKeyword id) {
            this.parent = parent;
            this.baseUri = baseUri.resolve(id.value());
            this.jsonPointer = jsonPointer;
            this.id = id;
            addKeyword(id);
        }

        Scope getParent() {
            return parent;
        }

        URI getBaseUri() {
            return baseUri;
        }

        /**
         * Returns the JSON pointer of the current keyword.
         *
         * @return the JSON pointer of the current keyword.
         */
        String getKeywordPointer() {
            return new StringBuilder()
                .append(jsonPointer)
                .append('/')
                .append(JsonPointers.encode(currentKeyword))
                .toString();
        }

        void setCurrentKeyword(String keyword) {
            this.currentKeyword = keyword;
        }

        void addKeyword(Keyword keyword) {
            this.keywords.put(keyword.name(), keyword);
        }

        JsonSchema buildSchema(JsonObject json) {
            return BasicJsonSchema.of(json, this.keywords, this.id, this.baseUri);
        }
    }
}
