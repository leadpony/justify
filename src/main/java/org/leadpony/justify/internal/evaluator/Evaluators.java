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

package org.leadpony.justify.internal.evaluator;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * @author leadpony
 */
public final class Evaluators {
     
    /**
     * The evaluator which evaluates any JSON schema as true ("valid").
     */
    public static final Evaluator ALWAYS_TRUE = (event, parser, depth, reporter)->Result.TRUE;

    /**
     * The evaluator whose result should be ignored.
     */
    public static final Evaluator ALWAYS_IGNORED = (event, parser, depth, reporter)->Result.IGNORED;
    
    private Evaluators() {
    }

    public static Evaluator alwaysFalse(JsonSchema schema) {
        requireNonNull(schema, "schema");
        return new NegativeEvaluator(schema);
    }
    
    public static LogicalEvaluator.Builder newConjunctionEvaluatorBuilder(InstanceType type) {
        return ConjunctionEvaluator.builder(type);
    }
    
    public static LogicalEvaluator.Builder newDisjunctionEvaluatorBuilder(InstanceType type) {
        return DisjunctionEvaluator.builder(type);
    }

    public static LogicalEvaluator.Builder newExclusiveDisjunctionEvaluatorBuilder(InstanceType type) {
        return ExclusiveDisjunctionEvaluator.builder(type);
    }

    private static final DefaultFactory DEFAULT_FACTORY = new DefaultFactory();

    /**
     * Returns the implementation of {@link JsonSchema.EvaluatorFactory}.
     * 
     * @return the evaluator factory.
     */
    public static JsonSchema.EvaluatorFactory asFactory() {
        return DEFAULT_FACTORY;
    }

    /**
     * The implementation of {@link JsonSchema.EvaluatorFactory}.
     * 
     * @author leadpony
     */
    private static class DefaultFactory implements JsonSchema.EvaluatorFactory {

        @Override
        public Evaluator alwaysTrue() {
            return ALWAYS_TRUE;
        }

        @Override
        public Evaluator alwaysFalse(JsonSchema schema) {
            return new NegativeEvaluator(schema);
        }
        
    }
    
    private static class NegativeEvaluator implements Evaluator {
        
        private final JsonSchema schema;
        
        NegativeEvaluator(JsonSchema schema) {
            this.schema = schema;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
            ProblemBuilder builder = ProblemBuilderFactory.DEFAULT.createProblemBuilder(parser)
                    .withMessage("instance.problem.unknown")
                    .withSchema(schema);
            reporter.accept(builder.build());
            return Result.FALSE;
        }
    }
}
