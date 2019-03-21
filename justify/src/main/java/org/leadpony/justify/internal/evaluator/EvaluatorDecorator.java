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

import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.ProblemDispatcher;

/**
 * A decorator of {@link Evaluator}.
 *
 * @author leadpony
 */
public class EvaluatorDecorator extends AbstractEvaluator {

    private final Evaluator real;
    private Result finalResult;

    public EvaluatorDecorator(Evaluator evaluator, EvaluatorContext context) {
        super(context);
        this.real = evaluator;
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        if (finalResult == null) {
            Result result = real.evaluate(event, depth, dispatcher);
            if (result != Result.PENDING) {
                finalResult = result;
            }
            return result;
        }
        return finalResult;
    }

    @Override
    public boolean isAlwaysFalse() {
        return real.isAlwaysFalse();
    }
}
