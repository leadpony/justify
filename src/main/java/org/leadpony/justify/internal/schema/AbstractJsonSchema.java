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

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.base.JsonPointerTokenizer;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * Skeletal implementation of {@link JsonSchema}.
 * 
 * @author leadpony
 */
abstract class AbstractJsonSchema implements JsonSchema {
   
    private final Map<String, Keyword> keywordMap;
    private final JsonBuilderFactory builderFactory;

    protected AbstractJsonSchema(Map<String, Keyword> keywordMap, JsonBuilderFactory builderFactory) {
        this.keywordMap = keywordMap;
        this.builderFactory = builderFactory;
    }

    protected AbstractJsonSchema(AbstractJsonSchema other, Map<String, Keyword> keywordMap) {
        this.keywordMap = keywordMap;
        this.builderFactory = other.builderFactory;
    }

    @Override
    public JsonValue toJson() {
        JsonObjectBuilder builder = builderFactory.createObjectBuilder();
        addToJson(builder);
        return builder.build();
    }
    
    @Override
    public Iterable<JsonSchema> getAllSubschemas() {
        Stream<JsonSchema> stream = keywordMap.values().stream()
                .filter(Keyword::hasSubschemas)
                .flatMap(Keyword::subschemas);
        return ()->stream.iterator();
    }
    
    @Override
    public JsonSchema getSubschema(String jsonPointer) {
        Objects.requireNonNull(jsonPointer, "jsonPointer must not be null.");
        if (jsonPointer.isEmpty()) {
            return this;
        }
        return searchKeywordsForSubschema(jsonPointer);
    }
    
    @Override
    public String toString() {
        return toJson().toString();
    }
    
    protected Map<String, Keyword> getKeywordMap() {
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
                    return candidate.getSubschema(tokenizer.remaining());
                } else {
                    return candidate;
                }
            }
        }
        return null;
    }
}
