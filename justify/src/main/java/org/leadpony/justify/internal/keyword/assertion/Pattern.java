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

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * Assertion specified with "pattern" validation keyword.
 *
 * @author leadpony
 */
public class Pattern extends AbstractStringAssertion {

    private final java.util.regex.Pattern pattern;

    public Pattern(java.util.regex.Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public String name() {
        return "pattern";
    }

    @Override
    public JsonValue getValueAsJson(JsonProvider jsonProvider) {
        return jsonProvider.createValue(pattern.toString());
    }

    @Override
    protected boolean testValue(String value) {
        return pattern.matcher(value).find();
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_PATTERN)
            .withParameter("pattern", pattern.toString())
            .build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_NOT_PATTERN)
            .withParameter("pattern", pattern.toString())
            .build();
    }
}
