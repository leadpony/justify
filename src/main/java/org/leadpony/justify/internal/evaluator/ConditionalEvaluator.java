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
import org.leadpony.justify.internal.base.ProblemReporter;

/**
 * Combination evaluator of if/then/else.
 * 
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
                new RetainingEvaluator(thenEvaluator) : Evaluators.ALWAYS_IGNORED;
        this.elseEvaluator = (elseEvaluator != null) ?
                new RetainingEvaluator(elseEvaluator) : Evaluators.ALWAYS_IGNORED;
        this.ifResult = Result.PENDING;
        this.thenResult = Result.PENDING;
        this.elseResult = Result.PENDING;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        ifResult = updateEvaluation(ifResult, ifEvaluator, event, parser, depth, ProblemReporter.SILENT);
        if (ifResult == Result.TRUE) {
            thenResult = updateEvaluation(thenResult, thenEvaluator, event, parser, depth, reporter);
            if (thenResult != Result.PENDING) {
                return finalizeEvaluation(thenResult, thenEvaluator, parser, reporter);
            }
        } else if (ifResult == Result.FALSE) {
            elseResult = updateEvaluation(elseResult, elseEvaluator, event, parser, depth, reporter);
            if (elseResult != Result.PENDING) {
                return finalizeEvaluation(elseResult, elseEvaluator, parser, reporter);
            }
        } else {
            thenResult = updateEvaluation(thenResult, thenEvaluator, event, parser, depth, reporter);
            elseResult = updateEvaluation(elseResult, elseEvaluator, event, parser, depth, reporter);
        }
        return null;
    }
    
    private Result updateEvaluation(Result result, Evaluator evaluator, Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        if (result == Result.PENDING) {
            return evaluator.evaluate(event, parser, depth, reporter);
        } else {
            return result;
        }
    }
    
    private Result finalizeEvaluation(Result result, Evaluator evaluator, JsonParser parser, Consumer<Problem> reporter) {
        if (result == Result.FALSE) {
            ((RetainingEvaluator)evaluator).problems().forEach(problem->reporter.accept(problem));
        }
        return result;
    }
}
