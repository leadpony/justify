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

import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
public class ConditionalEvaluator implements Evaluator {

    private final Evaluator ifEvaluator;
    private final Evaluator thenEvaluator;
    private final Evaluator elseEvaluator;
    
    private Result ifResult;
    private Result thenResult;
    private Result elseResult;
    
    public ConditionalEvaluator(Evaluator ifEvaluator, Evaluator thenEvaluator, Evaluator elseEvaluator) {
        this.ifEvaluator = ifEvaluator;
        this.thenEvaluator = (thenEvaluator != null) ? 
                new StoringEvaluator(thenEvaluator) : Evaluators.ALWAYS_IGNORED;
        this.elseEvaluator = (elseEvaluator != null) ?
                new StoringEvaluator(elseEvaluator) : Evaluators.ALWAYS_IGNORED;
        this.ifResult = Result.PENDING;
        this.thenResult = Result.PENDING;
        this.elseResult = Result.PENDING;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        ifResult = updateEvaluation(ifResult, ifEvaluator, event, parser, depth, p->{});
        if (ifResult == Result.TRUE) {
            thenResult = updateEvaluation(thenResult, thenEvaluator, event, parser, depth, consumer);
            if (thenResult != Result.PENDING) {
                return finalizeEvaluation(thenResult, thenEvaluator, consumer);
            }
        } else if (ifResult == Result.FALSE) {
            elseResult = updateEvaluation(elseResult, elseEvaluator, event, parser, depth, consumer);
            if (elseResult != Result.PENDING) {
                return finalizeEvaluation(elseResult, elseEvaluator, consumer);
            }
        } else {
            thenResult = updateEvaluation(thenResult, thenEvaluator, event, parser, depth, consumer);
            elseResult = updateEvaluation(elseResult, elseEvaluator, event, parser, depth, consumer);
        }
        return null;
    }
    
    private Result updateEvaluation(Result result, Evaluator evaluator, Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        if (result == Result.PENDING) {
            return evaluator.evaluate(event, parser, depth, consumer);
        } else {
            return result;
        }
    }
    
    private Result finalizeEvaluation(Result result, Evaluator evaluator, Consumer<Problem> consumer) {
        if (result == Result.FALSE) {
            ((StoringEvaluator)evaluator).problems().forEach(consumer);
        }
        return result;
    }
}
