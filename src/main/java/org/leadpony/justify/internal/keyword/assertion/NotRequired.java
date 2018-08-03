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

import java.util.Set;
import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * Negation of {@link Required}.
 * 
 * @author leadpony
 */
class NotRequired extends Required {

    NotRequired(Set<String> names) {
        super(names);
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        assert type == InstanceType.OBJECT;
        appender.append(new NegatedEvaluator(names));
    }

    @Override
    public Assertion negate() {
        return new Required(this.names);
    }

    private class NegatedEvaluator extends AssertionEvaluator {
        
        private final Set<String> names;
        
        private NegatedEvaluator(Set<String> names) {
            super(names);
            this.names = names;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
            if (event == Event.KEY_NAME) {
                missing.remove(parser.getString());
                return test(parser, reporter, false);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return test(parser, reporter, true);
            } else {
                return Result.PENDING;
            }
        }

        @Override
        protected Result test(JsonParser parser, Consumer<Problem> reporter, boolean last) {
            if (missing.isEmpty()) {
                Problem p = null;
                if (this.names.size() == 1) {
                    String name = this.names.iterator().next();
                    p = createProblemBuilder(parser)
                            .withMessage("instance.problem.not.required.single")
                            .withParameter("expected", name)
                            .build();
                } else {
                    p = createProblemBuilder(parser)
                        .withMessage("instance.problem.not.required")
                        .withParameter("expected", this.names)
                        .build();
                }
                reporter.accept(p);
                return Result.FALSE;
            } else if (last) {
                return Result.TRUE;
            } else {
                return Result.PENDING;
            }
        }
    }
}
