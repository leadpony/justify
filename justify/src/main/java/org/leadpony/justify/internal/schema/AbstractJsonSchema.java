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
import org.leadpony.justify.internal.base.JsonPointerTokenizer;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.annotation.Default;

/**
 * Skeletal implementation of {@link JsonSchema}.
 *
 * @author leadpony
 */
abstract class AbstractJsonSchema implements IdentifiableJsonSchema {

    private URI id;
    private final URI originalId;
    private final URI schema;
    private final String comment;

    private final Map<String, Keyword> keywordMap;
    private final JsonBuilderFactory builderFactory;

    protected AbstractJsonSchema(JsonSchemaBuilderResult result) {
        this.keywordMap = result.getKeywords();
        this.builderFactory = result.getBuilderFactory();
        keywordMap.forEach((k, v)->v.setEnclosingSchema(this));
        this.id = this.originalId = result.getId();
        this.schema = result.getSchema();
        this.comment = result.getComment();
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
        return schema;
    }

    @Override
    public String comment() {
        return comment;
    }

    @Override
    public JsonValue defaultValue() {
        if (containsKeyword("default")) {
            Default keyword = (Default)getKeyword("default");
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
    public Stream<JsonSchema> subschemas() {
        return keywordMap.values().stream()
                .filter(Keyword::hasSubschemas)
                .flatMap(Keyword::subschemas);
    }

    @Override
    public JsonSchema subschemaAt(String jsonPointer) {
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

    @Override
    public void setAbsoluteId(URI id) {
        this.id = id;
    }

    protected Keyword getKeyword(String name) {
        return keywordMap.get(name);
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
        if (this.originalId != null) {
            builder.add("$id", this.originalId.toString());
        }
        if (this.schema != null) {
            builder.add("$schema", this.schema.toString());
        }
        if (this.comment != null) {
            builder.add("$comment", this.comment);
        }
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
                    return candidate.subschemaAt(tokenizer.remaining());
                } else {
                    return candidate;
                }
            }
        }
        return null;
    }
}
