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
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
public class LongNotExclusiveEvaluator extends NotExclusiveEvaluator {

    private final InstanceMonitor monitor;
    
    LongNotExclusiveEvaluator(Stream<JsonSchema> children, InstanceType type) {
        super(children, type);
        this.monitor = InstanceMonitor.of(type);
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        Iterator<RetainingEvaluator> it = children.iterator();
        while (it.hasNext()) {
            RetainingEvaluator current = it.next();
            Result result = current.evaluate(event, parser, depth, reporter);
            if (result != Result.PENDING) {
                if (result == Result.FALSE) {
                    addBadEvaluator(current);
                }
                it.remove();
            }
        }
        if (monitor.isCompleted(event, depth)) {
            if (badEvaluators == null || badEvaluators.size() != 1) {
                return Result.TRUE;
            } else {
                return dispatchProblems(badEvaluators.get(0), reporter);
            }
        }
        return Result.PENDING;
    }
}
