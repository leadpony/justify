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

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.ObjectKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * Assertion specified with "maxProperties" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("maxProperties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class MaxProperties extends AbstractAssertion implements ObjectKeyword {

    private final int limit;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromNonNegativeInteger mapper = MaxProperties::new;
        return mapper;
    }

    public MaxProperties(int limit) {
        this.limit = limit;
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        return new AssertionEvaluator(context, limit, this);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        return new MinProperties.AssertionEvaluator(context, limit + 1, this);
    }

    @Override
    public JsonValue getValueAsJson(JsonProvider jsonProvider) {
        return jsonProvider.createValue(limit);
    }

    /**
     * An evaluator of this keyword.
     *
     * @author leadpony
     */
    static class AssertionEvaluator extends ShallowEvaluator {

        private final int maxProperties;
        private final ProblemBuilderFactory factory;
        private int currentCount;

        AssertionEvaluator(EvaluatorContext context, int maxProperties, ProblemBuilderFactory factory) {
            super(context);
            this.maxProperties = maxProperties;
            this.factory = factory;
        }

        @Override
        public Result evaluateShallow(Event event, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1) {
                if (event == Event.KEY_NAME) {
                    ++currentCount;
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (currentCount <= maxProperties) {
                    return Result.TRUE;
                } else {
                    Problem p = factory.createProblemBuilder(getContext())
                            .withMessage(Message.INSTANCE_PROBLEM_MAXPROPERTIES)
                            .withParameter("actual", currentCount)
                            .withParameter("limit", maxProperties)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            }
            return Result.PENDING;
        }
    }
}
