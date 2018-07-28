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

import java.math.BigDecimal;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
abstract class AbstractNumericBoundAssertion extends AbstractNumericAssertion {

    protected final BigDecimal bound;
    private final String name;
    private final String message;
    
    protected AbstractNumericBoundAssertion(BigDecimal bound, String name, String message) {
        this.bound = bound;
        this.name = name;
        this.message = message;
    }
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    protected Result evaluateAgainstNumber(BigDecimal value, JsonParser parser, Reporter reporter) {
        if (test(value, this.bound)) {
            return Result.TRUE;
        } else {
            Problem p = newProblemBuilder(parser)
                    .withMessage(this.message)
                    .withParameter("actual", value)
                    .withParameter("bound", this.bound)
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        }
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), this.bound);
    }
    
    protected abstract boolean test(BigDecimal actual, BigDecimal bound);
}
