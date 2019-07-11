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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
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

/**
 * Assertion specified with "required" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("required")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Required extends AbstractAssertion implements ObjectKeyword {

    private final Set<String> names;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.ARRAY) {
                Set<String> names = new LinkedHashSet<>();
                for (JsonValue item : value.asJsonArray()) {
                    if (item.getValueType() == ValueType.STRING) {
                        names.add(((JsonString) item).getString());
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                return new Required(value, names);
            }
            throw new IllegalArgumentException();
        };
    }

    public Required(JsonValue json, Set<String> names) {
        super(json);
        this.names = new LinkedHashSet<>(names);
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        if (names.isEmpty()) {
            return Evaluator.ALWAYS_TRUE;
        } else {
            return new AssertionEvaluator(context, names);
        }
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        if (names.isEmpty()) {
            return createAlwaysFalseEvaluator(context);
        } else {
            return new NegatedAssertionEvaluator(context, names);
        }
    }

    /**
     * An evaluator of this keyword.
     *
     * @author leadpony
     */
    private final class AssertionEvaluator extends ShallowEvaluator {

        private final Set<String> missing;

        private AssertionEvaluator(EvaluatorContext context, Set<String> required) {
            super(context);
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
                Problem p = createProblemBuilder(getContext())
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
    private final class NegatedAssertionEvaluator extends ShallowEvaluator {

        private final Set<String> missing;

        private NegatedAssertionEvaluator(EvaluatorContext context, Set<String> names) {
            super(context);
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
                p = createProblemBuilder(getContext())
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED)
                        .withParameter("required", name)
                        .build();
            } else {
                p = createProblemBuilder(getContext())
                        .withMessage(Message.INSTANCE_PROBLEM_NOT_REQUIRED_PLURAL)
                        .withParameter("required", names)
                        .build();
            }
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        }
    }
}
