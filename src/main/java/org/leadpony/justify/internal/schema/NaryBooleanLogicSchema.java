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
import java.util.List;
import java.util.Optional;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.Combiner;

/**
 * N-ary boolean logic schema.
 * 
 * @author leadpony
 */
abstract class NaryBooleanLogicSchema extends BooleanLogicSchema {

    private final List<JsonSchema> subschemas;

    protected NaryBooleanLogicSchema(Collection<JsonSchema> subschemas) {
        this.subschemas = new ArrayList<>(subschemas);
    }

    @Override
    public Optional<Evaluator> createEvaluator(InstanceType type) {
        Combiner combiner = createCombiner();
        this.subschemas.stream()
                .map(s->s.createEvaluator(type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(combiner::append);
        return combiner.getCombined();
    }

    @Override
    public List<JsonSchema> subschemas() {
        return subschemas;
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeKey(name());
        generator.writeStartArray();
        this.subschemas.forEach(s->s.toJson(generator));
        generator.writeEnd();
    }
    
    protected abstract Combiner createCombiner();
}
