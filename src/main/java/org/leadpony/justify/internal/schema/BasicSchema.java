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

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * JSON Schema with keywords.
 * 
 * @author leadpony
 */
public class BasicSchema extends AbstractJsonSchema {

    private URI id;
    private final URI originalId;
    private final URI schema;
    
    private final Map<String, Keyword> keywordMap;
    private final NavigableSchemaMap subschemaMap;
    
    /**
     * Constructs this schema.
     * 
     * @param builder the builder of this schema.
     */
    BasicSchema(DefaultSchemaBuilder builder) {
        super(builder.getBuilderFactory());
        this.id = this.originalId = builder.getId();
        this.schema = builder.getSchema();
        this.keywordMap = builder.getKeywordMap();
        this.subschemaMap = builder.getSubschemaMap();
    }
    
    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     * @param keywordMap the keywords of this schema.
     */
    BasicSchema(BasicSchema original, Map<String, Keyword> keywordMap) {
        super(original);
        this.id = original.id;
        this.originalId = original.originalId;
        this.schema = original.schema;
        this.keywordMap = keywordMap;
        this.subschemaMap = original.subschemaMap;
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
    public URI schemaURI() {
        return schema;
    }
    
    @Override
    public JsonSchema findSubschema(String jsonPointer) {
        Objects.requireNonNull(jsonPointer, "jsonPointer must not be null.");
        if (jsonPointer.isEmpty()) {
            return this;
        } else {
            return subschemaMap.getSchema(jsonPointer);
        }
    }
    
    @Override
    public Iterable<JsonSchema> getSubschemas() {
        return this.subschemaMap.values();
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        Objects.requireNonNull(type, "type must not be null.");
        LogicalEvaluator.Builder builder = createLogicalEvaluator(type);
        appendEvaluatorsTo(builder, type);
        return builder.build();
    }

    @Override
    public JsonSchema negate() {
        BasicSchema original = this;
        Map<String, Keyword> newMap = new LinkedHashMap<>(this.keywordMap);
        newMap.replaceAll((k, v)->v.negate());
        return new BasicSchema(original, newMap) {
            @Override
            public JsonSchema negate() {
                return original;
            }
            
            @Override
            protected LogicalEvaluator.Builder createLogicalEvaluator(InstanceType type) {
                return Evaluators.newDisjunctionEvaluatorBuilder(type);
            } 
        };
    }

    public void setAbsoluteId(URI id) {
        this.id = id;
    }
 
    protected void appendEvaluatorsTo(LogicalEvaluator.Builder builder, InstanceType type) {
        JsonBuilderFactory builderFactory = getBuilderFactory();
        for (Keyword keyword : this.keywordMap.values()) {
            if (keyword.canEvaluate()) {
                keyword.createEvaluator(type, builder, builderFactory);
            }
        }
    }
    
    protected LogicalEvaluator.Builder createLogicalEvaluator(InstanceType type) {
        return Evaluators.newConjunctionEvaluatorBuilder(type);
    } 
    
    @Override
    public void addToJson(JsonObjectBuilder builder) {
        if (this.originalId != null) {
            builder.add("$id", this.originalId.toString());
        }
        if (this.schema != null) {
            builder.add("$schema", this.schema.toString());
        }
        JsonBuilderFactory builderFactory = getBuilderFactory();
        for (Keyword keyword : this.keywordMap.values()) {
            keyword.addToJson(builder, builderFactory);
        }
    }
}
