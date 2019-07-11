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

package org.leadpony.justify.internal.keyword;

import java.util.Set;

import javax.json.JsonValue;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractEvaluator;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * Skeletal implementation of {@link SchemaKeyword}.
 *
 * @author leadpony
 */
public abstract class AbstractKeyword implements SchemaKeyword, ProblemBuilderFactory {

    /*
     * the name of this keyword.
     */
    private final String name;

    /*
     * JSON representation of this keyword.
     */
    private final JsonValue json;

    /*
     * the schema enclosing this keyword.
     */
    private JsonSchema schema;

    /**
     * Constructs this keyword.
     *
     * @param json the JSON representation of this keyword.
     */
    protected AbstractKeyword(JsonValue json) {
        this.name = guessOwnName();
        this.json = json;
    }

    /**
     * Constructs this keyword.
     *
     * @param name the name of this keyword.
     * @param json the JSON representation of this keyword.
     */
    protected AbstractKeyword(String name, JsonValue json) {
        this.name = name;
        this.json = json;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final JsonValue getValueAsJson() {
        return json;
    }

    @Override
    public JsonSchema getEnclosingSchema() {
        return schema;
    }

    @Override
    public void setEnclosingSchema(JsonSchema schema) {
        this.schema = schema;
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, InstanceType type) {
        assert context != null;
        if (!supportsType(type)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return doCreateEvaluator(context, type);
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        assert context != null;
        if (!supportsType(type)) {
            return new TypeMismatchEvaluator(context, type);
        }
        return doCreateNegatedEvaluator(context, type);
    }

    /**
     * Creates an evaluator for this keyword.
     *
     * @param context the context of the evaluator to create.
     * @param type    the type of the instance, cannot be {@code null}.
     */
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        throw new UnsupportedOperationException(name() + " does not support evaluation.");
    }

    /**
     * Creates an evaluator for the negation of this keyword.
     *
     * @param context the context of the evaluator to create.
     * @param type    the type of the instance, cannot be {@code null}.
     */
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        throw new UnsupportedOperationException(name() + " does not support evaluation.");
    }

    /**
     * Creates an evaluator which evaluates anything as false.
     *
     * @param context the context of the evaluator to be created.
     * @return the created evaluator.
     */
    protected final Evaluator createAlwaysFalseEvaluator(EvaluatorContext context) {
        return Evaluators.alwaysFalse(getEnclosingSchema(), context);
    }

    /**
     * Creates a new instance of problem builder for this keyword.
     *
     * @param context the context of the evaluator, cannot be {@code null}.
     * @return newly created instance of {@link ProblemBuilder}.
     */
    @Override
    public ProblemBuilder createProblemBuilder(EvaluatorContext context) {
        return ProblemBuilderFactory.super.createProblemBuilder(context)
                .withSchema(schema)
                .withKeyword(name());
    }

    private String guessOwnName() {
        KeywordType keywordType = getClass().getAnnotation(KeywordType.class);
        return keywordType.value();
    }

    /**
     * An evaluator used when the type does not matched to the expected.
     *
     * @author leadpony
     */
    private final class TypeMismatchEvaluator extends AbstractEvaluator {

        private final InstanceType actual;

        private TypeMismatchEvaluator(EvaluatorContext context, InstanceType actual) {
            super(context);
            this.actual = actual;
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            Set<InstanceType> expected = getSupportedTypes();
            ProblemBuilder builder = createProblemBuilder(getContext())
                    .withParameter("actual", actual);
            if (expected.size() > 1) {
                builder.withMessage(Message.INSTANCE_PROBLEM_TYPE_PLURAL)
                        .withParameter("expected", expected);
            } else {
                InstanceType first = expected.iterator().next();
                builder.withMessage(Message.INSTANCE_PROBLEM_TYPE)
                        .withParameter("expected", first);
            }
            dispatcher.dispatchProblem(builder.build());
            return Result.FALSE;
        }
    }
}
