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

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;

/**
 * {@link ConjunctionEvaluator} which can be extended.
 * 
 * @author leadpony
 */
class ExtendableConjunctionEvaluator extends LongConjunctionEvaluator 
        implements ExtendableLogicalEvaluator {
    
    public static LogicalEvaluator.Builder builder(Event finalEvent) {
        return new ExtendableConjunctionEvaluator(finalEvent);
    }

    private ExtendableConjunctionEvaluator(Event finalEvent) {
        super(finalEvent);
    }

    @Override
    public Evaluator build() {
        return this;
    }
    
    @Override
    public void extend(Evaluator evaluator) {
        append(evaluator);
    }

    @Override
    protected Result tryToMakeDecision(Event event, JsonParser parser, int depth, Reporter reporter) {
        if (depth == 0 && event == finalEvent) {
            assert isEmpty();
            return conclude(parser, reporter);
        } else {
            return Result.PENDING;
        }
    }
}
