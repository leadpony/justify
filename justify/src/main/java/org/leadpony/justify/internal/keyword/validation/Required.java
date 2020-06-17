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

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.keyword.KeywordTypes;
import org.leadpony.justify.internal.keyword.ObjectEvaluatorSource;

/**
 * Assertion specified with "required" validation keyword.
 *
 * @author leadpony
 */
@KeywordClass("required")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Required extends AbstractAssertionKeyword implements ObjectEvaluatorSource {

    public static final KeywordType TYPE = KeywordTypes.mappingStringSet("required", Required::new);

    private final Set<String> names;

    public Required(JsonValue json, Set<String> names) {
        super(json);
        this.names = new LinkedHashSet<>(names);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        if (names.isEmpty()) {
            return Evaluator.ALWAYS_TRUE;
        } else {
            return new ValueTypeEvaluator(parent, this, names);
        }
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        if (names.isEmpty()) {
            return Evaluator.alwaysFalse(parent, parent.getSchema());
        } else {
            return new NegatedValueTypeEvaluator(parent, this, names);
        }
    }

    /**
     * An evaluator of this keyword.
     *
     * @author leadpony
     */
    private final class ValueTypeEvaluator extends ShallowEvaluator {

        private final Set<String> missing;

        private ValueTypeEvaluator(Evaluator parent, Keyword keyword, Set<String> required) {
            super(parent, keyword);
            this.missing = new LinkedHashSet<>(required);
        }

        @Override
        public Result evaluateShallow(Event event, int depth, ProblemDispatcher dispatcher) {
            if (event == Event.KEY_NAME) {
                missing.remove(getParser().getString());
                if (missing.isEmpty()) {
                    return Result.TRUE;
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (missing.isEmpty()) {
                    return Result.TRUE;
                } else {
                    return dispatchProblems(dispatcher);
                }
            }
            return Result.PENDING;
        }

        private Result dispatchProblems(ProblemDispatcher dispatcher) {
            for (String property : missing) {
                Problem p = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_REQUIRED)
                        .withParameter("required", property)
                        .build();
                dispatcher.dispatchProblem(p);
            }
            return Result.FALSE;
        }
    }

    /**
     * An evaluator of negated version of this keyword.
     *
     * @author leadpony
     */
    private final class NegatedValueTypeEvaluator extends ShallowEvaluator {

        private final Set<String> missing;

        private NegatedValueTypeEvaluator(Evaluator parent, Keyword keyword,
                Set<String> names) {
            super(parent, keyword);
            this.missing = new LinkedHashSet<>(names);
        }

        @Override
        public Result evaluateShallow(Event event, int depth, ProblemDispatcher dispatcher) {
            if (event == Event.KEY_NAME) {
                missing.remove(getParser().getString());
                if (missing.isEmpty()) {
                    return dispatchProblem(dispatcher);
                }
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (missing.isEmpty()) {
                    return dispatchProblem(dispatcher);
                } else {
                    return Result.TRUE;
                }
            }
            return Result.PENDING;
        }

        private Result dispatchProblem(ProblemDispatcher dispatcher) {
            Problem p = null;
            if (names.size() == 1) {
                String name = names.iterator().next();
                p = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                        .withParameter("required", name)
                        .build();
            } else {
                p = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED_PLURAL)
                        .withParameter("required", names)
                        .build();
            }
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        }
    }
}
