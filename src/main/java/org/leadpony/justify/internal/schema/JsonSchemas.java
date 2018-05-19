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
import java.util.Optional;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;

/**
 * Utility class operating on {@link JsonSchema} instances.
 * 
 * @author leadpony
 */
public interface JsonSchemas {
    
    JsonSchema ALWAYS_TRUE = new AbstractJsonSchema() {
        
        @Override
        public Optional<Evaluator> createEvaluator(InstanceType type) {
            Objects.requireNonNull(type, "type must not be null.");
            return Optional.of(Evaluators.ALWAYS_TRUE);
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.write(true);
        }
    };
    
    JsonSchema ALWAYS_FALSE = new AbstractJsonSchema() {
        
        @Override
        public Optional<Evaluator> createEvaluator(InstanceType type) {
            Objects.requireNonNull(type, "type must not be null.");
            return Optional.of(Evaluators.ALWAYS_FALSE);
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.write(false);
        }
    };
    
    JsonSchema EMPTY = new AbstractJsonSchema() {
        
        @Override
        public Optional<Evaluator> createEvaluator(InstanceType type) {
            Objects.requireNonNull(type, "type must not be null.");
            return Optional.of(Evaluators.ALWAYS_TRUE);
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.writeStartObject().writeEnd();
        }
    };
}
