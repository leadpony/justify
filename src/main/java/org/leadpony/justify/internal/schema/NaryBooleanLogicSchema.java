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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
abstract class NaryBooleanLogicSchema extends BooleanLogicSchema {

    private final List<JsonSchema> subschemas;

    protected NaryBooleanLogicSchema(Collection<JsonSchema> subschemas) {
        this.subschemas = new ArrayList<>(subschemas);
    }

    @Override
    public List<JsonSchema> subschemas() {
        return subschemas;
    }

    protected void toJson(JsonGenerator generator, String name) {
        generator.writeKey(name);
        generator.writeStartArray();
        this.subschemas.forEach(s->s.toJson(generator));
        generator.writeEnd();
    }
    
    static abstract class DisjunctionEvaluator implements Evaluator {
        
        protected final List<DelayedEvaluator> running;
        protected List<DelayedEvaluator> failed;

        DisjunctionEvaluator(InstanceType type, List<JsonSchema> subschemas) {
            this.running = createEvaluators(type, subschemas);
        }

        private List<DelayedEvaluator> createEvaluators(InstanceType type, List<JsonSchema> subschemas) {
            List<DelayedEvaluator> evaluators = new LinkedList<>();
            for (JsonSchema schema : subschemas) {
                evaluators.add(new DelayedEvaluator(schema.createEvaluator(type)));
            }
            return evaluators;
        }
        
        protected void addFailed(DelayedEvaluator evaluator) {
            if (failed == null) {
                failed = new ArrayList<>();
            }
            failed.add(evaluator);
        }

        protected Result deliverProblems(Consumer<Problem> consumer) {
            if (failed.isEmpty()) {
                return Result.TRUE;
            }
            Collections.sort(failed, (a, b)->a.countProblems() - b.countProblems());
            DelayedEvaluator first = failed.get(0);
            first.problems().forEach(consumer);
            return Result.FALSE;
        }
    }
}
