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

package org.leadpony.justify.api;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * Skeletal implementation of simple JSON schema.
 * 
 * @author leadpony
 */
abstract class SimpleJsonSchema implements JsonSchema {

    protected final Evaluator ALWAYS_TRUE;
    protected final Evaluator ALWAYS_FALSE;
    
    protected SimpleJsonSchema() {
        final JsonSchema self = this;
        
        this.ALWAYS_TRUE = new Evaluator() {
            @Override
            public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
                return Result.TRUE;
            }
            
            @Override
            public boolean isAlwaysTrue() {
                return true;
            }
        };
        
        this.ALWAYS_FALSE = new Evaluator() {
            @Override
            public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
                dispatcher.dispatchInevitableProblem(parser, self);
                return Result.FALSE;
            }
            
            @Override
            public boolean isAlwaysTrue() {
                return false;
            }
        };
    }
}