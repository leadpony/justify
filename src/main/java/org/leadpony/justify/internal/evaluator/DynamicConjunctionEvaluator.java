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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.LinkedList;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
class DynamicConjunctionEvaluator extends ConjunctionEvaluator implements DynamicLogicalEvaluator{

    DynamicConjunctionEvaluator(Event stopEvent) {
        super(new LinkedList<>(), stopEvent);
    }

    @Override
    public void append(Evaluator evaluator) {
        requireNonNull(evaluator, "evaluator");
        children.add(evaluator);
    }

    @Override
    protected Result invokeChildEvaluator(Evaluator evaluator, Event event, JsonParser parser, int depth,
            Consumer<Problem> reporter) {
        assert depth > 0;
        return super.invokeChildEvaluator(evaluator, event, parser, depth - 1, reporter);
    }

    @Override
    protected Result tryToMakeDecision(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        if (depth == 0 && event == stopEvent) {
            assert isEmpty();
            return conclude(parser, reporter);
        } else {
            return Result.PENDING;
        }
    }
}
