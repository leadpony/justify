/*
 * Copyright 2018-2019 the Justify authors.
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

package org.leadpony.justify.internal.problem;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;

/**
 * Default implementation of {@link ProblemDispatcher}.
 *
 * @author leadpony
 */
public interface DefaultProblemDispatcher extends ProblemDispatcher {

    /**
     * {@inheritDoc}
     */
    @Override
    default void dispatchInevitableProblem(EvaluatorContext context, JsonSchema schema) {
        requireNonNull(context, "context");
        requireNonNull(schema, "schema");
        Problem problem = ProblemBuilderFactory.DEFAULT.createProblemBuilder(context)
                .withMessage(Message.INSTANCE_PROBLEM_UNKNOWN)
                .withSchema(schema)
                .withResolvability(false)
                .build();
        dispatchProblem(problem);
    }
}
