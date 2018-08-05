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
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * Assertion on values of numeric type.
 * 
 * @author leadpony
 */
abstract class AbstractNumericAssertion extends AbstractAssertion {

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type.isNumeric()) {
            appender.append(this::evaluate);
        }
    }

    @Override
    public void createNegatedEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type.isNumeric()) {
            appender.append(this::evaluateNegated);
        }
    }

    private Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        assert event == Event.VALUE_NUMBER;
        return evaluateAgainst(parser.getBigDecimal(), parser, reporter);
    }
    
    private Result evaluateNegated(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        assert event == Event.VALUE_NUMBER;
        return evaluateNegatedAgainst(parser.getBigDecimal(), parser, reporter);
    }

    /**
     * Evaluates this assertion on a numeric value.
     * 
     * @param value the value to apply this assertion.
     * @param parser the JSON parser.
     * @param reporter the reporter to which detected problems will be reported.
     * @return the result of the evaluation.
     */
    protected abstract Result evaluateAgainst(BigDecimal value, JsonParser parser, Consumer<Problem> reporter);

    /**
     * Evaluates the negated assertion on a numeric value.
     * 
     * @param value the value to apply this assertion.
     * @param parser the JSON parser.
     * @param reporter the reporter to which detected problems will be reported.
     * @return the result of the evaluation.
     */
    protected abstract Result evaluateNegatedAgainst(BigDecimal value, JsonParser parser, Consumer<Problem> reporter);
}
