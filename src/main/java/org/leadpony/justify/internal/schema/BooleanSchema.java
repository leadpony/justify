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

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;

/**
 * Boolean-type JSON schema.
 * 
 * @author leadpony
 */
public class BooleanSchema implements JsonSchema {
    
    /**
     * The boolean schema which is always evaluated to true.
     */
    public static final BooleanSchema TRUE = new BooleanSchema(true);

    /**
     * The boolean schema which is always evaluated to false.
     */
    public static final BooleanSchema FALSE = new BooleanSchema(false) {
        @Override
        public Collection<Evaluator> createEvaluators(InstanceType type) {
            return Arrays.asList(FalseEvaluator.singleton);
        }
    };

    private final boolean value;
    
    private BooleanSchema(boolean value) {
        this.value = value;
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.write(value);
    }
    
    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    public static JsonSchema valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    private static class FalseEvaluator implements Evaluator {
        
        private static final Evaluator singleton = new FalseEvaluator();

        @Override
        public Status evaluate(Event event, JsonParser parser, Consumer<Problem> collector) {
            return Status.FALSE;
        }
    }
}
