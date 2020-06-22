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

import java.util.HashMap;
import java.util.Map;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.problem.DeferredProblemDispatcher;

/**
 * Combination evaluator of if/then/else.
 *
 * @author leadpony
 */
public final class ConditionalEvaluator extends AbstractEvaluator {

    private Evaluator ifEvaluator;
    private Evaluator thenEvaluator;
    private Evaluator elseEvaluator;

    private Result ifResult;
    private Result thenResult;
    private Result elseResult;

    private Map<Evaluator, ProblemDispatcher> dispatchers;

    public static Evaluator of(JsonSchema ifSchema, JsonSchema thenSchema, JsonSchema elseSchema,
            Evaluator parent, InstanceType type) {

        ConditionalEvaluator self = new ConditionalEvaluator(parent);

        Evaluator ifEvaluator = ifSchema.createEvaluator(self, type);

        Evaluator thenEvaluator = thenSchema != null
                ? thenSchema.createEvaluator(self, type)
                : Evaluator.ALWAYS_TRUE;

        Evaluator elseEvaluator = elseSchema != null
                ? elseSchema.createEvaluator(self, type)
                : Evaluator.ALWAYS_TRUE;

        self.withChildren(ifEvaluator, thenEvaluator, elseEvaluator);
        return self;
    }

    public static Evaluator ofNegated(JsonSchema ifSchema, JsonSchema thenSchema, JsonSchema elseSchema,
            Evaluator parent, InstanceType type) {

        ConditionalEvaluator self = new ConditionalEvaluator(parent);

        Evaluator ifEvaluator = ifSchema.createEvaluator(self, type);

        Evaluator thenEvaluator = thenSchema != null
                ? thenSchema.createNegatedEvaluator(self, type)
                : ifSchema.createNegatedEvaluator(self, type);

        Evaluator elseEvaluator = elseSchema != null
                ? elseSchema.createNegatedEvaluator(self, type)
                : ifSchema.createEvaluator(self, type);

        self.withChildren(ifEvaluator, thenEvaluator, elseEvaluator);
        return self;
    }

    private ConditionalEvaluator(Evaluator parent) {
        super(parent);
        this.ifResult = Result.PENDING;
        this.thenResult = Result.PENDING;
        this.elseResult = Result.PENDING;
    }

    private void withChildren(Evaluator ifEvaluator, Evaluator thenEvaluator, Evaluator elseEvaluator) {
        this.ifEvaluator = ifEvaluator;
        this.thenEvaluator = thenEvaluator;
        this.elseEvaluator = elseEvaluator;
    }

    @Override
    public Result evaluate(Event event, int depth) {
        ifResult = updateEvaluation(ifResult, ifEvaluator, event, depth);
        if (ifResult == Result.TRUE) {
            thenResult = updateEvaluation(thenResult, thenEvaluator, event, depth);
            if (thenResult != Result.PENDING) {
                return finalizeEvaluation(thenResult, thenEvaluator);
            }
        } else if (ifResult == Result.FALSE) {
            elseResult = updateEvaluation(elseResult, elseEvaluator, event, depth);
            if (elseResult != Result.PENDING) {
                return finalizeEvaluation(elseResult, elseEvaluator);
            }
        } else {
            thenResult = updateEvaluation(thenResult, thenEvaluator, event, depth);
            elseResult = updateEvaluation(elseResult, elseEvaluator, event, depth);
        }
        return Result.PENDING;
    }

    @Override
    public ProblemDispatcher getDispatcherForChild(Evaluator evaluator) {
        if (dispatchers == null) {
            dispatchers = new HashMap<>();
        }

        ProblemDispatcher dispatcher = dispatchers.get(evaluator);
        if (dispatcher == null) {
            if (evaluator == ifEvaluator) {
                dispatcher = ProblemDispatcher.SILENT;
            } else {
                dispatcher = DeferredProblemDispatcher.empty();
            }
            dispatchers.put(evaluator, dispatcher);
        }
        return dispatcher;
    }

    private Result updateEvaluation(Result result, Evaluator evaluator, Event event, int depth) {
        if (result == Result.PENDING) {
            return evaluator.evaluate(event, depth);
        } else {
            return result;
        }
    }

    private Result finalizeEvaluation(Result result, Evaluator evaluator) {
        if (result == Result.FALSE) {
            DeferredProblemDispatcher deferred = (DeferredProblemDispatcher) dispatchers.get(evaluator);
            getDispatcher().dispatchAllProblems(deferred);
        }
        return result;
    }
}
