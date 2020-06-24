/*
 * Copyright 2018, 2020 the Justify authors.
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
import jakarta.json.JsonObject;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.EvaluationKeyword;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.evaluator.schema.ComplexSchemaBasedEvaluator;
import org.leadpony.justify.internal.evaluator.schema.SimpleSchemaBasedEvaluator;
import org.leadpony.justify.internal.keyword.metadata.Description;
import org.leadpony.justify.internal.keyword.metadata.Title;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * JSON Schema with keywords.
 *
 * @author leadpony
 */
public abstract class BasicJsonSchema extends AbstractJsonSchema implements ProblemBuilderFactory {

    public static JsonSchema of(URI id, JsonObject json, Map<String, Keyword> keywords) {
        List<EvaluationKeyword> evaluationKeywords = collectEvaluationKeywords(keywords);
        if (evaluationKeywords.isEmpty()) {
            return new None(id, json, keywords);
        } else if (evaluationKeywords.size() == 1) {
            return new One(id, json, keywords, evaluationKeywords.get(0));
        } else {
            return new Many(id, json, keywords, evaluationKeywords);
        }
    }

    /**
     * Constructs this schema.
     *
     * @param id       the identifier of this schema, may be {@code null}.
     * @param json     the JSON representation of this schema.
     * @param keywords all keywords.
     */
    protected BasicJsonSchema(URI id, JsonObject json, Map<String, Keyword> keywords) {
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

    private static List<EvaluationKeyword> collectEvaluationKeywords(Map<String, Keyword> keywords) {
        List<EvaluationKeyword> result = new ArrayList<>();
        for (Keyword keyword : keywords.values()) {
            if (keyword.canEvaluate()) {
                result.add((EvaluationKeyword) keyword);
            }
        }
        return result;
    }

    /**
     * JSON Schema without any evalutable keywords.
     */
    private static final class None extends BasicJsonSchema {

        private None(URI id, JsonObject json, Map<String, Keyword> keywords) {
            super(id, json, keywords);
        }

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return Evaluator.alwaysFalse(parent, this);
        }
    }

    /**
     * JSON Schema with single evalutable keyword.
     */
    private static final class One extends BasicJsonSchema {

        private final EvaluationKeyword evaluationKeyword;

        private One(URI id, JsonObject json, Map<String, Keyword> keywords,
                EvaluationKeyword evaluationKeyword) {
            super(id, json, keywords);
            this.evaluationKeyword = evaluationKeyword;
        }

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return SimpleSchemaBasedEvaluator.of(evaluationKeyword, parent, this, type);
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return SimpleSchemaBasedEvaluator.ofNegated(evaluationKeyword, parent, this, type);
        }
    }

    /**
     * JSON Schema with multiple evalutable keywords.
     */
    private static final class Many extends BasicJsonSchema {

        private final List<EvaluationKeyword> evaluationKeywords;

        private Many(URI id, JsonObject json, Map<String, Keyword> keywords,
                List<EvaluationKeyword> evaluationKeywords) {
            super(id, json, keywords);
            this.evaluationKeywords = evaluationKeywords;
        }

        @Override
        public Evaluator createEvaluator(Evaluator parenet, InstanceType type) {
            requireNonNull(type, "type");
            return ComplexSchemaBasedEvaluator.of(evaluationKeywords, parenet, this, type);
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            requireNonNull(type, "type");
            return ComplexSchemaBasedEvaluator.ofNegated(evaluationKeywords, parent, this, type);
        }
    }
}
