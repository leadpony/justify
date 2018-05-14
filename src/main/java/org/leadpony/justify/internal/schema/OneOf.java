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

package org.leadpony.justify.internal.schema;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * @author leadpony
 */
public class OneOf extends NaryBooleanLogicSchema {

    public OneOf(Collection<JsonSchema> subschemas) {
        super(subschemas);
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return new ExclusiveDisjunctionEvalutor(type, subschemas());
    }

    @Override
    public void toJson(JsonGenerator generator) {
        super.toJson(generator, "oneOf");
    }
    
    static class ExclusiveDisjunctionEvalutor extends DisjunctionEvaluator {

        private int numberOfTrueEvaluations;
        
        ExclusiveDisjunctionEvalutor(InstanceType type, List<JsonSchema> subschemas) {
            super(type, subschemas);
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            Iterator<DelayedEvaluator> it = running.iterator();
            while (it.hasNext()) {
                DelayedEvaluator evaluator = it.next();
                Result result = evaluator.evaluate(event, parser, depth, consumer);
                if (result != Result.CONTINUED) {
                    it.remove();
                    if (result == Result.TRUE) {
                        if (++numberOfTrueEvaluations > 1) {
                            return tooManyTrueEvaluations(consumer);
                        }
                    } else {
                        addFailed(evaluator);
                    }
                }
            }
            return running.isEmpty() ? deliverProblems(consumer) : Result.CONTINUED;
        }

        @Override
        protected Result deliverProblems(Consumer<Problem> consumer) {
            if (numberOfTrueEvaluations == 1) {
                return Result.TRUE;
            } else {
                return super.deliverProblems(consumer);
            }
        }
        
        private Result tooManyTrueEvaluations(Consumer<Problem> consumer) {
            Problem p = ProblemBuilder.newBuilder()
                    .withMessage("instance.problem.one.of")
                    .withParameter("actual", numberOfTrueEvaluations)
                    .build();
            consumer.accept(p);
            return Result.FALSE;
        }
    }
}
