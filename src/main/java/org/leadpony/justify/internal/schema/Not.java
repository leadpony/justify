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

import javax.json.JsonObjectBuilder;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;

/**
 * @author leadpony
 */
public class Not extends BooleanLogicSchema {
    
    private final JsonSchema subschema;
    private final JsonSchema negatedSubschema;
    
    public Not(JsonSchema subschema) {
        this(subschema, subschema.negate());
    }

    public Not(JsonSchema subschema, JsonSchema negatedSubschema) {
        this.subschema = subschema;
        this.negatedSubschema = negatedSubschema;
    }
    
    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return negatedSubschema.createEvaluator(type);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        builder.add(name(), this.subschema.toJson());
    }

    @Override
    public String name() {
        return "not";
    }

    @Override
    protected AbstractJsonSchema createNegatedSchema() {
        // Swaps affirmation and negation.
        return new Not(negatedSubschema, subschema);
    }
}
