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

package org.leadpony.justify.internal.keyword.assertion;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.Message;

/**
 * Assertion specified with "pattern" validation keyword.
 *
 * @author leadpony
 */
class Pattern extends AbstractStringAssertion {

    private final java.util.regex.Pattern pattern;

    Pattern(java.util.regex.Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public String name() {
        return "pattern";
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), pattern.toString());
    }

    @Override
    protected Result evaluateAgainst(String value, Event event, EvaluatorContext context, ProblemDispatcher dispatcher) {
        if (testValue(value)) {
            return Result.TRUE;
        } else {
            Problem p = createProblemBuilder(context, event)
                    .withMessage(Message.INSTANCE_PROBLEM_PATTERN)
                    .withParameter("pattern", pattern.toString())
                    .build();
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        }
    }

    @Override
    protected Result evaluateNegatedAgainst(String value, Event event, EvaluatorContext context, ProblemDispatcher dispatcher) {
        if (testValue(value)) {
            Problem p = createProblemBuilder(context, event)
                    .withMessage(Message.INSTANCE_PROBLEM_NOT_PATTERN)
                    .withParameter("pattern", pattern.toString())
                    .build();
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        } else {
            return Result.TRUE;
        }
    }

    private boolean testValue(String value) {
        return pattern.matcher(value).find();
    }
}
