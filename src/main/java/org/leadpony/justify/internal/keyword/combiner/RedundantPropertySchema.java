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
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Schema for redundant object properties.
 * 
 * @author leadpony
 */
class RedundantPropertySchema implements JsonSchema, Evaluator {
    
    private final String keyName;

    /**
     * Constructs this schema.
     * 
     * @param keyName the name of the property.
     */
    RedundantPropertySchema(String keyName) {
        this.keyName = keyName;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
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
    public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
        Problem p = ProblemBuilder.newBuilder(parser)
                .withMessage("instance.problem.additionalProperties")
                .withParameter("name", keyName)
                .build();
        reporter.reportProblem(p);
        return Result.FALSE;
    }
}