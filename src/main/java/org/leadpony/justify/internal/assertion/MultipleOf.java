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

import java.math.BigDecimal;

import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Assertion specified with "multipleOf" keyword.
 * 
 * @author leadpony
 */
class MultipleOf extends ShallowAssertion {
    
    protected final BigDecimal divisor;
    
    public MultipleOf(BigDecimal divisor) {
        this.divisor = divisor;
    }

    @Override
    public String name() {
        return "multipleOf";
    }
    
    @Override
    public boolean canApplyTo(InstanceType type) {
        return type.isNumeric();
    }
    
    @Override
    protected Result evaluateShallow(Event event, JsonParser parser, int depth, Reporter reporter) {
        assert event == Event.VALUE_NUMBER;
        BigDecimal actual = parser.getBigDecimal();
        return test(actual, parser, reporter);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        builder.add(name(), divisor);
    }
    
    @Override
    protected AbstractAssertion createNegatedAssertion() {
        throw new UnsupportedOperationException();
    }
    
    protected Result test(BigDecimal actual, JsonParser parser, Reporter reporter) {
        BigDecimal remainder = actual.remainder(divisor);
        if (remainder.compareTo(BigDecimal.ZERO) == 0) {
            return Result.TRUE;
        } else {
            Problem p = ProblemBuilder.newBuilder(parser)
                    .withMessage("instance.problem.multiple.of")
                    .withParameter("actual", actual)
                    .withParameter("divisor", divisor)
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        }
    }
}
