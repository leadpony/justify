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

package org.leadpony.justify.internal.keyword.assertion;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractKeywordEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.keyword.StringKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * Assertion on values of string type.
 *
 * @author leadpony
 */
abstract class AbstractStringAssertion extends AbstractAssertionKeyword implements StringKeyword  {

    protected AbstractStringAssertion(JsonValue json) {
        super(json);
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        String value = context.getParser().getString();
        if (testValue(value)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new AbstractKeywordEvaluator(context, schema, this) {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                ProblemBuilder builder = newProblemBuilder();
                buildProblem(builder, event, value);
                dispatcher.dispatchProblem(createProblem(builder));
                return Result.FALSE;
            }
        };
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        String value = context.getParser().getString();
        if (!testValue(value)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new AbstractKeywordEvaluator(context, schema, this) {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                ProblemBuilder builder = newProblemBuilder();
                buildProblem(builder, event, value);
                dispatcher.dispatchProblem(createNegatedProblem(builder));
                return Result.FALSE;
            }
        };
    }

    public void buildProblem(ProblemBuilder builder, Event event, String actual) {
        if (event == Event.KEY_NAME) {
            builder.withParameter("subject", "key")
                   .withParameter("localizedSubject", Message.STRING_KEY)
                   .withParameter("actual", toActualValue(actual));
        } else {
            builder.withParameter("subject", "value")
                   .withParameter("localizedSubject", Message.STRING_VALUE);
        }
        builder.withParameter("actual", toActualValue(actual));
    }

    protected abstract boolean testValue(String value);

    protected Object toActualValue(String value) {
        return value;
    }

    protected abstract Problem createProblem(ProblemBuilder builder);

    protected abstract Problem createNegatedProblem(ProblemBuilder builder);
}
