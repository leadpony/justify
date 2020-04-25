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

import jakarta.json.stream.JsonLocation;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.internal.base.json.SimpleJsonLocation;

/**
 * Factory interface producing instances of {@link ProblemBuilder}.
 *
 * @author leadpony
 */
public interface ProblemBuilderFactory {

    /**
     * Default instance of this type.
     */
    ProblemBuilderFactory DEFAULT = new ProblemBuilderFactory() {
    };

    /**
     * Creates new instance of this builder.
     *
     * @param context the evaluator context, cannot be {@code null}.
     * @return newly created instance of {@link ProblemBuilder}.
     */
    default ProblemBuilder createProblemBuilder(EvaluatorContext context) {
        JsonLocation location = context.getParser().getLocation();
        String pointer = context.getPointer();
        return createProblemBuilder(location, pointer);
    }

    /**
     * Creates new instance of this builder.
     *
     * @param location the location where problem occurred, cannot be {@code null}.
     * @param pointer  the JSON pointer where problem occurred, can be {@code null}.
     * @return newly created instance of {@link ProblemBuilder}.
     */
    default ProblemBuilder createProblemBuilder(JsonLocation location, String pointer) {
        return new ProblemBuilder(SimpleJsonLocation.before(location), pointer);
    }
}
