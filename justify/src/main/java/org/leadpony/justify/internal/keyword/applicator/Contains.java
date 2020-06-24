/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.internal.keyword.applicator;

import java.util.List;
import java.util.Map;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.CountingItemsEvaluator;
import org.leadpony.justify.internal.keyword.KeywordTypes;
import org.leadpony.justify.internal.keyword.validation.MaxContains;
import org.leadpony.justify.internal.keyword.validation.MinContains;
import org.leadpony.justify.internal.problem.ProblemBranch;

/**
 * @author leadpony
 */
@Spec(SpecVersion.DRAFT_2019_09)
public class Contains extends SimpleContains {

    public static final KeywordType TYPE = KeywordTypes.mappingSchema("contains", Contains::new);

    private final MaxContains maxContains;
    private final MinContains minContains;

    public Contains(JsonSchema subschema) {
        this(subschema, null, null);
    }

    public Contains(JsonSchema subschema, MaxContains maxContains, MinContains minContains) {
        super(subschema);
        this.maxContains = maxContains;
        this.minContains = minContains;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Keyword withKeywords(Map<String, Keyword> siblings) {
        MaxContains maxContains = null;
        if (siblings.containsKey("maxContains")) {
            Keyword keyword = siblings.get("maxContains");
            if (keyword instanceof MaxContains) {
                maxContains = (MaxContains) keyword;
            }
        }

        MinContains minContains = null;
        if (siblings.containsKey("minContains")) {
            Keyword keyword = siblings.get("minContains");
            if (keyword instanceof MinContains) {
                minContains = (MinContains) keyword;
            }
        }

        if (maxContains != null || minContains != null) {
            return new Contains(getSubschema(), maxContains, minContains);
        } else {
            return this;
        }
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        if (maxContains != null) {
            return new BoundedItemsEvaluator(parent, maxContains, minContains);
        } else if (minContains != null) {
            final int bound = minContains.value();
            if (bound > 1) {
                return new LowerBoundedItemsEvaluator(parent, minContains, bound);
            } else if (bound < 1) {
                return Evaluator.ALWAYS_TRUE;
            }
        }
        return super.createEvaluator(parent, type);
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        if (maxContains != null) {
            return new OutOfBoundsItemsEvaluator(parent, maxContains, minContains);
        } else if (minContains != null) {
            final int bound = minContains.value();
            if (bound > 1) {
                return new UpperBoundedItemsEvaluator(parent, minContains,
                        bound - 1 // At most bound - 1
                        );
            } else if (bound < 1) {
                return Evaluator.alwaysFalse(parent, parent.getSchema());
            }
        }
        return super.createNegatedEvaluator(parent, type);
    }

    private abstract class AbstractBoundedItemsEvaluator extends CountingItemsEvaluator {

        protected AbstractBoundedItemsEvaluator(Evaluator parent) {
            this(parent, Contains.this);
        }

        protected AbstractBoundedItemsEvaluator(Evaluator parent, Keyword keyword) {
            super(parent, keyword, getSubschema());
        }
    }

    /**
     * An array items evaluator with lower bound.
     *
     * @author leadpony
     */
    private class LowerBoundedItemsEvaluator extends AbstractBoundedItemsEvaluator {

        private final int bound;

        LowerBoundedItemsEvaluator(Evaluator parent, Keyword keyowrd, int bound) {
            super(parent, keyowrd);
            this.bound = bound;
        }

        @Override
        protected Result handleValidItem(int validItems) {
            if (validItems >= bound) {
                return Result.TRUE;
            }
            return Result.PENDING;
        }

        @Override
        protected Result finish(int validItems, List<ProblemBranch> branches) {
            return reportTooFewValidItems();
        }

        private Result reportTooFewValidItems() {
            Problem problem = newProblemBuilder()
                    .withMessage(Message.INSTANCE_PROBLEM_MINCONTAINS)
                    .withParameter("limit", bound)
                    .build();
            getDispatcher().dispatchProblem(problem);
            return Result.FALSE;
        }
    }

    /**
     * An array items evaluator with upper bound.
     *
     * @author leadpony
     */
    private class UpperBoundedItemsEvaluator extends AbstractBoundedItemsEvaluator {

        private final int bound;

        UpperBoundedItemsEvaluator(Evaluator parent, Keyword keyword, int bound) {
            super(parent, keyword);
            this.bound = bound;
        }

        @Override
        protected Result handleValidItem(int validItems) {
            if (validItems > bound) {
                return reportTooManyValidItems();
            }
            return Result.PENDING;
        }

        @Override
        protected Result finish(int validItems, List<ProblemBranch> branches) {
            return Result.TRUE;
        }

        private Result reportTooManyValidItems() {
            Problem problem = newProblemBuilder()
                    .withMessage(Message.INSTANCE_PROBLEM_MAXCONTAINS)
                    .withParameter("limit", bound)
                    .build();
            getDispatcher().dispatchProblem(problem);
            return Result.FALSE;
        }
    }

    private abstract class AbstractBothBoundedItemsEvaluator extends AbstractBoundedItemsEvaluator {

        private final MaxContains maxContains;
        // minContains can be null
        private final MinContains minContains;
        protected final int upperBound;
        protected final int lowerBound;

        protected AbstractBothBoundedItemsEvaluator(Evaluator parent,
                MaxContains maxContains,
                MinContains minContains) {
            super(parent);
            this.maxContains = maxContains;
            this.upperBound = maxContains.value();
            this.minContains = minContains;
            this.lowerBound = (minContains != null) ? minContains.value() : 1;
        }

        protected final Keyword getMaxKeyword() {
            return maxContains;
        }

        protected final Keyword getMinKeyword() {
            return (minContains != null) ? minContains : getKeyword();
        }
    }

    /**
     * An array items evaluator with both lower and upper bound.
     *
     * @author leadpony
     */
    private class BoundedItemsEvaluator extends AbstractBothBoundedItemsEvaluator {

        BoundedItemsEvaluator(Evaluator parent, MaxContains maxContains,
                MinContains minContains) {
            super(parent, maxContains, minContains);
        }

        @Override
        protected Result handleValidItem(int validItems) {
            if (validItems > upperBound && lowerBound <= upperBound) {
                return reportTooManyValidItems();
            }
            return Result.PENDING;
        }

        @Override
        protected Result finish(int validItems, List<ProblemBranch> branches) {
            Result result = Result.TRUE;

            if (validItems < lowerBound) {
                reportTooFewValidItems();
                result = Result.FALSE;
            }

            if (validItems > upperBound) {
                reportTooManyValidItems();
                result = Result.FALSE;
            }

            return result;
        }

        private Result reportTooFewValidItems() {
            Problem problem = newProblemBuilder()
                    .withKeyword(getMinKeyword().name())
                    .withMessage(Message.INSTANCE_PROBLEM_MINCONTAINS)
                    .withParameter("limit", lowerBound)
                    .build();
            getDispatcher().dispatchProblem(problem);
            return Result.FALSE;
        }

        private Result reportTooManyValidItems() {
            Problem problem = newProblemBuilder()
                    .withKeyword(getMaxKeyword().name())
                    .withMessage(Message.INSTANCE_PROBLEM_MAXCONTAINS)
                    .withParameter("limit", upperBound)
                    .build();
            getDispatcher().dispatchProblem(problem);
            return Result.FALSE;
        }
    }

    private class OutOfBoundsItemsEvaluator extends AbstractBothBoundedItemsEvaluator {

        OutOfBoundsItemsEvaluator(Evaluator parent, MaxContains maxContains,
                MinContains minContains) {
            super(parent, maxContains, minContains);
        }

        @Override
        protected Result handleValidItem(int validItems) {
            return Result.PENDING;
        }

        @Override
        protected Result finish(int validItems, List<ProblemBranch> branches) {
            if (validItems < lowerBound || validItems > upperBound) {
                return Result.TRUE;
            }
            getDispatcher().dispatchProblem(buildProblem());
            return Result.FALSE;
        }

        private Problem buildProblem() {
            ProblemBranch first = ProblemBranch.of(
                    newProblemBuilder()
                        .withKeyword(getMinKeyword().name())
                        .withMessage(Message.INSTANCE_PROBLEM_MAXCONTAINS)
                        .withParameter("limit", lowerBound - 1)
                        .build());

            ProblemBranch second = ProblemBranch.of(
                    newProblemBuilder()
                        .withKeyword(getMaxKeyword().name())
                        .withMessage(Message.INSTANCE_PROBLEM_MINCONTAINS)
                        .withParameter("limit", upperBound + 1)
                        .build());

            return newProblemBuilder()
                    .withBranch(first)
                    .withBranch(second)
                    .withMessage(Message.INSTANCE_PROBLEM_ANYOF)
                    .build();
        }
    }
}
