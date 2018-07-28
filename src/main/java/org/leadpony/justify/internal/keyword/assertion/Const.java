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

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator.Reporter;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.core.Problem;

/**
 * Assertion specified with "const" validation keyword.
 * 
 * @author leadpony
 */
class Const extends AbstractEqualityAssertion {

    private final JsonValue expected;
    
    Const(JsonValue expected) {
        this.expected = expected;
    }
    
    @Override
    public String name() {
        return "const";
    }
  
    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add("const", this.expected);
    }

    @Override
    protected Result testValue(JsonValue actual, JsonParser parser, Reporter reporter) {
        if (actual.equals(expected)) {
            return Result.TRUE;
        } else {
            Problem p = newProblemBuilder(parser)
                    .withMessage("instance.problem.const")
                    .withParameter("actual", actual)
                    .withParameter("expected", expected)
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        }
    }
}
