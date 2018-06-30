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

import javax.json.stream.JsonGenerator;

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
public class LeafSchema extends AbstractJsonSchema implements Resolvable {

    private URI id;
    private final URI originalId;
    private final URI schema;
    private final String title;
    private final String description;
    private final List<Assertion> assertions;
    
    LeafSchema(DefaultSchemaBuilder builder) {
        this.id = this.originalId = builder.getId();
        this.schema = builder.getSchema();
        this.title = builder.getTitle();
        this.description = builder.getDescription();
        this.assertions = builder.getAssertions();
    }
    
    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     * @param negating {@code true} if this schema is negation of the original.
     */
    protected LeafSchema(LeafSchema original, boolean negating) {
        assert negating;
        this.id = original.id;
        this.originalId = original.originalId;
        this.schema = original.schema;
        this.title = original.title;
        this.description = original.description;
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
        LogicalEvaluator evaluator = createLogicalEvaluator(type, false);
        return appendEvaluatorsTo(evaluator, type);
    }

    @Override
    public void toJson(JsonGenerator generator) {
        Objects.requireNonNull(generator, "generator must not be null.");
        generator.writeStartObject();
        appendJsonMembers(generator);
        generator.writeEnd();
    }
    
    @Override
    public URI resolve(URI baseURI) {
        assert this.originalId != null;
        this.id = baseURI.resolve(this.originalId);
        return id();
    }
    
    public void setAbsoluteId(URI id) {
        this.id = id;
    }
 
    @Override
    protected AbstractJsonSchema createNegatedSchema() {
        return new NegatedLeafSchema(this);
    }

    protected LogicalEvaluator appendEvaluatorsTo(LogicalEvaluator evaluator, InstanceType type) {
        assertions.stream()
            .filter(a->a.canApplyTo(type))
            .map(a->a.createEvaluator(type))
            .forEach(evaluator::append);
        return evaluator;
    }
    
    protected LogicalEvaluator createLogicalEvaluator(InstanceType type, boolean extensible) {
        return Evaluators.newConjunctionEvaluator(type, extensible);
    } 
    
    protected void appendJsonMembers(JsonGenerator generator) {
        if (this.originalId != null) {
            generator.write("$id", this.originalId.toString());
        }
        if (this.schema != null) {
            generator.write("$schema", this.schema.toString());
        }
        if (this.title != null) {
            generator.write("title", this.title);
        }
        if (this.description != null) {
            generator.write("description", this.description);
        }
        this.assertions.forEach(assertion->assertion.toJson(generator));
    }
}
