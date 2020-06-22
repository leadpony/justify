/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.internal.evaluator.schema;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.Message;
import jakarta.json.stream.JsonParser.Event;

/**
 * An evaluator which always evaluate a value as {@code false}.
 *
 * @author leadpony
 */
public final class AlwaysFalseEvaluator extends AbstractSchemaBasedEvaluator {

    public AlwaysFalseEvaluator(Evaluator parent, JsonSchema schema, EvaluatorContext context) {
        super(parent, schema, context);
    }

    @Override
    public Result evaluate(Event event, int depth) {
        Problem problem = createProblemBuilder()
                .withMessage(Message.INSTANCE_PROBLEM_UNKNOWN)
                .withResolvability(false)
                .build();
        getDispatcher().dispatchProblem(problem);
        return Result.FALSE;
    }

    @Override
    public boolean isAlwaysFalse() {
        return true;
    }
}
