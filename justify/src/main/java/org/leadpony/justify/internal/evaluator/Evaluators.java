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

package org.leadpony.justify.internal.evaluator;

import java.util.stream.Stream;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;

/**
 * Provides various kinds of evaluators.
 *
 * @author leadpony
 */
public final class Evaluators {

    private Evaluators() {
    }

    /**
     * Creates an evaluator which always evaluates the specified schema as false.
     *
     * @param schema the schema to evaluate, cannot be {@code null}.
     * @param context the context of the evaluator to be created.
     * @return newly created evaluator. It must not be {@code null}.
     */
    public static Evaluator alwaysFalse(JsonSchema schema, EvaluatorContext context) {
        return new Evaluator() {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                dispatcher.dispatchInevitableProblem(context, schema);
                return Result.FALSE;
            }

            @Override
            public boolean isAlwaysFalse() {
                return true;
            }
        };
    }

    public static LogicalEvaluator conjunctive(InstanceType type) {
        switch (type) {
        case ARRAY:
            return new ConjunctiveEvaluator(Event.END_ARRAY);
        case OBJECT:
            return new ConjunctiveEvaluator(Event.END_OBJECT);
        default:
            return new SimpleConjunctiveEvaluator();
        }
    }

    public static LogicalEvaluator disjunctive(EvaluatorContext context, InstanceType type) {
        switch (type) {
        case ARRAY:
            return new DisjunctiveEvaluator(context, Event.END_ARRAY);
        case OBJECT:
            return new DisjunctiveEvaluator(context, Event.END_OBJECT);
        default:
            return new SimpleDisjunctiveEvaluator(context);
        }
    }

    public static LogicalEvaluator exclusive(EvaluatorContext context, InstanceType type, Stream<Evaluator> operands,
            Stream<Evaluator> negated) {
        switch (type) {
        case ARRAY:
            return new ExclusiveEvaluator(context, Event.END_ARRAY, operands, negated);
        case OBJECT:
            return new ExclusiveEvaluator(context, Event.END_OBJECT, operands, negated);
        default:
            return new SimpleExclusiveEvaluator(context, operands, negated);
        }
    }

    public static LogicalEvaluator notExclusive(EvaluatorContext context, InstanceType type) {
        switch (type) {
        case ARRAY:
            return new NotExclusiveEvaluator(context, Event.END_ARRAY);
        case OBJECT:
            return new NotExclusiveEvaluator(context, Event.END_OBJECT);
        default:
            return new SimpleNotExclusiveEvaluator(context);
        }
    }
}
