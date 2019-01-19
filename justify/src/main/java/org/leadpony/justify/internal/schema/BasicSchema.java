/*
 * Copyright 2018-2019 the Justify authors.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * JSON Schema with keywords.
 *
 * @author leadpony
 */
public abstract class BasicSchema extends AbstractJsonSchema implements ProblemBuilderFactory {

    private URI id;
    private final URI originalId;
    private final URI schema;

    static JsonSchema newSchema(DefaultSchemaBuilder builder) {
        List<Keyword> evaluatables = collectEvaluatables(builder.getKeywordMap());
        if (evaluatables.isEmpty()) {
            return new None(builder);
        } else if (evaluatables.size() == 1) {
            return new One(builder, evaluatables.get(0));
        } else {
            return new Many(builder, evaluatables);
        }
    }

    /**
     * Constructs this schema.
     *
     * @param builder the builder of this schema.
     */
    protected BasicSchema(DefaultSchemaBuilder builder) {
        super(builder.getKeywordMap(), builder.getBuilderFactory());
        this.id = this.originalId = builder.getId();
        this.schema = builder.getSchema();
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
    public URI schema() {
        return schema;
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

    private static List<Keyword> collectEvaluatables(Map<String, Keyword> keywords) {
        List<Keyword> evaluatables = new ArrayList<>();
        for (Keyword keyword : keywords.values()) {
            keyword.addToEvaluatables(evaluatables, keywords);
        }
        return evaluatables;
    }

    /*
     * JSON Schema without any evalutable keywords.
     */
    private static class None extends BasicSchema {

        private None(DefaultSchemaBuilder builder) {
            super(builder);
        }

        @Override
        public Evaluator createEvaluator(InstanceType type) {
            requireNonNull(type, "type");
            return Evaluators.alwaysTrue(this);
        }

        @Override
        public Evaluator createNegatedEvaluator(InstanceType type) {
            requireNonNull(type, "type");
            return Evaluators.alwaysFalse(this);
        }
    }

    /*
     * JSON Schema with single evalutable keyword.
     */
    private static class One extends BasicSchema {

        private final Keyword evaluatable;

        private One(DefaultSchemaBuilder builder, Keyword evaluatable) {
            super(builder);
            this.evaluatable = evaluatable;
        }

        @Override
        public Evaluator createEvaluator(InstanceType type) {
            requireNonNull(type, "type");
            return evaluatable.createEvaluator(type, getBuilderFactory());
        }

        @Override
        public Evaluator createNegatedEvaluator(InstanceType type) {
            requireNonNull(type, "type");
            return evaluatable.createNegatedEvaluator(type, getBuilderFactory());
        }
    }

    /*
     * JSON Schema with multiple evalutable keywords.
     */
    private static class Many extends BasicSchema {

        private final List<Keyword> evaluatables;

        private Many(DefaultSchemaBuilder builder, List<Keyword> evaluatables) {
            super(builder);
            this.evaluatables = evaluatables;
        }

        @Override
        public Evaluator createEvaluator(InstanceType type) {
            requireNonNull(type, "type");
            return createCombinedEvaluator(type);
        }

        @Override
        public Evaluator createNegatedEvaluator(InstanceType type) {
            requireNonNull(type, "type");
            return createCombinedNegatedEvaluator(type);
        }

        private Evaluator createCombinedEvaluator(InstanceType type) {
            JsonBuilderFactory builderFactory = getBuilderFactory();
            LogicalEvaluator evaluator = Evaluators.conjunctive(type);
            evaluator.withProblemBuilderFactory(this);
            for (Keyword keyword : this.evaluatables) {
                Evaluator child = keyword.createEvaluator(type, builderFactory);
                evaluator.append(child);
            }
            return evaluator;
        }

        private Evaluator createCombinedNegatedEvaluator(InstanceType type) {
            JsonBuilderFactory builderFactory = getBuilderFactory();
            LogicalEvaluator evaluator = Evaluators.disjunctive(type);
            evaluator.withProblemBuilderFactory(this);
            for (Keyword keyword : this.evaluatables) {
                Evaluator child = keyword.createNegatedEvaluator(type, builderFactory);
                evaluator.append(child);
            }
            return evaluator;
        }
    }
}
