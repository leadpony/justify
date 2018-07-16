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

import java.util.Objects;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * JSON schema with any subschemas, including child schemas.
 * 
 * @author leadpony
 */
class CompositeSchema extends SimpleSchema {
    
    private NavigableSchemaMap subschemaMap;
    
    CompositeSchema(DefaultSchemaBuilder builder) {
        super(builder);
        this.subschemaMap = builder.getSubschemaMap();
    }

    /**
     * Copy constructor.
     * 
     * @param original the original schema.
     * @param negating {@code true} if this schema is negation of the original.
     */
    CompositeSchema(CompositeSchema original, boolean negating) {
        super(original, negating);
        assert negating;
        this.subschemaMap = original.subschemaMap;
    }

    @Override
    public JsonSchema findSubschema(String jsonPointer) {
        Objects.requireNonNull(jsonPointer, "jsonPointer must not be null.");
        if (jsonPointer.isEmpty()) {
            return this;
        } else {
            return subschemaMap.getSchema(jsonPointer);
        }
    }
    
    @Override
    public Iterable<JsonSchema> getSubschemas() {
        return this.subschemaMap.values();
    }

    @Override
    public JsonSchema negate() {
        return new Negated(this);
    }
    
    /**
     * Negated type of enclosing class.
     *  
     * @author leadpony
     */
    private static class Negated extends CompositeSchema {
        
        private Negated(CompositeSchema original) {
            super(original, true);
        }
        
        @Override
        protected LogicalEvaluator.Builder createLogicalEvaluator(InstanceType type) {
            return Evaluators.newDisjunctionEvaluatorBuilder(type);
        } 
    }
}
