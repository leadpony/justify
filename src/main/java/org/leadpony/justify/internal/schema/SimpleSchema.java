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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * JSON Schema without any subschemas.
 * 
 * @author leadpony
 */
public class SimpleSchema extends AbstractJsonSchema {

    private final String title;
    private final String description;
    protected final List<Assertion> assertions;
    
    SimpleSchema(DefaultSchemaBuilder builder) {
        this.title = builder.title();
        this.description = builder.description();
        this.assertions = builder.assertions();
    }
    
    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     */
    protected SimpleSchema(SimpleSchema original) {
        this.title = original.title;
        this.description = original.description;
        this.assertions = new ArrayList<>(original.assertions);
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
    protected AbstractJsonSchema createNegatedSchema() {
        return new NegatedSimpleSchema(this);
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
        if (this.title != null) {
            generator.write("title", this.title);
        }
        if (this.description != null) {
            generator.write("description", this.description);
        }
        this.assertions.forEach(assertion->assertion.toJson(generator));
    }
}
