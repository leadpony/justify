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
public class JsonSchemas {
    
    private static final JsonSchema ALWAYS_TRUE = new AbstractJsonSchema() {
        
        @Override
        public Evaluator createEvaluator(InstanceType type) {
            Objects.requireNonNull(type, "type must not be null.");
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.write(true);
        }
    };
    
    private static final JsonSchema ALWAYS_FALSE = new AbstractJsonSchema() {
        
        @Override
        public Evaluator createEvaluator(InstanceType type) {
            Objects.requireNonNull(type, "type must not be null.");
            return NEGATIVE_EVALUATOR;
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.write(false);
        }
    };
    
    private static final Evaluator NEGATIVE_EVALUATOR = new Evaluator() {
 
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            Problem p = ProblemBuilder.newBuilder()
                    .withMessage("instance.problem.unknown")
                    .build();
            consumer.accept(p);
            return Result.FALSE;
        }
    };
    
    private static final JsonSchema EMPTY = new AbstractJsonSchema() {
        
        @Override
        public Evaluator createEvaluator(InstanceType type) {
            Objects.requireNonNull(type, "type must not be null.");
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public void toJson(JsonGenerator generator) {
            Objects.requireNonNull(generator, "generator must not be null.");
            generator.writeStartObject().writeEnd();
        }
    };

    public static JsonSchema alwaysTrue() {
        return ALWAYS_TRUE;
    }

    public static JsonSchema alwaysFalse() {
        return ALWAYS_FALSE;
    }

    public static JsonSchema empty() {
        return EMPTY;
    }
}
