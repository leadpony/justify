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
package org.leadpony.justify.internal.evaluator;

import jakarta.json.stream.JsonLocation;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.json.SimpleJsonLocation;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * An implementation of {@link Evaluator} which uses {@link EvaluatorContext}.
 *
 * @author leadpony
 */
public abstract class RedundantElementEvaluator implements Evaluator {

    private final Evaluator parent;
    private final JsonSchema schema;

    protected RedundantElementEvaluator(Evaluator parent, JsonSchema schema) {
        this.parent = parent;
        this.schema = schema;
    }

    @Override
    public final Evaluator getParent() {
        return parent;
    }

    @Override
    public final JsonSchema getSchema() {
        return schema;
    }

    protected ProblemBuilder newProblemBuilder() {
        EvaluatorContext context = getContext();
        JsonLocation location = context.getParser().getLocation();
        String pointer = context.getPointer();
        return new ProblemBuilder(SimpleJsonLocation.before(location), pointer)
            .withSchema(getSchema());
    }
}
