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

package org.leadpony.justify.internal.keyword;

import java.util.Set;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.Evaluators;

/**
 * Skeletal implementation of {@link Keyword}.
 * 
 * @author leadpony
 */
public abstract class AbstractKeyword implements Keyword, ProblemBuilderFactory {

    // the schema enclosing this keyword.
    private JsonSchema schema;
    
    @Override
    public JsonSchema getEnclosingSchema() {
        return schema;
    }

    @Override
    public void setEnclosingSchema(JsonSchema schema) {
        this.schema = schema;
    }
    
    @Override
    public Evaluator createEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        if (!supportsType(type)) {
            return createAlwaysTrueEvaluator();
        }
        return doCreateEvaluator(type, builderFactory);
    }

    @Override
    public Evaluator createNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        if (!supportsType(type)) {
            return new TypeMismatchEvaluator(type);
        }
        return doCreateNegatedEvaluator(type, builderFactory);
    }
    
    /**
     * Creates an evaluator for this keyword.
     * @param type the type of the instance, cannot be {@code null}.
     * @param builderFactory the factory for producing builders of JSON containers, cannot be {@code null}.
     */
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        throw new UnsupportedOperationException(name() + " does not support evaluation.");
    }

    /**
     * Creates an evaluator for the negation of this keyword.
     * @param type the type of the instance, cannot be {@code null}.
     * @param builderFactory the factory for producing builders of JSON containers, cannot be {@code null}.
     */
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        throw new UnsupportedOperationException(name() + " does not support evaluation.");
    }
    
    protected Evaluator createAlwaysTrueEvaluator() {
        return Evaluators.alwaysTrue(getEnclosingSchema());
    }

    protected Evaluator createAlwaysFalseEvaluator() {
        return Evaluators.alwaysFalse(getEnclosingSchema());
    }
    
    /**
     * Creates a new instance of problem builder for this keyword.
     * 
     * @param parser the JSON parser, cannot be {@code null}.
     * @return newly created instance of {@link ProblemBuilder}.
     */
    @Override
    public ProblemBuilder createProblemBuilder(JsonParser parser) {
        return ProblemBuilderFactory.super.createProblemBuilder(parser)
                .withSchema(schema)
                .withKeyword(name());
    }
    
    private class TypeMismatchEvaluator implements Evaluator {
        
        private final InstanceType actual;
        
        private TypeMismatchEvaluator(InstanceType actual) {
            this.actual = actual;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            Set<InstanceType> expected = getSupportedTypes();
            ProblemBuilder builder = createProblemBuilder(parser)
                                    .withParameter("actual", actual);
            if (expected.size() > 1) {
                builder.withMessage("instance.problem.type.plural")
                       .withParameter("expected", expected);
            } else {
                InstanceType first = expected.iterator().next();
                builder.withMessage("instance.problem.type")
                       .withParameter("expected", first);
            }
            dispatcher.dispatchProblem(builder.build());
            return Result.FALSE;
        }
    }
}
