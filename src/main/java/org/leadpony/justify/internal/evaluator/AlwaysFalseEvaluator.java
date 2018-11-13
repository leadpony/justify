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

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * Evaluator which evaluates any schemas as invalid.
 * 
 * @author leadpony
 */
class AlwaysFalseEvaluator implements Evaluator {

    private final JsonSchema schema;
    
    AlwaysFalseEvaluator(JsonSchema schema) {
        this.schema = schema;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        ProblemBuilder builder = ProblemBuilderFactory.DEFAULT.createProblemBuilder(parser)
                .withMessage("instance.problem.unknown")
                .withSchema(schema);
        dispatcher.dispatchProblem(builder.build());
        return Result.FALSE;
    }
}
