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
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Utility class operating on {@link Evaluator} instances.
 * 
 * @author leadpony
 */
public interface Evaluators {

    /**
     * The evaluator which evaluates any instances as true ("valid").
     */
    Evaluator ALWAYS_TRUE = new DefaultEvaluator() {
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            return Result.TRUE;
        }
    };

    /**
     * The evaluator which evaluates any instances as false ("invalid")
     * and reports a problem.
     */
    Evaluator ALWAYS_FALSE = new DefaultEvaluator() {
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            Problem p = ProblemBuilder.newBuilder()
                    .withMessage("instance.problem.unknown")
                    .build();
            consumer.accept(p);
            return Result.FALSE;
        }
    };
}
