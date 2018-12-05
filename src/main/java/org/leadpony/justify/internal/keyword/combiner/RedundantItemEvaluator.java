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

package org.leadpony.justify.internal.keyword.combiner;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * @author leadpony
 */
class RedundantItemEvaluator implements Evaluator {
    
    private final int itemIndex;
    private final JsonSchema schema;
    
    RedundantItemEvaluator(int itemIndex, JsonSchema schema) {
        assert schema.isBoolean() || schema == JsonSchema.EMPTY;
        this.itemIndex = itemIndex;
        this.schema = schema;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        Problem p = ProblemBuilderFactory.DEFAULT.createProblemBuilder(parser)
                .withMessage("instance.problem.redundant.item")
                .withParameter("index", itemIndex)
                .withSchema(schema)
                .build();
        dispatcher.dispatchProblem(p);
        return Result.FALSE;
    }
}
