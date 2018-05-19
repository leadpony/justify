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
import org.leadpony.justify.internal.evaluator.DefaultEvaluator;

/**
 * JSON schema with any subschemas, including child schemas.
 * 
 * @author leadpony
 */
public class ComplexSchema extends SimpleSchema {
    
    private final Map<String, JsonSchema> properties;
    private final List<JsonSchema> items;
    private final List<JsonSchema> subschemas;
    
    ComplexSchema(DefaultSchemaBuilder builder) {
        super(builder);
        this.properties = builder.properties();
        this.items = builder.items();
        this.subschemas = builder.subschemas();
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
        return subschemas;
    }

    @Override
    public void toJson(JsonGenerator generator) {
        Objects.requireNonNull(generator, "generator must not be null.");
        generator.writeStartObject();
        appendMembers(generator);
        generator.writeEnd();
    }

    @Override
    protected Optional<Evaluator> createEvaluatorForBranch(InstanceType type) {
        return combineEvaluators(type).map(evaluator->{
            if (type == InstanceType.ARRAY) {
                return new ArrayVisitor(evaluator);
            } else {
                return new ObjectVisitor(evaluator);
            }
        });
    }
    
    @Override
    protected Optional<Evaluator> combineEvaluators(InstanceType type) {
        Stream<Evaluator> asssertionEvaluators = assertions.stream()
                .filter(a->a.canApplyTo(type))
                .map(Assertion::createEvaluator);
        Stream<Evaluator> subschemaEvaluators = subschemas.stream()
                .map(s->s.createEvaluator(type))
                .filter(Optional::isPresent)
                .map(Optional::get);
        return Stream.concat(asssertionEvaluators, subschemaEvaluators)
                .reduce(accumulator);
    }
    
    @Override
    protected void appendMembers(JsonGenerator generator) {
        super.appendMembers(generator);
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
    
    private abstract class Visitor implements DefaultEvaluator {
        
        private final DefaultEvaluator pendingEvaluator;
        private Result pendingResult = Result.PENDING;
        protected Evaluator evaluator;
        
        Visitor(Evaluator evaluator) {
            this.pendingEvaluator = (event, parser, depth, consumer)->{
                return this.pendingResult;
            };
            this.evaluator = accumulator.apply(evaluator, this.pendingEvaluator);
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            boolean continued = canContinue(event, depth);
            if (!continued) {
                this.pendingResult = Result.CANCELED;
            }
            if (depth == 1) {
                Evaluator child = findChild(event, parser);
                if (child != null) {
                    appendChild(child);
                }
            }
            Result result = evaluator.evaluate(event, parser, depth, consumer);
            if (continued || (result == Result.TRUE || result == Result.FALSE)) {
                return result;
            } else {
                return Result.TRUE;
            }
        }
        
        protected abstract Evaluator findChild(Event event, JsonParser parser);

        protected abstract boolean canContinue(Event event, int depth);
        
        private void appendChild(Evaluator child) {
            DefaultEvaluator wrapper = (event, parser, depth, consumer)->{
                if (depth > 0) {
                    return child.evaluate(event, parser, depth - 1, consumer);
                }
                return Result.PENDING;
            };
            this.evaluator = accumulator.apply(this.evaluator, wrapper);
        }
    }
    
    private class ArrayVisitor extends Visitor {

        private int itemIndex;

        ArrayVisitor(Evaluator evaluator) {
            super(evaluator);
        }
        
        @Override
        protected Evaluator findChild(Event event, JsonParser parser) {
            switch (event) {
            case END_ARRAY:
            case END_OBJECT:
                break;
            default:
                InstanceType type = InstanceTypes.fromEvent(event, parser); 
                JsonSchema schema = findChildSchema(itemIndex++);
                if (schema != null) {
                    return schema.createEvaluator(type).orElse(null);
                }
                break;
            }
            return null;
        }
        
        @Override
        protected boolean canContinue(Event event, int depth) {
            return depth > 0 || event != Event.END_ARRAY;
        }
    }
    
    private class ObjectVisitor extends Visitor {

        private String propertyName;

        ObjectVisitor(Evaluator evaluator) {
            super(evaluator);
        }

        @Override
        protected Evaluator findChild(Event event, JsonParser parser) {
            switch (event) {
            case KEY_NAME:
                propertyName = parser.getString();
                break;
            case END_ARRAY:
            case END_OBJECT:
                break;
            default:
                InstanceType type = InstanceTypes.fromEvent(event, parser); 
                JsonSchema schema = findChildSchema(propertyName);
                if (schema != null) {
                    return schema.createEvaluator(type).orElse(null);
                }
                break;
            }
            return null;
        }

        @Override
        protected boolean canContinue(Event event, int depth) {
            return depth > 0 || event != Event.END_OBJECT;
        }
    }
}
