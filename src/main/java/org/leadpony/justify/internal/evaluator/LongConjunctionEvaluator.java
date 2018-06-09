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

import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
class LongConjunctionEvaluator extends ConjunctionEvaluator {
   
    protected final Event lastEvent;
    
    LongConjunctionEvaluator(Event lastEvent) {
        this.lastEvent = lastEvent;
    }

    @Override
    protected Result tryToMakeDecision(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        if (isEmpty()) {
            return conclude(parser, consumer);
        } else if (depth == 0 && event == lastEvent) {
            assert false;
            return conclude(parser, consumer);
        } else {
            return Result.PENDING;
        }
    }
}
