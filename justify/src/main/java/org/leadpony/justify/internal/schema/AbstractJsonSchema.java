/*
 * Copyright 2018-2019 the Justify authors.
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
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.json.JsonPointerTokenizer;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.annotation.Default;
import org.leadpony.justify.internal.keyword.core.Comment;
import org.leadpony.justify.internal.keyword.core.Schema;

/**
 * Skeletal implementation of {@link JsonSchema}.
 *
 * @author leadpony
 */
abstract class AbstractJsonSchema implements JsonSchema, Resolvable {

    private URI id;

    private final Map<String, Keyword> keywordMap;
    private final JsonBuilderFactory builderFactory;

    protected AbstractJsonSchema(URI id, Map<String, Keyword> keywords, JsonBuilderFactory builderFactory) {
        this.keywordMap = keywords;
        this.builderFactory = builderFactory;
        keywordMap.forEach((k, v)->v.setEnclosingSchema(this));
        this.id = id;
        if (hasAbsoluteId()) {
            resolveSubschemas(id());
        }
    }

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
        JsonObjectBuilder builder = builderFactory.createObjectBuilder();
        found.addToJson(builder, builderFactory);
        return builder.build().get(keyword);
    }

    @Override
    public Stream<JsonSchema> getSubschemas() {
        return keywordMap.values().stream()
                .filter(Keyword::hasSubschemas)
                .flatMap(Keyword::getSubschemas);
    }

    @Override
    public Stream<JsonSchema> getInPlaceSubschemas() {
        return keywordMap.values().stream()
                .filter(Keyword::hasSubschemas)
                .filter(Keyword::isInPlace)
                .flatMap(Keyword::getSubschemas);
    }

    @Override
    public JsonSchema getSubschemaAt(String jsonPointer) {
        requireNonNull(jsonPointer, "jsonPointer");
        if (jsonPointer.isEmpty()) {
            return this;
        }
        return searchKeywordsForSubschema(jsonPointer);
    }

    @Override
    public JsonValue toJson() {
        JsonObjectBuilder builder = builderFactory.createObjectBuilder();
        addToJson(builder);
        return builder.build();
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

    @SuppressWarnings("unchecked")
    protected <T extends Keyword> T getKeyword(String name) {
        return (T)keywordMap.get(name);
    }

    protected Map<String, Keyword> getKeywordsAsMap() {
        return keywordMap;
    }

    /**
     * Returns the factory for producing builders of JSON instances.
     *
     * @return the JSON builder factory.
     */
    protected JsonBuilderFactory getBuilderFactory() {
        return builderFactory;
    }

    /**
     * Adds this schema to the JSON representation.
     *
     * @param builder the builder for building JSON object, never be {@code null}.
     */
    protected void addToJson(JsonObjectBuilder builder) {
        for (Keyword keyword : this.keywordMap.values()) {
            keyword.addToJson(builder, builderFactory);
        }
    }

    private JsonSchema searchKeywordsForSubschema(String jsonPointer) {
        JsonPointerTokenizer tokenizer = new JsonPointerTokenizer(jsonPointer);
        Keyword keyword = keywordMap.get(tokenizer.next());
        if (keyword != null) {
            JsonSchema candidate = keyword.getSubschema(tokenizer);
            if (candidate != null) {
                if (tokenizer.hasNext()) {
                    return candidate.getSubschemaAt(tokenizer.remaining());
                } else {
                    return candidate;
                }
            }
        }
        return null;
    }

    private void resolveSubschemas(URI baseUri) {
        getSubschemas()
            .filter(s->!s.hasAbsoluteId())
            .filter(s->s instanceof Resolvable)
            .forEach(s->((Resolvable)s).resolve(baseUri));
    }
}
