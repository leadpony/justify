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
import java.util.LinkedList;
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
public class AllOf extends NaryBooleanLogicSchema {

    public AllOf(Collection<JsonSchema> subschemas) {
        super(subschemas);
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return new MatchAllEvaluator(type, this.subschemas);
    }

    @Override
    public void toJson(JsonGenerator generator) {
        super.toJson(generator, "allOf");
    }
    
    private static class MatchAllEvaluator implements Evaluator {
        
        private final List<Evaluator> evaluators;
        
        private MatchAllEvaluator(InstanceType type, List<JsonSchema> subschemas) {
            this.evaluators = new LinkedList<>();
            subschemas.stream().map(s->s.createEvaluator(type)).forEach(evaluators::add);
        }

        @Override
        public Status evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            Iterator<Evaluator> it = evaluators.iterator();
            while (it.hasNext()) {
                Evaluator evaluator = it.next();
                Status status = evaluator.evaluate(event, parser, depth, consumer);
                if (status != Status.CONTINUED) {
                    it.remove();
                    if (status == Status.FALSE) {
                        return Status.FALSE;
                    }
                }
            }
            return evaluators.isEmpty() ? Status.TRUE : Status.CONTINUED;
        }
    }
}
