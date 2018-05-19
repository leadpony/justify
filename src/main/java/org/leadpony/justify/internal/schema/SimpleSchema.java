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
import java.util.Optional;
import java.util.function.BinaryOperator;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.evaluator.DefaultEvaluator;

/**
 * JSON Schema without any subschemas.
 * 
 * @author leadpony
 */
public class SimpleSchema extends AbstractJsonSchema {

    protected final String title;
    protected final String description;
    protected final List<Assertion> assertions;
    protected final BinaryOperator<Evaluator> accumulator;
    
    SimpleSchema(DefaultSchemaBuilder builder) {
        this.title = builder.title();
        this.description = builder.description();
        this.assertions = builder.assertions();
        this.accumulator = Evaluator::and;
    }
    
    @Override
    public Optional<Evaluator> createEvaluator(InstanceType type) {
        Objects.requireNonNull(type, "type must not be null.");
        if (type.isContainer()) {
            return createEvaluatorForBranch(type);
        } else {
            return createEvaluatorForLeaf(type);
        }
    }

    @Override
    public void toJson(JsonGenerator generator) {
        Objects.requireNonNull(generator, "generator must not be null.");
        generator.writeStartObject();
        appendMembers(generator);
        generator.writeEnd();
    }
 
    List<Assertion> assertions() {
        return assertions;
    }
    
    protected Optional<Evaluator> createEvaluatorForBranch(InstanceType type) {
        return combineEvaluators(type).map(evaluator->{
            DefaultEvaluator wrapper= (event, parser, depth, consumer)->{
                Result result = evaluator.evaluate(event, parser, depth, consumer);
                if (depth == 0 && (event == Event.END_ARRAY || event == Event.END_OBJECT)) {
                    if (result == Result.PENDING) {
                        result = Result.TRUE;
                    }
                }
                return result;
            };
            return wrapper;
        });
    }
    
    protected Optional<Evaluator> createEvaluatorForLeaf(InstanceType type) {
        return combineEvaluators(type).map(evaluator->{
            DefaultEvaluator wrapper =  (event, parser, depth, consumer)->{
                Result result = evaluator.evaluate(event, parser, depth, consumer);
                return (result == Result.PENDING) ? Result.TRUE : result;
            };
            return wrapper;
        });
    }
    
    protected Optional<Evaluator> combineEvaluators(InstanceType type) {
        return assertions.stream()
               .filter(a->a.canApplyTo(type))
               .map(Assertion::createEvaluator)
               .reduce(accumulator);
    }

    protected void appendMembers(JsonGenerator generator) {
        if (this.title != null) {
            generator.write("title", this.title);
        }
        if (this.description != null) {
            generator.write("description", this.description);
        }
        this.assertions.forEach(assertion->assertion.toJson(generator));
    }
}
