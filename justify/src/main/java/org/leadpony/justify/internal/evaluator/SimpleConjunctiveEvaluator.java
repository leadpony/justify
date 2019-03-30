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

import java.util.ArrayList;

import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;

/**
 * Evaluator for "allOf" boolean logic.
 *
 * @author leadpony
 */
class SimpleConjunctiveEvaluator extends ArrayList<Evaluator> implements LogicalEvaluator {

    private static final long serialVersionUID = 1L;

    SimpleConjunctiveEvaluator() {
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        Result finalResult = Result.TRUE;
        for (Evaluator operand : this) {
            if (operand.evaluate(event, depth, dispatcher) == Result.FALSE) {
                finalResult = Result.FALSE;
            }
        }
        return finalResult;
    }

    @Override
    public void append(Evaluator evaluator) {
        if (evaluator == Evaluator.ALWAYS_TRUE) {
            return;
        }
        add(evaluator);
    }
}
