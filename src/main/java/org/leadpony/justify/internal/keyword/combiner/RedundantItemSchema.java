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

import java.util.function.Consumer;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * Schema for redundant array items.
 * 
 * @author leadpony
 */
class RedundantItemSchema implements JsonSchema, Evaluator {
    
    private final int itemIndex;
    private final ProblemBuilderFactory problemBuilderFactory;

    /**
     * Constructs this schema.
     * 
     * @param itemIndex the index of the item.
     * @param problemBuilderFactory the factory producing problem builders.
     */
    RedundantItemSchema(int itemIndex, ProblemBuilderFactory problemBuilderFactory) {
        this.itemIndex = itemIndex;
        this.problemBuilderFactory = problemBuilderFactory;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type, EvaluatorFactory evaluatorFactory) {
        return this;
    }

    @Override
    public JsonSchema negate() {
        return JsonSchema.TRUE;
    }

    @Override
    public JsonValue toJson() {
        return JsonValue.FALSE;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        Problem p = problemBuilderFactory.createProblemBuilder(parser)
                .withMessage("instance.problem.additionalItems")
                .withParameter("index", itemIndex)
                .build();
        reporter.accept(p);
        return Result.FALSE;
    }
}