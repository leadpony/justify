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

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.ProblemDispatcher;

/**
 * @author leadpony
 */
public class NotExclusiveEvaluator extends SimpleNotExclusiveEvaluator {

    private final InstanceMonitor monitor;
    
    NotExclusiveEvaluator(InstanceType type) {
        this.monitor = InstanceMonitor.of(type);
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        Iterator<DeferredEvaluator> it = iterator();
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            Result result = current.evaluate(event, parser, depth, dispatcher);
            if (result != Result.PENDING) {
                if (result == Result.FALSE) {
                    addBadEvaluator(current);
                }
                it.remove();
            }
        }
        if (monitor.isCompleted(event, depth)) {
            return finalizeResult(dispatcher);
        }
        return Result.PENDING;
    }
}
