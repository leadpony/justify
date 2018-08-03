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
import static org.leadpony.justify.internal.base.ParserEvents.lastEventOf;

import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * @author leadpony
 */
public final class DefaultEvaluatorFactory implements JsonSchema.EvaluatorFactory {
    
    public static final DefaultEvaluatorFactory SINGLETON = new DefaultEvaluatorFactory();
   
    /**
     * The evaluator which evaluates any JSON schema as true ("valid").
     */
    private static final Evaluator ALWAYS_TRUE = (event, parser, depth, reporter)->Result.TRUE;

    /**
     * The evaluator whose result should be ignored.
     */
    private static final Evaluator ALWAYS_IGNORED = (event, parser, depth, reporter)->Result.IGNORED;
    
    private DefaultEvaluatorFactory() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Evaluator alwaysTrue() {
        return ALWAYS_TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Evaluator alwaysFalse(JsonSchema schema) {
        requireNonNull(schema, "schema");
        return new NegativeEvaluator(schema);
    }
    
    /**
     * Returns the evaluator whose result should be ignored.
     * 
     * @return the evaluator.
     */
    public Evaluator alwaysIgnored() {
        return ALWAYS_IGNORED;
    }
    
    public LogicalEvaluator.Builder createConjunctionEvaluatorBuilder(InstanceType type) {
        return ConjunctionEvaluator.builder(type);
    }
    
    public LogicalEvaluator.Builder createDisjunctionEvaluatorBuilder(InstanceType type) {
        return DisjunctionEvaluator.builder(type);
    }

    public LogicalEvaluator.Builder createExclusiveDisjunctionEvaluatorBuilder(InstanceType type) {
        return ExclusiveDisjunctionEvaluator.builder(type);
    }

    public DynamicLogicalEvaluator createDynamicConjunctionEvaluator(InstanceType type) {
        return new DynamicConjunctionEvaluator(lastEventOf(type));
    }
    
    public DynamicLogicalEvaluator createDynamicDisjunctionEvaluator(
            InstanceType type, ProblemBuilderFactory problemBuilderFactory) {
        return new DynamicDisjunctionEvaluator(lastEventOf(type), problemBuilderFactory);
    }
    
    private static class NegativeEvaluator implements Evaluator {
        
        private final JsonSchema schema;
        
        NegativeEvaluator(JsonSchema schema) {
            this.schema = schema;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
            Problem p = ProblemBuilderFactory.DEFAULT.createProblemBuilder(parser)
                    .withMessage("instance.problem.unknown")
                    .withSchema(schema)
                    .build();
            reporter.accept(p);
            return Result.FALSE;
        }
    }
}
