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

package org.leadpony.justify.internal.evaluator;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.problem.SilentProblemDispatcher;

/**
 * Combination evaluator of if/then/else.
 *
 * @author leadpony
 */
public final class ConditionalEvaluator implements Evaluator {

    private final Evaluator ifEvaluator;
    private final DeferredEvaluator thenEvaluator;
    private final DeferredEvaluator elseEvaluator;

    private Result ifResult;
    private Result thenResult;
    private Result elseResult;

    public static Evaluator of(JsonSchema ifSchema, JsonSchema thenSchema, JsonSchema elseSchema,
            Evaluator parent, InstanceType type) {

        Evaluator ifEvaluator = ifSchema.createEvaluator(parent, type);

        DeferredEvaluator thenEvaluator = new DeferredEvaluator(parent);
        thenEvaluator.setEvaluator(thenSchema != null
                ? thenSchema.createEvaluator(thenEvaluator, type)
                : Evaluator.ALWAYS_TRUE
                );

        DeferredEvaluator elseEvaluator = new DeferredEvaluator(parent);
        elseEvaluator.setEvaluator(elseSchema != null
                ? elseSchema.createEvaluator(elseEvaluator, type)
                : Evaluator.ALWAYS_TRUE
                );

        return new ConditionalEvaluator(ifEvaluator, thenEvaluator, elseEvaluator);
    }

    public static Evaluator ofNegated(JsonSchema ifSchema, JsonSchema thenSchema, JsonSchema elseSchema,
            Evaluator parent, InstanceType type) {

        Evaluator ifEvaluator = ifSchema.createEvaluator(parent, type);

        DeferredEvaluator thenEvaluator = new DeferredEvaluator(parent);
        thenEvaluator.setEvaluator(thenSchema != null
                ? thenSchema.createNegatedEvaluator(thenEvaluator, type)
                : ifSchema.createNegatedEvaluator(thenEvaluator, type)
                );

        DeferredEvaluator elseEvaluator = new DeferredEvaluator(parent);
        elseEvaluator.setEvaluator(elseSchema != null
                ? elseSchema.createNegatedEvaluator(elseEvaluator, type)
                : ifSchema.createEvaluator(elseEvaluator, type)
                );

        return new ConditionalEvaluator(ifEvaluator, thenEvaluator, elseEvaluator);
    }

    private ConditionalEvaluator(Evaluator ifEvaluator,
            DeferredEvaluator thenEvaluator,
            DeferredEvaluator elseEvaluator) {
        this.ifEvaluator = ifEvaluator;
        this.thenEvaluator = thenEvaluator;
        this.elseEvaluator = elseEvaluator;
        this.ifResult = Result.PENDING;
        this.thenResult = Result.PENDING;
        this.elseResult = Result.PENDING;
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        ifResult = updateEvaluation(ifResult, ifEvaluator, event, depth, SilentProblemDispatcher.SINGLETON);
        if (ifResult == Result.TRUE) {
            thenResult = updateEvaluation(thenResult, thenEvaluator, event, depth, dispatcher);
            if (thenResult != Result.PENDING) {
                return finalizeEvaluation(thenResult, thenEvaluator, dispatcher);
            }
        } else if (ifResult == Result.FALSE) {
            elseResult = updateEvaluation(elseResult, elseEvaluator, event, depth, dispatcher);
            if (elseResult != Result.PENDING) {
                return finalizeEvaluation(elseResult, elseEvaluator, dispatcher);
            }
        } else {
            thenResult = updateEvaluation(thenResult, thenEvaluator, event, depth, dispatcher);
            elseResult = updateEvaluation(elseResult, elseEvaluator, event, depth, dispatcher);
        }
        return Result.PENDING;
    }

    private Result updateEvaluation(Result result, Evaluator evaluator, Event event, int depth,
            ProblemDispatcher dispatcher) {
        if (result == Result.PENDING) {
            return evaluator.evaluate(event, depth, dispatcher);
        } else {
            return result;
        }
    }

    private Result finalizeEvaluation(Result result, DeferredEvaluator evaluator, ProblemDispatcher dispatcher) {
        if (result == Result.FALSE) {
            evaluator.problems().forEach(problem -> dispatcher.dispatchProblem(problem));
        }
        return result;
    }
}
