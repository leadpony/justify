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

import java.util.Objects;

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * {@link Reporter} with default implementation.
 *  
 * @author leadpony
 */
public interface ProblemReporter extends Evaluator.Reporter {
    
    /**
     * Silent reporter which ignores the problem.
     */
    ProblemReporter SILENT = problem->{};

    @Override
    default void reportUnknownProblem(JsonParser parser) {
        Objects.requireNonNull(parser, "parser must not be null.");
        Problem problem = ProblemBuilder.newBuilder(parser)
                .withMessage("instance.problem.unknown")
                .build();
        reportProblem(problem);
    }
}
