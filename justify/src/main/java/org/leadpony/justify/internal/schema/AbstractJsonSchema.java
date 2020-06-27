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

package org.leadpony.justify.internal.schema;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.net.URI;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.json.JsonValue;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.JsonSchemaVisitor;
import org.leadpony.justify.api.keyword.ApplicatorKeyword;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.keyword.core.Comment;
import org.leadpony.justify.internal.keyword.core.Schema;
import org.leadpony.justify.internal.keyword.metadata.Default;

/**
 * Skeletal implementation of {@link JsonSchema}.
 *
 * @author leadpony
 */
abstract class AbstractJsonSchema extends AbstractMap<String, Keyword> implements ObjectJsonSchema, Resolvable {

    private URI id;
    private final JsonValue json;

    private final Map<String, Keyword> keywordMap;

    protected AbstractJsonSchema(URI id, JsonValue json, Map<String, Keyword> keywords) {
        this.id = id;
        this.json = json;
        this.keywordMap = Collections.unmodifiableMap(keywords);
        if (hasAbsoluteId()) {
            resolveSubschemas(id());
        }
    }

    /* As a JsonSchema */

    @Override
    public boolean hasId() {
        return id != null;
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public URI schema() {
        if (containsKeyword("$schema")) {
            Schema keyword = getKeyword("$schema");
            return keyword.value();
        } else {
            return null;
        }
    }

    @Override
    public String comment() {
        if (containsKeyword("$comment")) {
            Comment keyword = getKeyword("$comment");
            return keyword.value();
        } else {
            return null;
        }
    }

    @Override
    public JsonValue defaultValue() {
        if (containsKeyword("default")) {
            Default keyword = getKeyword("default");
            return keyword.value();
        } else {
            return null;
        }
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean containsKeyword(String keyword) {
        requireNonNull(keyword, "keyword");
        return keywordMap.containsKey(keyword);
    }

    @Override
    public JsonValue getKeywordValue(String keyword) {
        return getKeywordValue(keyword, null);
    }

    @Override
    public JsonValue getKeywordValue(String keyword, JsonValue defaultValue) {
        requireNonNull(keyword, "keyword");
        Keyword found = keywordMap.get(keyword);
        if (found == null) {
            return defaultValue;
        }
        return found.getValueAsJson();
    }

    @Override
    public Stream<JsonSchema> getSubschemas() {
        return keywordMap.values().stream()
                .filter(Keyword::containsSchemas)
                .flatMap(Keyword::getSchemasAsStream);
    }

    @Override
    public Stream<JsonSchema> getInPlaceSubschemas() {
        return getApplicatorKeywordsAsStream()
                .filter(ApplicatorKeyword::containsSchemas)
                .filter(keyword -> keyword.getApplicableLocation() == ApplicatorKeyword.ApplicableLocation.CURRENT)
                .flatMap(ApplicatorKeyword::getSchemasAsStream);
    }

    @Override
    public Optional<JsonSchema> findSchema(String jsonPointer) {
        requireNonNull(jsonPointer, "jsonPointer");
        if (jsonPointer.isEmpty()) {
            return Optional.of(this);
        }

        int next = jsonPointer.indexOf('/', 1);
        if (next < 0) {
            Keyword keyword = keywordMap.get(jsonPointer.substring(1));
            if (keyword != null) {
                return keyword.findSchema("");
            }
        } else {
            Keyword keyword = keywordMap.get(jsonPointer.substring(1, next));
            if (keyword != null) {
                return keyword.findSchema(jsonPointer.substring(next));
            }
        }

        return Optional.empty();
    }

    @Override
    public Map<String, JsonSchema> collectSchemas() {
        Map<String, JsonSchema> schemas = new LinkedHashMap<>();
        JsonSchemaCollector collector = new JsonSchemaCollector(schemas);
        walkSchemaTree(collector);
        return schemas;
    }

    @Override
    public Map<URI, JsonSchema> collectIdentifiedSchemas(URI baseUri) {
        requireNonNull(baseUri, "baseUri");
        Map<URI, JsonSchema> schemas = new LinkedHashMap<>();
        IdentifiedJsonSchemaCollector collector = new IdentifiedJsonSchemaCollector(baseUri, schemas);
        walkSchemaTree(collector);
        return schemas;
    }

    @Override
    public void walkSchemaTree(JsonSchemaVisitor visitor) {
        requireNonNull(visitor, "visitor");
        JsonSchemaWalker walker = new JsonSchemaWalker(visitor);
        walker.walkSchema(this);
    }

    @Override
    public final JsonValue toJson() {
        return json;
    }

    /* As an Object */

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    /* Resolvable interface */

    @Override
    public void resolve(URI baseUri) {
        if (hasAbsoluteId()) {
            return;
        } else if (hasId()) {
            this.id = baseUri.resolve(this.id);
            baseUri = this.id;
        }
        resolveSubschemas(baseUri);
    }

    public boolean hasAbsoluteId() {
        return hasId() && id().isAbsolute();
    }

    /* As a Map */

    @Override
    public int size() {
        return keywordMap.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return keywordMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return keywordMap.containsValue(value);
    }

    @Override
    public Keyword get(Object key) {
        return keywordMap.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Entry<String, Keyword>> entrySet() {
        Set<?> entrySet = this.keywordMap.entrySet();
        return (Set<Entry<String, Keyword>>) entrySet;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Keyword> T getKeyword(String name) {
        return (T) keywordMap.get(name);
    }

    private Stream<ApplicatorKeyword> getApplicatorKeywordsAsStream() {
        return keywordMap.values().stream()
                .filter(keyword -> keyword instanceof ApplicatorKeyword)
                .map(keyword -> (ApplicatorKeyword) keyword);
    }

    private void resolveSubschemas(URI baseUri) {
        getSubschemas()
                .filter(s -> !s.hasAbsoluteId())
                .filter(s -> s instanceof Resolvable)
                .forEach(s -> ((Resolvable) s).resolve(baseUri));
    }
}
