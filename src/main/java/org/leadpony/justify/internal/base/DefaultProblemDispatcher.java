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

package org.leadpony.justify.internal.base;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.ProblemDispatcher;

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
    default void dispatchInevitableProblem(JsonParser parser, JsonSchema schema) {
        requireNonNull(parser, "parser");
        Problem problem = ProblemBuilderFactory.DEFAULT.createProblemBuilder(parser)
                .withMessage("instance.problem.unknown")
                .withSchema(schema)
                .build();
        dispatchProblem(problem);
    }
}
