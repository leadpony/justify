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
import java.util.function.Function;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;

/**
 * Evaluator for "allOf" boolean logic.
 *
 * @author leadpony
 */
class SimpleConjunctiveEvaluator extends ArrayList<Evaluator> implements LogicalEvaluator {

    private static final long serialVersionUID = 1L;

    private final Evaluator parent;

    SimpleConjunctiveEvaluator(Evaluator parent) {
        this.parent = parent;
    }

    @Override
    public Evaluator getParent() {
        return parent;
    }

    @Override
    public Result evaluate(Event event, int depth) {
        Result finalResult = Result.TRUE;
        for (Evaluator operand : this) {
            if (operand.evaluate(event, depth) == Result.FALSE) {
                finalResult = Result.FALSE;
            }
        }
        return finalResult;
    }

    @Override
    public void append(Function<Evaluator, Evaluator> mapper) {
        Evaluator child = mapper.apply(this);
        if (child != Evaluator.ALWAYS_TRUE) {
            add(child);
        }
    }
}
