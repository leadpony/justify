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

package org.leadpony.justify.internal.keyword.assertion;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.ObjectEvaluatorSource;

/**
 * Assertion specified with "maxProperties" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("maxProperties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class MaxProperties extends AbstractAssertionKeyword implements ObjectEvaluatorSource {

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

    public MaxProperties(JsonValue json, int limit) {
        super(json);
        this.limit = limit;
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        return new AssertionEvaluator(context, schema, this, limit);
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        return new MinProperties.AssertionEvaluator(context, schema, this, limit + 1);
    }

    /**
     * An evaluator of this keyword.
     *
     * @author leadpony
     */
    static class AssertionEvaluator extends ShallowEvaluator {

        private final int maxProperties;
        private int currentCount;

        AssertionEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword, int maxProperties) {
            super(context, schema, keyword);
            this.maxProperties = maxProperties;
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
                    Problem p = newProblemBuilder()
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
