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

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;

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

    public static AppendableLogicalEvaluator conjunctive(InstanceType type) {
        if (type.isContainer()) {
            return new ConjunctiveEvaluator(type);
        } else {
            return new SimpleConjunctiveEvaluator();
        }
    }

    public static AppendableLogicalEvaluator disjunctive(InstanceType type) {
        if (type.isContainer()) {
            return new DisjunctiveEvaluator(type);
        } else {
            return new SimpleDisjunctiveEvaluator();
        }
    }

    public static LogicalEvaluator exclusive(Stream<JsonSchema> children, InstanceType type) {
        return new ExclusiveEvaluator(children, type);
    }

    public static LogicalEvaluator notExclusive(Stream<JsonSchema> children, InstanceType type) {
        if (type.isContainer()) {
            return new LongNotExclusiveEvaluator(children, type);
        } else {
            return new NotExclusiveEvaluator(children, type);
        }
    }
}
