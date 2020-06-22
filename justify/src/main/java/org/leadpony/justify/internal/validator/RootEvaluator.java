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
package org.leadpony.justify.internal.validator;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.evaluator.schema.AlwaysFalseEvaluator;

import jakarta.json.stream.JsonParser;

/**
 * @author leadpony
 */
public interface RootEvaluator extends Evaluator, EvaluatorContext, ProblemDispatcher {

    /* As an Evaluator */

    @Override
    default Result evaluate(JsonParser.Event event, int depth, ProblemDispatcher dispatcher) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Evaluator getParent() {
        return null;
    }

    @Override
    default JsonSchema getSchema() {
        return null;
    }

    @Override
    default EvaluatorContext getContext() {
        return this;
    }

    @Override
    default JsonParser getParser() {
        return Evaluator.super.getParser();
    }

    @Override
    default ProblemDispatcher getDispatcher(Evaluator evaluator) {
        return this;
    }

    /* As an EvaluatorContext */

    @Override
    default Evaluator createAlwaysFalseEvaluator(Evaluator parent, JsonSchema schema) {
        return new AlwaysFalseEvaluator(parent, schema, this);
    }
}
