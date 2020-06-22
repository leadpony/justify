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

package org.leadpony.justify.api;

import java.util.Collection;
import java.util.Objects;

/**
 * A dispatcher interface for dispatching the problems found
 * while validating a JSON document.
 *
 * <p>Note that this type is not intended to be used directly by end users.</p>
 *
 * @author leadpony
 */
public interface ProblemDispatcher {

    /**
     * Dispatches the problem found.
     * @param problem the problem to dispatch, cannot be {@code null}.
     */
    void dispatchProblem(Problem problem);

    /**
     * Dispatches all problems in the specified collection.
     * @param problems the collection of the problems to dispatch.
     * @throws NullPointerException if the specified {@code problems} is {@code null}.
     */
    default void dispatchAllProblems(Collection<Problem> problems) {
        Objects.requireNonNull(problems, "problems must not be null.");
        for (Problem problem : problems) {
            dispatchProblem(problem);
        }
    }
}
