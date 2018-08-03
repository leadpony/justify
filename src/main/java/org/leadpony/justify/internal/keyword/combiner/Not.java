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

import javax.json.JsonBuilderFactory;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * Boolean logic specified with "not" validation keyword.
 * 
 * @author leadpony
 */
class Not extends UnaryCombiner {
    
    private final JsonSchema negatedSubschema;
    
    Not(JsonSchema subschema) {
        this(subschema, subschema.negate());
    }

    private Not(JsonSchema subschema, JsonSchema negatedSubschema) {
        super(subschema);
        this.negatedSubschema = negatedSubschema;
    }
    
    @Override
    public String name() {
        return "not";
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        Evaluator evaluator = negatedSubschema.createEvaluator(type, getEvaluatorFactory());
        if (evaluator != null) {
            appender.append(evaluator);
        }
    }

    @Override
    public Keyword negate() {
        // Swaps affirmation and negation.
        return new Not(negatedSubschema, getSubschema());
    }
}
