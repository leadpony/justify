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

/**
 * @author leadpony
 */
public class AnyOf extends NaryBooleanLogicSchema {

    public AnyOf(Collection<JsonSchema> subschemas) {
        super(subschemas);
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return new InclusiveDisjunctionEvaluator(type, subschemas());
    }

    @Override
    public void toJson(JsonGenerator generator) {
        super.toJson(generator, "anyOf");
    }

    static class InclusiveDisjunctionEvaluator extends DisjunctionEvaluator {
       
        InclusiveDisjunctionEvaluator(InstanceType type, List<JsonSchema> subschemas) {
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
                        return Result.TRUE;
                    } else {
                        addFailed(evaluator);
                    }
                }
            }
            return running.isEmpty() ? deliverProblems(consumer) : Result.CONTINUED;
        }
    }
}
