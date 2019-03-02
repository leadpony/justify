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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.annotation.Description;
import org.leadpony.justify.internal.keyword.annotation.Title;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * JSON Schema with keywords.
 *
 * @author leadpony
 */
public abstract class BasicSchema extends AbstractJsonSchema implements ProblemBuilderFactory {

    static JsonSchema newSchema(JsonSchemaBuilderResult result) {
        List<Keyword> evaluatables = collectEvaluatables(result.getKeywords());
        if (evaluatables.isEmpty()) {
            return new None(result);
        } else if (evaluatables.size() == 1) {
            return new One(result, evaluatables.get(0));
        } else {
            return new Many(result, evaluatables);
        }
    }

    /**
     * Constructs this schema.
     *
     * @param result the result of the schema builder.
     */
    protected BasicSchema(JsonSchemaBuilderResult result) {
        super(result);
    }

    @Override
    public String title() {
        if (!containsKeyword("title")) {
            return null;
        }
        Title keyword = (Title) getKeyword("title");
        return keyword.value();
    }

    @Override
    public String description() {
        if (!containsKeyword("description")) {
            return null;
        }
        Description keyword = (Description) getKeyword("description");
        return keyword.value();
    }

    @Override
    public ProblemBuilder createProblemBuilder(JsonParser parser) {
        return ProblemBuilderFactory.super.createProblemBuilder(parser)
                .withSchema(this);
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

        private None(JsonSchemaBuilderResult result) {
            super(result);
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

        private One(JsonSchemaBuilderResult result, Keyword evaluatable) {
            super(result);
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

        private Many(JsonSchemaBuilderResult result, List<Keyword> evaluatables) {
            super(result);
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
