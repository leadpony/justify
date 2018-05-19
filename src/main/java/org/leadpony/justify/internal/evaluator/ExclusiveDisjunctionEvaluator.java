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

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * @author leadpony
 */
class ExclusiveDisjunctionEvaluator extends DisjunctionEvaluator {
    
    private int numberOfTrueEvaluations;
    
    private ExclusiveDisjunctionEvaluator(Evaluator first, Evaluator second) {
        super(first, second);
    }
    
    static Evaluator of(Evaluator first, Evaluator second) {
        return new ExclusiveDisjunctionEvaluator(first, second);
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        Iterator<Evaluator> it = evaluators.iterator();
        while (it.hasNext()) {
            Evaluator evaluator = it.next();
            Result result = evaluator.evaluate(event, parser, depth, this);
            if (result != Result.PENDING) {
                it.remove();
                if (result == Result.TRUE) {
                    if (++numberOfTrueEvaluations > 1) {
                        return tooManyTrueEvaluations(consumer);
                    }
                }
            }
        }
        return evaluators.isEmpty() ? deliverProblems(consumer) : Result.PENDING;
    }
    
    @Override
    public Evaluator xor(Evaluator other) {
        Objects.requireNonNull(other, "other must not be null.");
        return append(other);
    }

    @Override
    protected Result deliverProblems(Consumer<Problem> consumer) {
        if (numberOfTrueEvaluations == 1) {
            return Result.TRUE;
        } else {
            return super.deliverProblems(consumer);
        }
    }
    
    private Result tooManyTrueEvaluations(Consumer<Problem> consumer) {
        Problem p = ProblemBuilder.newBuilder()
                .withMessage("instance.problem.one.of")
                .withParameter("actual", numberOfTrueEvaluations)
                .build();
        consumer.accept(p);
        return Result.FALSE;
    }
}
