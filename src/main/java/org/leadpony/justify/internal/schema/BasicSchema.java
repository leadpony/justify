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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.AppendableLogicalEvaluator;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * JSON Schema with keywords.
 * 
 * @author leadpony
 */
public class BasicSchema extends AbstractJsonSchema implements ProblemBuilderFactory {

    private URI id;
    private final URI originalId;
    private final URI schema;
    private final List<Keyword> evaluatables;
    
    /**
     * Constructs this schema.
     * 
     * @param builder the builder of this schema.
     */
    BasicSchema(DefaultSchemaBuilder builder) {
        super(builder.getKeywordMap(), builder.getBuilderFactory());
        this.id = this.originalId = builder.getId();
        this.schema = builder.getSchema();
        this.evaluatables = builder.getKeywordMap().values().stream()
                .filter(Keyword::canEvaluate)
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
    public URI schemaId() {
        return schema;
    }

    @Override
    public Evaluator evaluator(InstanceType type, EvaluatorFactory factory, boolean affirmative) {
        requireNonNull(type, "type");
        requireNonNull(factory, "factory");
        JsonBuilderFactory builderFactory = getBuilderFactory();
        AppendableLogicalEvaluator evaluator = affirmative ? 
                Evaluators.conjunctive(type) : Evaluators.disjunctive(type);
        evaluator.withProblemBuilderFactory(this);
        for (Keyword keyword : evaluatables) {
            keyword.createEvaluator(type, evaluator, builderFactory, affirmative);
        }
        return evaluator;
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        if (this.originalId != null) {
            builder.add("$id", this.originalId.toString());
        }
        if (this.schema != null) {
            builder.add("$schema", this.schema.toString());
        }
        super.addToJson(builder);
    }
    
    @Override
    public ProblemBuilder createProblemBuilder(JsonParser parser) {
        return ProblemBuilderFactory.super.createProblemBuilder(parser)
                .withSchema(this);
    }

    public void setAbsoluteId(URI id) {
        this.id = id;
    }
}
