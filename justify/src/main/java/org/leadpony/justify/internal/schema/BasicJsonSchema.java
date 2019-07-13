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

import javax.json.JsonObject;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.SchemaKeyword;
import org.leadpony.justify.internal.keyword.annotation.Description;
import org.leadpony.justify.internal.keyword.annotation.Title;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * JSON Schema with keywords.
 *
 * @author leadpony
 */
public abstract class BasicJsonSchema extends AbstractJsonSchema implements ProblemBuilderFactory {

    public static JsonSchema of(URI id, JsonObject json, Map<String, SchemaKeyword> keywords) {
        List<Evaluatable> evaluatables = collectEvaluatables(keywords);
        if (evaluatables.isEmpty()) {
            return new None(id, json, keywords);
        } else if (evaluatables.size() == 1) {
            return new One(id, json, keywords, evaluatables.get(0));
        } else {
            return new Many(id, json, keywords, evaluatables);
        }
    }

    /**
     * Constructs this schema.
     *
     * @param id       the identifier of this schema, may be {@code null}.
     * @param json     the JSON representation of this schema.
     * @param keywords all keywords.
     */
    protected BasicJsonSchema(URI id, JsonObject json, Map<String, SchemaKeyword> keywords) {
        super(id, json, keywords);
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
    public ProblemBuilder createProblemBuilder(EvaluatorContext context) {
        return ProblemBuilderFactory.super.createProblemBuilder(context)
                .withSchema(this);
    }

    private static List<Evaluatable> collectEvaluatables(Map<String, SchemaKeyword> keywords) {
        List<Evaluatable> evaluatables = new ArrayList<>();
        for (SchemaKeyword keyword : keywords.values()) {
            keyword.addToEvaluatables(evaluatables, keywords);
        }
        return evaluatables;
    }

    /**
     * JSON Schema without any evalutable keywords.
     */
    private static final class None extends BasicJsonSchema {

        private None(URI id, JsonObject json, Map<String, SchemaKeyword> keywords) {
            super(id, json, keywords);
        }

        @Override
        public Evaluator createEvaluator(EvaluatorContext context, InstanceType type) {
            requireNonNull(type, "type");
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            requireNonNull(type, "type");
            return Evaluators.alwaysFalse(this, context);
        }
    }

    /**
     * JSON Schema with single evalutable keyword.
     */
    private static final class One extends BasicJsonSchema {

        private final Evaluatable evaluatable;

        private One(URI id, JsonObject json, Map<String, SchemaKeyword> keywords,
                Evaluatable evaluatable) {
            super(id, json, keywords);
            this.evaluatable = evaluatable;
        }

        @Override
        public Evaluator createEvaluator(EvaluatorContext context, InstanceType type) {
            requireNonNull(type, "type");
            return evaluatable.createEvaluator(context, type);
        }

        @Override
        public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            requireNonNull(type, "type");
            return evaluatable.createNegatedEvaluator(context, type);
        }
    }

    /**
     * JSON Schema with multiple evalutable keywords.
     */
    private static final class Many extends BasicJsonSchema {

        private final List<Evaluatable> evaluatables;

        private Many(URI id, JsonObject json, Map<String, SchemaKeyword> keywords,
                List<Evaluatable> evaluatables) {
            super(id, json, keywords);
            this.evaluatables = evaluatables;
        }

        @Override
        public Evaluator createEvaluator(EvaluatorContext context, InstanceType type) {
            requireNonNull(type, "type");
            return createCombinedEvaluator(context, type);
        }

        @Override
        public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            requireNonNull(type, "type");
            return createCombinedNegatedEvaluator(context, type);
        }

        private Evaluator createCombinedEvaluator(EvaluatorContext context, InstanceType type) {
            LogicalEvaluator evaluator = Evaluators.conjunctive(type);
            for (Evaluatable evaluatable : this.evaluatables) {
                Evaluator child = evaluatable.createEvaluator(context, type);
                evaluator.append(child);
            }
            return evaluator;
        }

        private Evaluator createCombinedNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            LogicalEvaluator evaluator = Evaluators.disjunctive(context, type);
            evaluator.withProblemBuilderFactory(this);
            for (Evaluatable evaluatable : this.evaluatables) {
                Evaluator child = evaluatable.createNegatedEvaluator(context, type);
                evaluator.append(child);
            }
            return evaluator;
        }
    }
}
