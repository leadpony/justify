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
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.json.SimpleJsonLocation;
import org.leadpony.justify.internal.evaluator.AbstractEvaluator;
import org.leadpony.justify.internal.problem.ProblemBuilder;

import jakarta.json.stream.JsonLocation;

/**
 * A skeletal implementation of evaluators produced by JSON schemas.
 *
 * @author leadpony
 */
abstract class AbstractSchemaBasedEvaluator extends AbstractEvaluator {

    private final JsonSchema schema;
    private EvaluatorContext context;
    private ProblemDispatcher dispatcher;

    protected AbstractSchemaBasedEvaluator(Evaluator parent, JsonSchema schema) {
        super(parent);
        this.schema = schema;
    }

    protected AbstractSchemaBasedEvaluator(Evaluator parent, JsonSchema schema, EvaluatorContext context) {
        this(parent, schema);
        this.context = context;
    }

    @Override
    public final JsonSchema getSchema() {
        return schema;
    }

    @Override
    public final EvaluatorContext getContext() {
        if (context != null) {
            return context;
        }
        context = getParent().getContext();
        return context;
    }

    @Override
    public ProblemDispatcher getDispatcher() {
        if (dispatcher != null) {
            return dispatcher;
        }
        dispatcher = super.getDispatcher();
        return dispatcher;
    }

    /**
     * {@inheritDoc}
     * @return {@code true} bacause this evaluator is based on a schema.
     */
    @Override
    public boolean isBasedOnSchema() {
        return true;
    }

    protected final ProblemBuilder createProblemBuilder() {
        EvaluatorContext context = getContext();
        JsonLocation location = context.getParser().getLocation();
        String pointer = context.getPointer();
        return new ProblemBuilder(SimpleJsonLocation.before(location), pointer)
            .withSchema(getSchema());
    }
}
