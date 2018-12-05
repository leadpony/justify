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

import java.util.stream.Stream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

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
     * Creates an evaluator which always evaluates the specified schema as true.
     * @param schema the schema to evaluate, cannot be {@code null}.
     * @return newly created evaluator. It must not be {@code null}.
     */
    public static Evaluator alwaysTrue(JsonSchema schema) {
        return new Evaluator() {
            @Override
            public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
                return Result.TRUE;
            }
            @Override
            public boolean isAlwaysTrue() {
                return true;
            }
        };
    }
    
    /**
     * Creates an evaluator which always evaluates the specified schema as false.
     * @param schema the schema to evaluate, cannot be {@code null}.
     * @return newly created evaluator. It must not be {@code null}.
     */
    public static Evaluator alwaysFalse(JsonSchema schema) {
        return new Evaluator() {
            @Override
            public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
                dispatcher.dispatchInevitableProblem(parser, schema);
                return Result.FALSE;
            }
            @Override
            public boolean isAlwaysFalse() {
                return true;
            }
        };
    }

    public static LogicalEvaluator conjunctive(InstanceType type) {
        if (type.isContainer()) {
            return new ConjunctiveEvaluator(type);
        } else {
            return new SimpleConjunctiveEvaluator();
        }
    }

    public static LogicalEvaluator disjunctive(InstanceType type) {
        if (type.isContainer()) {
            return new DisjunctiveEvaluator(type);
        } else {
            return new SimpleDisjunctiveEvaluator();
        }
    }

    public static LogicalEvaluator exclusive(InstanceType type, Stream<Evaluator> operands, Stream<Evaluator> negated) {
        if (type.isContainer()) {
            return new ExclusiveEvaluator(type, operands, negated);
        } else {
            return new SimpleExclusiveEvaluator(operands, negated);
        }
    }

    public static LogicalEvaluator notExclusive(InstanceType type) {
        if (type.isContainer()) {
            return new NotExclusiveEvaluator(type);
        } else {
            return new SimpleNotExclusiveEvaluator();
        }
    }
}
