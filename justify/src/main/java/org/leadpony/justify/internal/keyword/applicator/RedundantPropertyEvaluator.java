/*
 * Copyright 2018-2020 the Justify authors.
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

package org.leadpony.justify.internal.keyword.applicator;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.RedundantElementEvaluator;

/**
 * @author leadpony
 */
class RedundantPropertyEvaluator extends RedundantElementEvaluator {

    private final String keyName;

    RedundantPropertyEvaluator(Evaluator parent, JsonSchema schema, String keyName) {
        super(parent, schema);
        this.keyName = keyName;
    }

    @Override
    public Result evaluate(Event event, int depth) {
        Problem p = newProblemBuilder()
                .withMessage(Message.INSTANCE_PROBLEM_REDUNDANT_PROPERTY)
                .withParameter("name", keyName)
                .build();
        getDispatcher().dispatchProblem(p);
        return Result.FALSE;
    }
}
