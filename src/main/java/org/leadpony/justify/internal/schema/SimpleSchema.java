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
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * JSON Schema without any subschemas including child schemas.
 * 
 * @author leadpony
 */
public class SimpleSchema extends AbstractJsonSchema {

    private URI id;
    private final URI originalId;
    private final URI schema;
    
    private final Collection<Keyword> keywords;
    
    SimpleSchema(DefaultSchemaBuilder builder) {
        super(builder.getBuilderFactory());
        this.id = this.originalId = builder.getId();
        this.schema = builder.getSchema();
        this.keywords = builder.getKeywords();
    }
    
    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     * @param negating {@code true} if this schema is negation of the original.
     */
    SimpleSchema(SimpleSchema original, boolean negating) {
        super(original);
        assert negating;
        this.id = original.id;
        this.originalId = original.originalId;
        this.schema = original.schema;
        this.keywords = original.keywords.stream()
                .map(Keyword::negate)
                .collect(Collectors.toList());
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
        return jsonPointer.isEmpty() ? this : null;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        Objects.requireNonNull(type, "type must not be null.");
        LogicalEvaluator.Builder builder = createLogicalEvaluator(type, false);
        appendEvaluatorsTo(builder, type);
        return builder.build();
    }

    @Override
    public JsonSchema negate() {
        return new Negated(this);
    }

    public void setAbsoluteId(URI id) {
        this.id = id;
    }
 
    protected void appendEvaluatorsTo(LogicalEvaluator.Builder builder, InstanceType type) {
        JsonBuilderFactory builderFactory = getBuilderFactory();
        for (Keyword keyword : this.keywords) {
            if (keyword.canEvaluate()) {
                keyword.createEvaluator(type, builder, builderFactory);
            }
        }
    }
    
    protected LogicalEvaluator.Builder createLogicalEvaluator(InstanceType type, boolean extendable) {
        return Evaluators.newConjunctionEvaluatorBuilder(type, extendable);
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
        for (Keyword keyword : this.keywords) {
            keyword.addToJson(builder, builderFactory);
        }
    }
    
    /**
     * Negated type of enclosing class.
     *  
     * @author leadpony
     */
    private static class Negated extends SimpleSchema {
        
        private Negated(SimpleSchema original) {
            super(original, true);
        }
  
        @Override
        protected LogicalEvaluator.Builder createLogicalEvaluator(InstanceType type, boolean extendable) {
            return Evaluators.newDisjunctionEvaluatorBuilder(type, extendable);
        } 
    }
}
