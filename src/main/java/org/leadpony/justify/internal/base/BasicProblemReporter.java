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

import org.leadpony.justify.core.Evaluator.ProblemReporter;
import org.leadpony.justify.core.Problem;

/**
 * {@link ProblemReporter} with default implementation.
 *  
 * @author leadpony
 */
public interface BasicProblemReporter extends ProblemReporter {

    @Override
    default void reportProblem(Problem problem, JsonParser parser) {
        Objects.requireNonNull(parser, "parser must not be null.");
        if (problem == null) {
            problem = buildUnknownProblem(parser);
        }
        reportProblem(problem);
    }
    
    /**
     * Reports a problem found during the evaluation.
     * 
     * @param problem the problem to be reported, cannot be {@code null}.
     */
    void reportProblem(Problem problem);
    
    /**
     * Builds a problem when omitted.
     * 
     * @param parser the JSON parser, cannot be {@code null}.
     * @return the built problem.
     */
    default Problem buildUnknownProblem(JsonParser parser) {
        return ProblemBuilder.newBuilder(parser)
                .withMessage("instance.problem.unknown")
                .build();
    }
}
