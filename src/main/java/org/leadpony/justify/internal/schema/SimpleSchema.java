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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * JSON Schema without any subschemas including child schemas.
 * 
 * @author leadpony
 */
public class SimpleSchema extends AbstractJsonSchema {

    private URI id;
    private final URI originalId;
    private final URI schema;
    
    private final String title;
    private final String description;
    private final JsonValue defaultValue;
    
    private final List<Assertion> assertions;
    
    SimpleSchema(DefaultSchemaBuilder builder) {
        this.id = this.originalId = builder.getId();
        this.schema = builder.getSchema();
        this.title = builder.getTitle();
        this.description = builder.getDescription();
        this.defaultValue = builder.getDefault();
        this.assertions = builder.getAssertions();
    }
    
    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     * @param negating {@code true} if this schema is negation of the original.
     */
    SimpleSchema(SimpleSchema original, boolean negating) {
        assert negating;
        this.id = original.id;
        this.originalId = original.originalId;
        this.schema = original.schema;
        this.title = original.title;
        this.description = original.description;
        this.defaultValue = original.defaultValue;
        this.assertions = original.assertions.stream()
                .map(Assertion::negate)
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

    public void setAbsoluteId(URI id) {
        this.id = id;
    }
 
    @Override
    protected AbstractJsonSchema createNegatedSchema() {
        return new Negated(this);
    }

    protected void appendEvaluatorsTo(LogicalEvaluator.Builder builder, InstanceType type) {
        assertions.stream()
            .filter(a->a.canApplyTo(type))
            .map(a->a.createEvaluator(type))
            .filter(Objects::nonNull)
            .forEach(builder::append);
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
        if (this.title != null) {
            builder.add("title", this.title);
        }
        if (this.description != null) {
            builder.add("description", this.description);
        }
        if (this.defaultValue != null) {
            builder.add("default", this.defaultValue);
        }
        assertions.forEach(assertion->assertion.addToJson(builder));
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
