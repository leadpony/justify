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

import java.util.Set;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * An assertion specified with "enum" validation keyword.
 *
 * @author leadpony
 */
class Enum extends AbstractEqualityAssertion {

    private final Set<JsonValue> expected;

    Enum(Set<JsonValue> expected) {
        this.expected = expected;
    }

    @Override
    public String name() {
        return "enum";
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonArrayBuilder arrayBuilder = builderFactory.createArrayBuilder();
        expected.forEach(arrayBuilder::add);
        builder.add("enum", arrayBuilder);
    }

    @Override
    protected boolean testValue(JsonValue value) {
        for (JsonValue expected : this.expected) {
            if (value.equals(expected)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_ENUM)
            .withParameter("expected", this.expected)
            .build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_NOT_ENUM)
        .withParameter("expected", this.expected)
        .build();
    }
}
