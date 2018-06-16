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
import java.util.stream.Collectors;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

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
    public Iterable<JsonSchema> getActiveSubschemas() {
        return subschemas;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        LogicalEvaluator logical = createLogicalEvaluator(type);
        this.subschemas.stream()
                .map(s->s.createEvaluator(type))
                .forEach(logical::append);
        return logical;
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeKey(name());
        generator.writeStartArray();
        this.subschemas.forEach(s->s.toJson(generator));
        generator.writeEnd();
    }
    
    protected List<JsonSchema> negateSubschemas() {
        return subschemas.stream().map(JsonSchema::negate).collect(Collectors.toList());
    }
    
    protected abstract LogicalEvaluator createLogicalEvaluator(InstanceType type);
}
