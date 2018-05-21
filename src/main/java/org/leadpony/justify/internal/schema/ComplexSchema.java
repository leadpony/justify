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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.base.InstanceTypes;
import org.leadpony.justify.internal.evaluator.AppendableEvaluator;
import org.leadpony.justify.internal.evaluator.Combiner;

/**
 * JSON schema with any subschemas, including child schemas.
 * 
 * @author leadpony
 */
public class ComplexSchema extends SimpleSchema {
    
    protected final Map<String, JsonSchema> properties;
    protected final List<JsonSchema> items;
    protected final List<JsonSchema> subschemas;
    
    ComplexSchema(DefaultSchemaBuilder builder) {
        super(builder);
        this.properties = builder.properties();
        this.items = builder.items();
        this.subschemas = builder.subschemas();
    }

    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     */
    protected ComplexSchema(ComplexSchema original) {
        super(original);
        this.properties = new HashMap<>(original.properties);
        this.items = new ArrayList<>(original.items);
        this.subschemas = new ArrayList<>(original.subschemas);
    }
    
    @Override
    public JsonSchema findChildSchema(String propertyName) {
        Objects.requireNonNull(propertyName, "propertyName must not be null.");
        return properties.get(propertyName);
    }

    @Override
    public JsonSchema findChildSchema(int itemIndex) {
        if (itemIndex < items.size()) {
            return items.get(itemIndex);
        } else {
            return null;
        }
    }
    
    @Override
    public List<JsonSchema> subschemas() {
        return Collections.unmodifiableList(subschemas);
    }
    
    @Override
    public void toJson(JsonGenerator generator) {
        Objects.requireNonNull(generator, "generator must not be null.");
        generator.writeStartObject();
        appendJsonMembers(generator);
        generator.writeEnd();
    }

    @Override
    protected Optional<Evaluator> createEvaluatorForBranch(InstanceType type) {
        Combiner combiner = createCombiner();
        combineEvaluators(type, combiner);
        combiner.withEndCondition((event, depth, empty)->
            (depth == 0 && (event == Event.END_ARRAY || event == Event.END_OBJECT))
        );
        AppendableEvaluator appendable = combiner.getAppendable();
        return Optional.of( 
                (type == InstanceType.ARRAY) ?
                    new ArrayVisitor(appendable) :
                    new ObjectVisitor(appendable)
               );
        }
    
    @Override
    protected void combineEvaluators(InstanceType type, Combiner combiner) {
        Stream<Evaluator> asssertionEvaluators = assertions.stream()
                .filter(a->a.canApplyTo(type))
                .map(Assertion::createEvaluator);
        Stream<Evaluator> subschemaEvaluators = subschemas.stream()
                .map(s->s.createEvaluator(type))
                .filter(Optional::isPresent)
                .map(Optional::get);
        Stream.concat(asssertionEvaluators, subschemaEvaluators)
                .forEach(combiner::append);
    }
   
    @Override
    protected AbstractJsonSchema createNegatedSchema() {
        return new ComplexSchema(this).negateSelf();
    }
    
    @Override
    protected ComplexSchema negateSelf() {
        super.negateSelf();
        this.properties.replaceAll((k, v)->v.negate());
        this.items.replaceAll(JsonSchema::negate);
        this.subschemas.replaceAll(JsonSchema::negate);
        return this;
    }
    
    @Override
    protected void appendJsonMembers(JsonGenerator generator) {
        super.appendJsonMembers(generator);
        if (!properties.isEmpty()) {
            generator.writeStartObject("properties");
            this.properties.forEach((name, schema)->{
                generator.writeKey(name);
                schema.toJson(generator);
            });
            generator.writeEnd();
        }
        if (!items.isEmpty()) {
            generator.writeStartArray("items");
            items.forEach(schema->schema.toJson(generator));
            generator.writeEnd();
        }
        subschemas.forEach(schema->schema.toJson(generator));
    }
 
    private abstract class Visitor implements Evaluator {
        
        private AppendableEvaluator evaluator;
        private Evaluator child;
        
        Visitor(AppendableEvaluator evaluator) {
            this.evaluator = evaluator;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            if (depth == 1) {
                update(event, parser);
            }
            return evaluator.evaluate(event, parser, depth, consumer);
        }
        
        protected abstract void update(Event event, JsonParser parser);

        private Result evaluateChild(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            return child.evaluate(event, parser, depth - 1, consumer);
        }
   
        protected void appendChild(Optional<Evaluator> child) {
            child.ifPresent(c->{
                this.child = c;
                this.evaluator.append(this::evaluateChild);
            });
        }
        
        protected void clearChild() {
            this.child = null;
        }
    }
    
    private class ArrayVisitor extends Visitor {

        private int itemIndex;

        ArrayVisitor(AppendableEvaluator evaluator) {
            super(evaluator);
        }
        
        @Override
        protected void update(Event event, JsonParser parser) {
            switch (event) {
            case END_ARRAY:
            case END_OBJECT:
                break;
            default:
                clearChild();
                InstanceType type = InstanceTypes.fromEvent(event, parser); 
                JsonSchema schema = findChildSchema(itemIndex++);
                if (schema != null) {
                    appendChild(schema.createEvaluator(type));
                }
                break;
            }
        }
    }
    
    private class ObjectVisitor extends Visitor {

        private String propertyName;

        ObjectVisitor(AppendableEvaluator evaluator) {
            super(evaluator);
        }

        @Override
        protected void update(Event event, JsonParser parser) {
            switch (event) {
            case KEY_NAME:
                propertyName = parser.getString();
                clearChild();
                break;
            case END_ARRAY:
            case END_OBJECT:
                break;
            default:
                InstanceType type = InstanceTypes.fromEvent(event, parser); 
                JsonSchema schema = findChildSchema(propertyName);
                if (schema != null) {
                    appendChild(schema.createEvaluator(type));
                }
                break;
            }
        }
    }
}
