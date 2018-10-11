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
import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
abstract class AbstractNumericBoundAssertion extends AbstractNumericAssertion {

    private final BigDecimal limit;
    private final String name;
    private final String messageKey;
    private final String negatedMessageKey;
    
    /**
     * Constructs this assertion.
     * 
     * @param limit the lower or upper limit.
     * @param name the name of this assertion.
     * @param messageKey the error message for normal evaluation. 
     * @param negatedMessageKey the error message for negated evaluation.
     */
    protected AbstractNumericBoundAssertion(
            BigDecimal limit, String name, String messageKey, String negatedMessageKey) {
        this.limit = limit;
        this.name = name;
        this.messageKey = messageKey;
        this.negatedMessageKey = negatedMessageKey;
    }
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), this.limit);
    }

    @Override
    protected Result evaluateAgainst(BigDecimal value, JsonParser parser, Consumer<Problem> reporter) {
        if (testValue(value, this.limit)) {
            return Result.TRUE;
        } else {
            Problem p = createProblemBuilder(parser)
                    .withMessage(this.messageKey)
                    .withParameter("actual", value)
                    .withParameter("limit", this.limit)
                    .build();
            reporter.accept(p);
            return Result.FALSE;
        }
    }

    @Override
    protected Result evaluateNegatedAgainst(BigDecimal value, JsonParser parser, Consumer<Problem> reporter) {
        if (testValue(value, this.limit)) {
            Problem p = createProblemBuilder(parser)
                    .withMessage(this.negatedMessageKey)
                    .withParameter("actual", value)
                    .withParameter("limit", this.limit)
                    .build();
            reporter.accept(p);
            return Result.FALSE;
        } else {
            return Result.TRUE;
        }
    }

    protected abstract boolean testValue(BigDecimal actual, BigDecimal limit);
}
