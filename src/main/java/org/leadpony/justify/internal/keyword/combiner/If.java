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

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.ConditionalEvaluator;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * "If" conditional keyword.
 * 
 * @author leadpony
 */
public class If extends Conditional {
    
    private JsonSchema thenSchema;
    private JsonSchema elseSchema;

    public If(JsonSchema schema) {
        super(schema);
    }
    
    @Override
    public String name() {
        return "if";
    }

    @Override
    public boolean canEvaluate() {
        return true;
    }
    
    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender) {
        if (thenSchema == null && elseSchema == null) {
            return;
        }
        Evaluator ifEvaluator = getSchema().createEvaluator(type);
        Evaluator thenEvaluator = thenSchema != null ?
                thenSchema.createEvaluator(type) : null;
        Evaluator elseEvaluator = elseSchema != null ?
                elseSchema.createEvaluator(type) : null;
        appender.append(new ConditionalEvaluator(ifEvaluator, thenEvaluator, elseEvaluator));
    }

    @Override
    public void configure(Map<String, Keyword> others) {
        if (others.containsKey("then")) {
            thenSchema = ((Conditional)others.get("then")).getSchema();
        }
        if (others.containsKey("else")) {
            elseSchema = ((Conditional)others.get("else")).getSchema();
        }
    }
}
