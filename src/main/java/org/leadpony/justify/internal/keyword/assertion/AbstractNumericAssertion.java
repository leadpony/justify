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
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.keyword.NumericKeyword;

/**
 * Assertion on a value of numeric type.
 * 
 * @author leadpony
 */
abstract class AbstractNumericAssertion extends AbstractAssertion implements NumericKeyword, Evaluator {

    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return this;
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return this::evaluateNegated;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        assert event == Event.VALUE_NUMBER;
        return evaluateAgainst(parser.getBigDecimal(), parser, dispatcher);
    }
    
    private Result evaluateNegated(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        assert event == Event.VALUE_NUMBER;
        return evaluateNegatedAgainst(parser.getBigDecimal(), parser, dispatcher);
    }

    /**
     * Evaluates this assertion on a numeric value.
     * 
     * @param value the value to apply this assertion.
     * @param parser the JSON parser.
     * @param dispatcher the dispatcher by which detected problems will be dispatched.
     * @return the result of the evaluation.
     */
    protected abstract Result evaluateAgainst(BigDecimal value, JsonParser parser, ProblemDispatcher dispatcher);

    /**
     * Evaluates the negated assertion on a numeric value.
     * 
     * @param value the value to apply this assertion.
     * @param parser the JSON parser.
     * @param dispatcher the dispatcher by which detected problems will be dispatched.
     * @return the result of the evaluation.
     */
    protected abstract Result evaluateNegatedAgainst(BigDecimal value, JsonParser parser, ProblemDispatcher dispatcher);
}
