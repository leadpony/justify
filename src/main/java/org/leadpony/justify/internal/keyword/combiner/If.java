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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.Map;

import javax.json.JsonBuilderFactory;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.ConditionalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * "If" conditional keyword.
 * 
 * @author leadpony
 */
class If extends UnaryCombiner {
    
    private JsonSchema thenSchema;
    private JsonSchema elseSchema;

    If(JsonSchema schema) {
        super(schema);
    }
    
    @Override
    public String name() {
        return "if";
    }

    @Override
    public boolean canEvaluate() {
        return thenSchema != null || elseSchema != null;
    }
    
    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        Evaluator ifEvaluator = getSubschema().createEvaluator(type);
        Evaluator thenEvaluator = getThenSchema().createEvaluator(type);
        Evaluator elseEvaluator = getElseSchema().createEvaluator(type);
        return new ConditionalEvaluator(ifEvaluator, thenEvaluator, elseEvaluator);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        Evaluator ifEvaluator = getSubschema().createEvaluator(type);
        Evaluator thenEvaluator = getThenSchema().createNegatedEvaluator(type);
        Evaluator elseEvaluator = getElseSchema().createNegatedEvaluator(type);
        return new ConditionalEvaluator(ifEvaluator, thenEvaluator, elseEvaluator);
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
        if (siblings.containsKey("then")) {
            thenSchema = ((UnaryCombiner)siblings.get("then")).getSubschema();
        }
        if (siblings.containsKey("else")) {
            elseSchema = ((UnaryCombiner)siblings.get("else")).getSubschema();
        }
    }
    
    private JsonSchema getThenSchema() {
        return (thenSchema != null) ? thenSchema : JsonSchema.TRUE;
    }

    private JsonSchema getElseSchema() {
        return (elseSchema != null) ? elseSchema : JsonSchema.TRUE;
    }
}
