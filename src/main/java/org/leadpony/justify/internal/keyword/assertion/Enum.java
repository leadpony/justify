/*
 * Copyright 2018 the Justify authors.
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
import javax.json.stream.JsonParser;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.Evaluator.Result;

/**
 * Assertion specified with "enum" validation keyword.
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
    protected Result assertEquals(JsonValue actual, JsonParser parser, ProblemDispatcher dispatcher) {
        if (contains(actual)) {
            return Result.TRUE;
        } else {
            Problem p = createProblemBuilder(parser)
                    .withMessage("instance.problem.enum")
                    .withParameter("actual", actual)
                    .withParameter("expected", this.expected)
                    .build();
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        }
    }

    @Override
    protected Result assertNotEquals(JsonValue actual, JsonParser parser, ProblemDispatcher dispatcher) {
        if (contains(actual)) {
            Problem p = createProblemBuilder(parser)
                    .withMessage("instance.problem.not.enum")
                    .withParameter("actual", actual)
                    .withParameter("expected", this.expected)
                    .build();
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        } else {
            return Result.TRUE;
        }
    }
    
    private boolean contains(JsonValue value) {
        for (JsonValue expected : this.expected) {
            if (value.equals(expected)) {
                return true;
            }
        }
        return false;
    }
}
