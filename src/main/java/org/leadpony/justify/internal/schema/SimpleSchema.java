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
import java.util.Optional;
import java.util.function.Supplier;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.evaluator.Combiner;
import org.leadpony.justify.internal.evaluator.EndCondition;
import org.leadpony.justify.internal.evaluator.Evaluators;

/**
 * JSON Schema without any subschemas.
 * 
 * @author leadpony
 */
public class SimpleSchema extends AbstractJsonSchema {

    private final String title;
    private final String description;
    protected final List<Assertion> assertions;
    private Supplier<Combiner> combiner;
    
    SimpleSchema(DefaultSchemaBuilder builder) {
        this.title = builder.title();
        this.description = builder.description();
        this.assertions = builder.assertions();
        this.combiner = Evaluators::newConjunctionCombiner;
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
        this.combiner = Evaluators::newConjunctionCombiner;
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
        appendJsonMembers(generator);
        generator.writeEnd();
    }
 
    protected Optional<Evaluator> createEvaluatorForBranch(InstanceType type) {
        Combiner combiner = createCombiner();
        combineEvaluators(type, combiner);
        combiner.withEndCondition((event, depth, empty)->
            empty ||
            (depth == 0 && (event == Event.END_ARRAY || event == Event.END_OBJECT))
        );
        return combiner.getCombined();
    }
    
    protected Optional<Evaluator> createEvaluatorForLeaf(InstanceType type) {
        Combiner combiner = createCombiner();
        combineEvaluators(type, combiner);
        combiner.withEndCondition(EndCondition.IMMEDIATE);
        return combiner.getCombined();
    }

    protected Optional<Evaluator> combineEvaluators(InstanceType type) {
        Combiner combiner = createCombiner();
        combineEvaluators(type, combiner);
        return combiner.getCombined();
    }
    
    protected void combineEvaluators(InstanceType type, Combiner combiner) {
        assertions.stream()
               .filter(a->a.canApplyTo(type))
               .map(Assertion::createEvaluator)
               .forEach(combiner::append);
    }
    
    protected Combiner createCombiner() {
        return combiner.get();
    }

    @Override
    protected AbstractJsonSchema createNegatedSchema() {
        return new SimpleSchema(this).negateSelf();
    }
    
    protected SimpleSchema negateSelf() {
        this.assertions.replaceAll(Assertion::negate);
        this.combiner = Evaluators::newInclusiveDisjunctionCombiner;
        return this;
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
