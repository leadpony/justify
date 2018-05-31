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
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.base.InstanceTypes;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * JSON schema with any subschemas, including child schemas.
 * 
 * @author leadpony
 */
public class ComplexSchema extends SimpleSchema {
    
    protected final PropertySchemaFinder propertySchemaFinder;
    protected final ItemSchemaFinder itemSchemaFinder;
    protected final List<JsonSchema> subschemas;
    
    ComplexSchema(DefaultSchemaBuilder builder) {
        super(builder);
        this.propertySchemaFinder = builder.getPropertySchemaFinder();
        this.itemSchemaFinder = builder.getItemSchemaFinder();
        this.subschemas = builder.subschemas();
    }

    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     * @param negating {@code true} if this schema is negation of the original.
     */
    protected ComplexSchema(ComplexSchema original, boolean negating) {
        super(original, negating);
        assert negating;
        this.propertySchemaFinder = original.propertySchemaFinder.negate();
        this.itemSchemaFinder = original.itemSchemaFinder.negate();
        this.subschemas = original.subschemas.stream()
                .map(JsonSchema::negate)
                .collect(Collectors.toList());
    }
    
    @Override
    public Evaluator createEvaluator(InstanceType type) {
        Objects.requireNonNull(type, "type must not be null.");
        if (type.isContainer()) {
            return createEvaluatorForBranch(type);
        } else {
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
        return new NegatedComplexSchema(this);
    }
    
    private Evaluator createEvaluatorForBranch(InstanceType type) {
        LogicalEvaluator logical = createLogicalEvaluator(type, true);
        appendEvaluatorsTo(logical, type);
        return  (type == InstanceType.ARRAY) ?
                new ArrayVisitor(logical) : new ObjectVisitor(logical);
    }
    
    @Override
    protected LogicalEvaluator appendEvaluatorsTo(LogicalEvaluator evaluator, InstanceType type) {
        super.appendEvaluatorsTo(evaluator, type);
        subschemas.stream()
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
        subschemas.forEach(schema->schema.toJson(generator));
    }
 
    /**
     * Finds child schema to be applied to the specified property in object.
     * 
     * @param propertyName the name of the property, cannot be {@code null}.
     * @return the child schema if found , {@code null} otherwise.
     */
    private JsonSchema findChildSchema(String propertyName) {
        Objects.requireNonNull(propertyName, "propertyName must not be null.");
        return propertySchemaFinder.findSchema(propertyName);
    }

    /**
     * Finds child schema to be applied to the specified item in array.
     * 
     * @param itemIndex the index of the item.
     * @return the child schema if found , {@code null} otherwise.
     */
    private JsonSchema findChildSchema(int itemIndex) {
        return itemSchemaFinder.findSchema(itemIndex);
    }
    
    private class ArrayVisitor extends ContainerVisitor {

        private int itemIndex;

        private ArrayVisitor(LogicalEvaluator evaluator) {
            super(evaluator);
        }
        
        @Override
        protected void update(Event event, JsonParser parser) {
            switch (event) {
            case END_ARRAY:
            case END_OBJECT:
                break;
            default:
                JsonSchema schema = findChildSchema(itemIndex++);
                if (schema != null) {
                    InstanceType type = InstanceTypes.fromEvent(event, parser); 
                    appendChild(schema.createEvaluator(type));
                }
                break;
            }
        }
    }
    
    private class ObjectVisitor extends ContainerVisitor {

        private String propertyName;

        private ObjectVisitor(LogicalEvaluator evaluator) {
            super(evaluator);
        }

        @Override
        protected void update(Event event, JsonParser parser) {
            switch (event) {
            case KEY_NAME:
                propertyName = parser.getString();
                break;
            case END_ARRAY:
            case END_OBJECT:
                break;
            default:
                JsonSchema schema = findChildSchema(propertyName);
                if (schema != null) {
                    InstanceType type = InstanceTypes.fromEvent(event, parser); 
                    appendChild(schema.createEvaluator(type));
                }
                break;
            }
        }
    }
}
