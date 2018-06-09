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

package org.leadpony.justify.internal.assertion;

import java.util.Set;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * @author leadpony
 */
class NotRequired extends Required {

    NotRequired(Set<String> names) {
        super(names);
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        assert type == InstanceType.OBJECT;
        return new NegatedEvaluator(names);
    }

    @Override
    protected AbstractAssertion createNegatedAssertion() {
        return new Required(this.names);
    }

    private static class NegatedEvaluator extends PropertyEvaluator {
        
        private final Set<String> names;
        
        private NegatedEvaluator(Set<String> names) {
            super(names);
            this.names = names;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            if (event == Event.KEY_NAME) {
                remaining.remove(parser.getString());
                return test(parser, consumer, false);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return test(parser, consumer, true);
            } else {
                return Result.PENDING;
            }
        }

        @Override
        protected Result test(JsonParser parser, Consumer<Problem> consumer, boolean last) {
            if (remaining.isEmpty()) {
                Problem p = ProblemBuilder.newBuilder(parser)
                        .withMessage("instance.problem.not.required")
                        .withParameter("expected", this.names)
                        .build();
                consumer.accept(p);
                return Result.FALSE;
            } else if (last) {
                return Result.TRUE;
            } else {
                return Result.PENDING;
            }
        }
    }
}
