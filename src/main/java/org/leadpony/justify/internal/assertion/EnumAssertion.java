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

package org.leadpony.justify.internal.assertion;

import java.util.Set;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator.ProblemReporter;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.core.Problem;

/**
 * Assertion specified with "enum" keyword.
 * 
 * @author leadpony
 */
public class EnumAssertion extends AbstractEqualityAssertion {
    
    private final Set<JsonValue> expected;

    public EnumAssertion(Set<JsonValue> expected, JsonProvider jsonProvider) {
        super(jsonProvider);
        this.expected = expected;
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeKey("enum");
        generator.writeStartArray();
        expected.forEach(generator::write);
        generator.writeEnd();
    }

    @Override
    protected AbstractAssertion createNegatedAssertion() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Result testValue(JsonValue actual, JsonParser parser, ProblemReporter reporter) {
        for (JsonValue expected : this.expected) {
            if (actual.equals(expected)) {
                return Result.TRUE;
            }
        }
        Problem p = ProblemBuilder.newBuilder(parser)
                .withMessage("instance.problem.enum")
                .withParameter("actual", actual)
                .withParameter("expected", this.expected)
                .build();
        reporter.reportProblem(p, parser);
        return Result.FALSE;
    }
}
