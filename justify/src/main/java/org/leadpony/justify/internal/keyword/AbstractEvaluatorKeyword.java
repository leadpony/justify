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
package org.leadpony.justify.internal.keyword;

import java.util.Set;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.EvaluatorSource;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordEvaluator;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.problem.ProblemBuilder;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

/**
 * @author leadpony
 */
public abstract class AbstractEvaluatorKeyword extends AbstractKeyword implements EvaluatorSource {

    protected AbstractEvaluatorKeyword(String name, JsonValue json) {
        super(name, json);
    }

    protected AbstractEvaluatorKeyword(JsonValue json) {
        super(json);
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        assert context != null;
        if (!supportsType(type)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return doCreateEvaluator(context, schema, type);
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        assert context != null;
        if (!supportsType(type)) {
            return new TypeMismatchEvaluator(context, schema, this, type);
        }
        return doCreateNegatedEvaluator(context, schema, type);
    }

    /**
     * Creates an evaluator for this keyword.
     *
     * @param context the context of the evaluator to create.
     * @param schema  the enclosing schema.
     * @param type    the type of the instance, cannot be {@code null}.
     */
    protected Evaluator doCreateEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        throw new UnsupportedOperationException(name() + " does not support evaluation.");
    }

    /**
     * Creates an evaluator for the negation of this keyword.
     *
     * @param context the context of the evaluator to create.
     * @param schema  the enclosing schema.
     * @param type    the type of the instance, cannot be {@code null}.
     */
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        throw new UnsupportedOperationException(name() + " does not support evaluation.");
    }

    /**
     * Creates an evaluator which evaluates anything as false.
     *
     * @param context the context of the evaluator to be created.
     * @param schema  the enclosing schema.
     * @return the created evaluator.
     */
    protected final Evaluator createAlwaysFalseEvaluator(EvaluatorContext context, JsonSchema schema) {
        return Evaluators.alwaysFalse(schema, context);
    }

    /**
     * An evaluator used when the type does not match the expected type.
     *
     * @author leadpony
     */
    private final class TypeMismatchEvaluator extends AbstractKeywordEvaluator {

        private final InstanceType actual;

        private TypeMismatchEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword,
                InstanceType actual) {
            super(context, schema, keyword);
            this.actual = actual;
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            Set<InstanceType> expected = getSupportedTypes();
            ProblemBuilder builder = newProblemBuilder()
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
