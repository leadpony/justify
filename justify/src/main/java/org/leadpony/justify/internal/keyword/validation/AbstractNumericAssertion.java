/*
 * Copyright 2018-2020 the Justify authors.
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

package org.leadpony.justify.internal.keyword.validation;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.evaluator.AbstractKeywordBasedEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * An assertion on a value of numeric type.
 *
 * @author leadpony
 */
abstract class AbstractNumericAssertion extends AbstractAssertionKeyword {

    private static final Set<InstanceType> SUPPORTED_TYPES = EnumSet.of(InstanceType.NUMBER, InstanceType.INTEGER);

    protected AbstractNumericAssertion(JsonValue json) {
        super(json);
    }

    @Override
    public boolean supportsType(InstanceType type) {
        return type.isNumeric();
    }

    @Override
    public Set<InstanceType> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        EvaluatorContext context = parent.getContext();
        BigDecimal value = context.getParser().getBigDecimal();
        if (testValue(value)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new AbstractKeywordBasedEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth) {
                ProblemBuilder builder = newProblemBuilder()
                        .withParameter("actual", value);
                getDispatcher().dispatchProblem(createProblem(builder));
                return Result.FALSE;
            }
        };
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        EvaluatorContext context = parent.getContext();
        BigDecimal value = context.getParser().getBigDecimal();
        if (!testValue(value)) {
            return Evaluator.ALWAYS_TRUE;
        }
        return new AbstractKeywordBasedEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth) {
                ProblemBuilder builder = newProblemBuilder()
                        .withParameter("actual", value);
                getDispatcher().dispatchProblem(createNegatedProblem(builder));
                return Result.FALSE;
            }
        };
    }

    protected abstract boolean testValue(BigDecimal value);

    protected abstract Problem createProblem(ProblemBuilder builder);

    protected abstract Problem createNegatedProblem(ProblemBuilder builder);
}
