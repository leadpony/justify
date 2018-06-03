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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.base.CompositeIterator;
import org.leadpony.justify.internal.base.InstanceTypes;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * JSON schema with any subschemas, including child schemas.
 * 
 * @author leadpony
 */
class InternalSchema extends LeafSchema {
    
    private final PropertySchemaFinder propertySchemaFinder;
    private final ItemSchemaFinder itemSchemaFinder;
    private final List<JsonSchema> subschemas;
    private final Map<String, JsonSchema> definitions;
    
    InternalSchema(DefaultSchemaBuilder builder) {
        super(builder);
        this.propertySchemaFinder = builder.getPropertySchemaFinder();
        this.itemSchemaFinder = builder.getItemSchemaFinder();
        this.subschemas = builder.getSubschemas();
        this.definitions = builder.getDefinitions();
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
        this.subschemas = original.subschemas.stream()
                .map(JsonSchema::negate)
                .collect(Collectors.toList());
        this.definitions = original.definitions;
    }

    @Override
    public boolean hasSubschema() {
        return true;
    }
    
    @Override
    public Iterable<JsonSchema> subschemas() {
        return ()->{
            return new CompositeIterator<JsonSchema>()
                    .add(propertySchemaFinder)
                    .add(subschemas.stream().map(JsonSchema::subschemas))
                    .add(definitions.values())
                    ;
        };
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
        return new NegatedComplexSchema(this);
    }
    
    private Evaluator createEvaluatorForArray() {
        LogicalEvaluator logical = createLogicalEvaluator(InstanceType.ARRAY, true);
        appendEvaluatorsTo(logical, InstanceType.ARRAY);
        return new ArrayEvaluator(logical);
    }
    
    private Evaluator createEvaluatorForObject() {
        LogicalEvaluator logical = createLogicalEvaluator(InstanceType.OBJECT, true);
        appendEvaluatorsTo(logical, InstanceType.OBJECT);
        return new ObjectEvaluator(logical);
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
        if (!definitions.isEmpty()) {
            generator.writeKey("definitions");
            generator.writeStartObject();
            definitions.forEach((name, schema)->{
                generator.writeKey(name);
                schema.toJson(generator);
            });
            generator.writeEnd();
        }
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
    
    private class ArrayEvaluator extends ContainerEvaluator {

        private int itemIndex;

        private ArrayEvaluator(LogicalEvaluator evaluator) {
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
    
    private class ObjectEvaluator extends ContainerEvaluator {

        private final List<JsonSchema> foundSchemas = new ArrayList<>();

        private ObjectEvaluator(LogicalEvaluator evaluator) {
            super(evaluator);
        }

        @Override
        protected void update(Event event, JsonParser parser) {
            switch (event) {
            case KEY_NAME:
                findChildSchema(parser.getString());
                break;
            case END_ARRAY:
            case END_OBJECT:
                break;
            default:
                if (!foundSchemas.isEmpty()) {
                    appendEvaluators(event, parser);
                }
                break;
            }
        }
        
        private void findChildSchema(String propertyName) {
            propertySchemaFinder.findSchema(propertyName, this.foundSchemas);
        }
        
        private void appendEvaluators(Event event, JsonParser parser) {
            InstanceType type = InstanceTypes.fromEvent(event, parser); 
            this.foundSchemas.stream()
                .map(s->s.createEvaluator(type))
                .filter(Objects::nonNull)
                .forEach(this::appendChild);
            this.foundSchemas.clear();
        }
    }
}
