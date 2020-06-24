/*
 * Copyright 2018, 2020 the Justify authors.
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

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;
import org.leadpony.justify.internal.keyword.AbstractObjectAssertionKeyword;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * Assertion specified with "minProperties" validation keyword.
 *
 * @author leadpony
 */
@KeywordClass("minProperties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class MinProperties extends AbstractObjectAssertionKeyword {

    public static final KeywordType TYPE = KeywordTypes.mappingNonNegativeInteger("minProperties", MinProperties::new);

    private final int limit;

    public MinProperties(JsonValue json, int limit) {
        super(json);
        this.limit = limit;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        return new PropertiesEvaluator(parent, this, limit);
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        if (limit > 0) {
            return new MaxProperties.PropertiesEvaluator(parent, this, limit - 1);
        } else {
            return Evaluator.alwaysFalse(parent, parent.getSchema());
        }
    }

    /**
     * An evaluator of this keyword.
     *
     * @author leadpony
     */
    static class PropertiesEvaluator extends ShallowEvaluator {

        private final int minProperties;
        private int currentCount;

        PropertiesEvaluator(Evaluator parent, Keyword keyword, int minProperties) {
            super(parent, keyword);
            this.minProperties = minProperties;
        }

        @Override
        public Result evaluateShallow(Event event, int depth) {
            if (depth == 1) {
                if (event == Event.KEY_NAME && ++currentCount >= minProperties) {
                    return Result.TRUE;
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (currentCount >= minProperties) {
                    return Result.TRUE;
                } else {
                    Problem p = newProblemBuilder()
                            .withMessage(Message.INSTANCE_PROBLEM_MINPROPERTIES)
                            .withParameter("actual", currentCount)
                            .withParameter("limit", minProperties)
                            .build();
                    getDispatcher().dispatchProblem(p);
                    return Result.FALSE;
                }
            }
            return Result.PENDING;
        }
    }
}
