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

import java.util.Set;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.problem.ProblemBuilder;

import jakarta.json.stream.JsonParser.Event;

/**
 * An evaluator used when the instance type is not supported by the keyword.
 *
 * @author leadpony
 */
public final class UnsupportedTypeEvaluator extends AbstractKeywordAwareEvaluator {

    private final Set<InstanceType> expected;
    private final InstanceType actual;

    public UnsupportedTypeEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword,
            Set<InstanceType> expected, InstanceType actual) {
        super(context, schema, keyword);
        this.expected = expected;
        this.actual = actual;
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        ProblemBuilder builder = newProblemBuilder()
                .withParameter("actual", actual);
        if (expected.size() > 1) {
            builder.withMessage(Message.INSTANCE_PROBLEM_TYPE_PLURAL)
                    .withParameter("expected", expected);
        } else {
            InstanceType first = expected.iterator().next();
            builder.withMessage(Message.INSTANCE_PROBLEM_TYPE)
                    .withParameter("expected", first);
        }
        dispatcher.dispatchProblem(builder.build());
        return Result.FALSE;
    }
}
