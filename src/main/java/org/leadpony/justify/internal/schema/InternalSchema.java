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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * JSON schema with any subschemas, including child schemas.
 * 
 * @author leadpony
 */
class InternalSchema extends LeafSchema {
    
    private final PropertySchemaFinder propertySchemaFinder;
    private final ItemSchemaFinder itemSchemaFinder;
    private final List<JsonSchema> activeSubschemas;
    private NavigableSchemaMap subschemaMap;
    
    InternalSchema(DefaultSchemaBuilder builder) {
        super(builder);
        this.propertySchemaFinder = builder.getPropertySchemaFinder();
        this.itemSchemaFinder = builder.getItemSchemaFinder();
        this.activeSubschemas = builder.getSubschemas();
        this.subschemaMap = builder.getSubschemaMap();
    }

    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     * @param negating {@code true} if this schema is negation of the original.
     */
    protected InternalSchema(InternalSchema original, boolean negating) {
        super(original, negating);
        assert negating;
        this.propertySchemaFinder = original.propertySchemaFinder.negate();
        this.itemSchemaFinder = original.itemSchemaFinder.negate();
        this.activeSubschemas = original.activeSubschemas.stream()
                .map(JsonSchema::negate)
                .collect(Collectors.toList());
        this.subschemaMap = original.subschemaMap;
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
        switch (type) {
        case ARRAY:
            return createEvaluatorForArray();
        case OBJECT:
            return createEvaluatorForObject();
        default:
            return super.createEvaluator(type);
        }
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
        return new NegatedInternalSchema(this);
    }
    
    private Evaluator createEvaluatorForArray() {
        LogicalEvaluator logical = createLogicalEvaluator(InstanceType.ARRAY, true);
        appendEvaluatorsTo(logical, InstanceType.ARRAY);
        return new ArrayWalker(logical, this.itemSchemaFinder);
    }
    
    private Evaluator createEvaluatorForObject() {
        LogicalEvaluator logical = createLogicalEvaluator(InstanceType.OBJECT, true);
        appendEvaluatorsTo(logical, InstanceType.OBJECT);
        return new ObjectWalker(logical, this.propertySchemaFinder);
    }

    @Override
    protected LogicalEvaluator appendEvaluatorsTo(LogicalEvaluator evaluator, InstanceType type) {
        super.appendEvaluatorsTo(evaluator, type);
        activeSubschemas.stream()
            .map(s->s.createEvaluator(type))
            .filter(Objects::nonNull)
            .forEach(evaluator::append);
        return evaluator;
    }
    
    @Override
    protected void appendJsonMembers(JsonGenerator generator) {
        super.appendJsonMembers(generator);
        propertySchemaFinder.toJson(generator);
        itemSchemaFinder.toJson(generator);
        activeSubschemas.forEach(schema->schema.toJson(generator));
    }
}
