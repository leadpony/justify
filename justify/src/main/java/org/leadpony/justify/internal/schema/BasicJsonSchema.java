/*
 * Copyright 2018-2020 the Justify authors.
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
import java.util.Set;

import jakarta.json.JsonObject;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.EvaluatorSource;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.evaluator.UnsupportedTypeEvaluator;
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
        List<EvaluatorSource> sources = collectEvaluatorSources(keywords);
        if (sources.isEmpty()) {
            return new None(id, json, keywords);
        } else if (sources.size() == 1) {
            return new One(id, json, keywords, sources.get(0));
        } else {
            return new Many(id, json, keywords, sources);
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

    private static List<EvaluatorSource> collectEvaluatorSources(Map<String, Keyword> keywords) {
        List<EvaluatorSource> sources = new ArrayList<>();
        for (Keyword keyword : keywords.values()) {
            keyword.getEvaluatorSource(keywords).ifPresent(sources::add);
        }
        return sources;
    }

    protected final Evaluator createEvaluatorFromKeyword(EvaluatorSource source, EvaluatorContext context,
            InstanceType type) {
        if (source.supportsType(type)) {
            return source.createEvaluator(context, type, this);
        } else {
            return Evaluator.ALWAYS_TRUE;
        }
    }

    protected final Evaluator createNegatedEvaluatorFromKeyword(EvaluatorSource source, EvaluatorContext context,
            InstanceType type) {
        if (source.supportsType(type)) {
            return source.createNegatedEvaluator(context, type, this);
        } else {
            return createUnsupportedTypeEvaluator(source, context, type);
        }
    }

    private Evaluator createUnsupportedTypeEvaluator(EvaluatorSource source, EvaluatorContext context,
            InstanceType type) {
        Set<InstanceType> supported = source.getSupportedTypes();
        Keyword keyword = source.getSourceKeyword();
        return new UnsupportedTypeEvaluator(context, this, keyword, supported, type);
    }

    /**
     * JSON Schema without any evalutable keywords.
     */
    private static final class None extends BasicJsonSchema {

        private None(URI id, JsonObject json, Map<String, Keyword> keywords) {
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
            return context.createAlwaysFalseEvaluator(this);
        }
    }

    /**
     * JSON Schema with single evalutable keyword.
     */
    private static final class One extends BasicJsonSchema {

        private final EvaluatorSource source;

        private One(URI id, JsonObject json, Map<String, Keyword> keywords,
                EvaluatorSource source) {
            super(id, json, keywords);
            this.source = source;
        }

        @Override
        public Evaluator createEvaluator(EvaluatorContext context, InstanceType type) {
            requireNonNull(type, "type");
            return createEvaluatorFromKeyword(source, context, type);
        }

        @Override
        public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            requireNonNull(type, "type");
            return createNegatedEvaluatorFromKeyword(source, context, type);
        }
    }

    /**
     * JSON Schema with multiple evalutable keywords.
     */
    private static final class Many extends BasicJsonSchema {

        private final List<EvaluatorSource> sources;

        private Many(URI id, JsonObject json, Map<String, Keyword> keywords,
                List<EvaluatorSource> sources) {
            super(id, json, keywords);
            this.sources = sources;
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
            for (EvaluatorSource source : this.sources) {
                Evaluator child = createEvaluatorFromKeyword(source, context, type);
                evaluator.append(child);
            }
            return evaluator;
        }

        private Evaluator createCombinedNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            LogicalEvaluator evaluator = Evaluators.disjunctive(context, this, null, type);
            for (EvaluatorSource source : this.sources) {
                Evaluator child = createNegatedEvaluatorFromKeyword(source, context, type);
                evaluator.append(child);
            }
            return evaluator;
        }
    }
}
