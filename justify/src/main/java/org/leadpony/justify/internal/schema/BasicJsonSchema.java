/*
 * Copyright 2018, 2020 the Justify authors.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.JsonSchemaVisitor;
import org.leadpony.justify.api.keyword.ApplicatorKeyword;
import org.leadpony.justify.api.keyword.EvaluationKeyword;
import org.leadpony.justify.api.keyword.IdKeyword;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.evaluator.schema.ComplexSchemaBasedEvaluator;
import org.leadpony.justify.internal.evaluator.schema.SimpleSchemaBasedEvaluator;
import org.leadpony.justify.internal.keyword.core.Anchor;
import org.leadpony.justify.internal.keyword.core.Comment;
import org.leadpony.justify.internal.keyword.core.Schema;
import org.leadpony.justify.internal.keyword.metadata.Default;
import org.leadpony.justify.internal.keyword.metadata.Description;
import org.leadpony.justify.internal.keyword.metadata.Title;

/**
 * A basic implementation of {@link JsonSchema}.
 *
 * @author leadpony
 */
public abstract class BasicJsonSchema extends AbstractMap<String, Keyword> implements ObjectJsonSchema {

    private final Map<String, Keyword> keywordMap;
    private final IdKeyword id;
    private final JsonValue json;

    private Optional<String> anchor;

    public static JsonSchema of(Map<String, Keyword> keywords, IdKeyword id, JsonObject json) {
        combineKeywords(keywords);
        List<EvaluationKeyword> evaluationKeywords = collectEvaluationKeywords(keywords);
        if (evaluationKeywords.isEmpty()) {
            return new None(keywords, id, json);
        } else if (evaluationKeywords.size() == 1) {
            return new One(keywords, id, json, evaluationKeywords.get(0));
        } else {
            return new Many(keywords, id, json, evaluationKeywords);
        }
    }

    protected BasicJsonSchema(Map<String, Keyword> keywords, IdKeyword id, JsonValue json) {
        this.keywordMap = Collections.unmodifiableMap(keywords);
        this.id = id;
        this.json = json;
    }

    /* As a JsonSchema */

    @Override
    public boolean hasId() {
        return id != null;
    }

    @Override
    public URI id() {
        return id != null ? id.value() : null;
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
    public String title() {
        if (!containsKeyword("title")) {
            return null;
        }
        Title keyword = (Title) getKeyword("title");
        return keyword.value();
    }

    @Override
    public String description() {
        if (!containsKeyword("description")) {
            return null;
        }
        Description keyword = (Description) getKeyword("description");
        return keyword.value();
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
    public Optional<String> getAnchor() {
        if (this.anchor != null) {
            return this.anchor;
        }
        Optional<Anchor> keyword = getKeyword("$anchor", Anchor.class);
        this.anchor = keyword.map(Anchor::value);
        return this.anchor;
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
        if (!hasId()) {
            schemas.put(baseUri, this);
        }
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

    protected <T extends Keyword> Optional<T> getKeyword(String name, Class<T> type) {
        if (containsKeyword(name)) {
            Keyword keyword = this.keywordMap.get(name);
            if (type.isInstance(keyword)) {
                return Optional.of(type.cast(keyword));
            }
        }
        return Optional.empty();
    }

    private Stream<ApplicatorKeyword> getApplicatorKeywordsAsStream() {
        return keywordMap.values().stream()
                .filter(keyword -> keyword instanceof ApplicatorKeyword)
                .map(keyword -> (ApplicatorKeyword) keyword);
    }

    private static void combineKeywords(Map<String, Keyword> keywords) {
        keywords.replaceAll((name, keyword) -> keyword.withKeywords(keywords));
    }

    private static List<EvaluationKeyword> collectEvaluationKeywords(Map<String, Keyword> keywords) {
        List<EvaluationKeyword> result = new ArrayList<>();
        for (Keyword keyword : keywords.values()) {
            if (keyword.canEvaluate()) {
                EvaluationKeyword evaluation = (EvaluationKeyword) keyword;
                if (evaluation.isExclusive()) {
                    result.clear();
                    result.add(evaluation);
                    break;
                }
                result.add(evaluation);
            }
        }
        return result;
    }

    /**
     * JSON Schema without any evalutable keywords.
     */
    static final class None extends BasicJsonSchema {

        None(Map<String, Keyword> keywords, IdKeyword id, JsonObject json) {
            super(keywords, id, json);
        }

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return Evaluator.alwaysFalse(parent, this);
        }
    }

    /**
     * JSON Schema with single evalutable keyword.
     */
    static final class One extends BasicJsonSchema {

        private final EvaluationKeyword evaluationKeyword;

        One(Map<String, Keyword> keywords, IdKeyword id, JsonObject json,
                EvaluationKeyword evaluationKeyword) {
            super(keywords, id, json);
            this.evaluationKeyword = evaluationKeyword;
        }

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return SimpleSchemaBasedEvaluator.of(evaluationKeyword, parent, this, type);
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return SimpleSchemaBasedEvaluator.ofNegated(evaluationKeyword, parent, this, type);
        }
    }

    /**
     * JSON Schema with multiple evalutable keywords.
     */
    static final class Many extends BasicJsonSchema {

        private final List<EvaluationKeyword> evaluationKeywords;

        Many(Map<String, Keyword> keywords, IdKeyword id, JsonObject json,
                List<EvaluationKeyword> evaluationKeywords) {
            super(keywords, id, json);
            this.evaluationKeywords = evaluationKeywords;
        }

        @Override
        public Evaluator createEvaluator(Evaluator parenet, InstanceType type) {
            requireNonNull(type, "type");
            return ComplexSchemaBasedEvaluator.of(evaluationKeywords, parenet, this, type);
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return ComplexSchemaBasedEvaluator.ofNegated(evaluationKeywords, parent, this, type);
        }
    }
}
