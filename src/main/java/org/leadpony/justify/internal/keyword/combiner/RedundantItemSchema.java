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

import javax.json.JsonValue;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.internal.base.ProblemBuilder;

class RedundantItemSchema implements JsonSchema {
    
    private final int itemIndex;
    
    RedundantItemSchema(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return (event, parser, depth, reporter)->{
            Problem p = ProblemBuilder.newBuilder(parser)
                    .withMessage("instance.problem.additionalItems")
                    .withParameter("index", itemIndex)
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        };
    }

    @Override
    public JsonSchema negate() {
        return JsonSchema.TRUE;
    }

    @Override
    public JsonValue toJson() {
        return JsonValue.FALSE;
    }
}