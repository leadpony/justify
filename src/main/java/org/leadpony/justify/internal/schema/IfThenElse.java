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

import java.util.Optional;

import javax.json.JsonObjectBuilder;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.ConditionalEvaluator;

/**
 * A set of schemas representing "if", "then", and "else" keywords.
 *  
 * @author leadpony
 */
public class IfThenElse extends AbstractJsonSchema implements SchemaComponent {
    
    private final JsonSchema ifSchema;
    private final Optional<JsonSchema> thenSchema;
    private final Optional<JsonSchema> elseSchema;
    
    public IfThenElse(JsonSchema ifSchema, JsonSchema thenSchema, JsonSchema elseSchema) {
        this.ifSchema = ifSchema;
        this.thenSchema = Optional.ofNullable(thenSchema);
        this.elseSchema = Optional.ofNullable(elseSchema);
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        if (!thenSchema.isPresent() && !elseSchema.isPresent()) {
            return null;
        }
        Evaluator ifEvaluator = ifSchema.createEvaluator(type);
        Evaluator thenEvaluator = createEvaluator(thenSchema, type);
        Evaluator elseEvaluator = createEvaluator(elseSchema, type);
        return new ConditionalEvaluator(ifEvaluator, thenEvaluator, elseEvaluator);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        builder.add("if", ifSchema.toJson());
        thenSchema.ifPresent(schema->{
            builder.add("then", schema.toJson());
        });
        elseSchema.ifPresent(schema->{
            builder.add("else", schema.toJson());
        });
    }
    
    @Override
    protected AbstractJsonSchema createNegatedSchema() {
        throw new UnsupportedOperationException();
    }
    
    private static Evaluator createEvaluator(Optional<JsonSchema> schema, InstanceType type) {
        return schema.isPresent() ?
                schema.get().createEvaluator(type) : null;
    }
}
