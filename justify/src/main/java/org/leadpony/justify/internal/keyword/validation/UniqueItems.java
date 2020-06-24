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

import java.util.HashMap;
import java.util.Map;

import jakarta.json.JsonBuilderFactory;
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
import org.leadpony.justify.internal.base.json.JsonInstanceBuilder;
import org.leadpony.justify.internal.evaluator.AbstractKeywordBasedEvaluator;
import org.leadpony.justify.internal.keyword.AbstractArrayAssertionKeyword;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * An assertion specified with "uniqueItems" validation keyword.
 *
 * @author leadpony
 */
@KeywordClass("uniqueItems")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class UniqueItems extends AbstractArrayAssertionKeyword {

    public static final KeywordType TYPE = KeywordTypes.mappingJson("uniqueItems", UniqueItems::of);

    private static final UniqueItems TRUE = new UniqueItems(JsonValue.TRUE) {

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            return new UniqueItemsEvaluator(parent, this);
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            return new NegatedUniqueItemsEvaluator(parent, this);
        }
    };

    private static final UniqueItems FALSE = new UniqueItems(JsonValue.FALSE) {

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            return Evaluator.alwaysFalse(parent, parent.getSchema());
        }
    };

    public static Keyword of(JsonValue value) {
        if (value == JsonValue.TRUE) {
            return TRUE;
        } else if (value == JsonValue.FALSE) {
            return FALSE;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Keyword of(boolean value) {
        return value ? TRUE : FALSE;
    }

    protected UniqueItems(JsonValue json) {
        super(json);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    /**
     * An evaluator which evaluates the items.
     *
     * @author leadpony
     */
    private class UniqueItemsEvaluator extends AbstractKeywordBasedEvaluator {

        private final JsonBuilderFactory builderFactory;
        private final Map<JsonValue, Integer> values = new HashMap<>();
        private boolean duplicated;
        private int firstOccurrenceAt, secondOccurrenceAt;
        private int index;
        private JsonInstanceBuilder builder;

        protected UniqueItemsEvaluator(Evaluator parent, Keyword keyword) {
            super(parent, keyword);
            this.builderFactory = getContext().getJsonBuilderFactory();
        }

        @Override
        public Result evaluate(Event event, int depth) {
            if (depth == 0) {
                if (event == Event.END_ARRAY) {
                    return getFinalResult();
                } else {
                    return Result.PENDING;
                }
            }
            if (hasDuplicatedItems()) {
                return Result.PENDING;
            }
            if (builder == null) {
                builder = new JsonInstanceBuilder(builderFactory);
            }
            if (builder.append(event, getParser())) {
                return Result.PENDING;
            } else {
                JsonValue value = builder.build();
                builder = null;
                testItemValue(value, index++);
                return Result.PENDING;
            }
        }

        private void testItemValue(JsonValue value, int index) {
            if (values.containsKey(value)) {
                duplicated = true;
                firstOccurrenceAt = values.get(value);
                secondOccurrenceAt = index;
            } else {
                values.put(value, index);
            }
        }

        protected final boolean hasDuplicatedItems() {
            return duplicated;
        }

        protected Result getFinalResult() {
            if (duplicated) {
                Problem p = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_UNIQUEITEMS)
                        .withParameter("index", secondOccurrenceAt)
                        .withParameter("firstIndex", firstOccurrenceAt)
                        .build();
                getDispatcher().dispatchProblem(p);
                return Result.FALSE;
            } else {
                return Result.TRUE;
            }
        }
    }

    /**
     * A negated version of {@link UniqueItemsEvaluator}.
     *
     * @author leadpony
     */
    private final class NegatedUniqueItemsEvaluator extends UniqueItemsEvaluator {

        private NegatedUniqueItemsEvaluator(Evaluator parent, Keyword keyword) {
            super(parent, keyword);
        }

        @Override
        protected Result getFinalResult() {
            if (hasDuplicatedItems()) {
                return Result.TRUE;
            } else {
                Problem p = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_UNIQUEITEMS)
                        .build();
                getDispatcher().dispatchProblem(p);
                return Result.FALSE;
            }
        }
    }
}
