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

import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.evaluator.DefaultEvaluatorFactory;
import org.leadpony.justify.internal.evaluator.DynamicLogicalEvaluator;

/**
 * @author leadpony
 */
class BroadcastItems extends UnaryCombiner implements Items {
    
    BroadcastItems(JsonSchema subschema) {
        super(subschema);
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type == InstanceType.ARRAY) {
            appender.append(new ArrayItemSchemaEvaluator(createDynamicEvaluator()));
        }
    }
    
    @Override
    public BroadcastItems negate() {
        return new Negated(getSubschema());
    }

    /**
     * Creates evaluator to combine the evaluations of children.
     * 
     * @return newly created evaluator.
     */
    protected DynamicLogicalEvaluator createDynamicEvaluator() {
        return DefaultEvaluatorFactory.SINGLETON.createDynamicConjunctionEvaluator(InstanceType.ARRAY);
    }
    
    class ArrayItemSchemaEvaluator extends AbstractChildSchemaEvaluator {

        ArrayItemSchemaEvaluator(DynamicLogicalEvaluator dynamicEvaluator) {
            super(dynamicEvaluator);
        }
        
        @Override
        protected void update(Event event, JsonParser parser, Consumer<Problem> reporter) {
            if (ParserEvents.isValue(event)) {
                Evaluator evaluator = createChildEvaluator(event, parser);
                if (evaluator != null) {
                    appendChild(evaluator);
                }
            }
        }
        
        protected Evaluator createChildEvaluator(Event event, JsonParser parser) {
            InstanceType type = ParserEvents.toInstanceType(event, parser);
            return getSubschema().createEvaluator(type, getEvaluatorFactory());
        }
    }
    
    private static class Negated extends BroadcastItems {

        private Negated(JsonSchema subschema) {
            super(subschema.negate());
        }

        @Override
        protected DynamicLogicalEvaluator createDynamicEvaluator() {
            return DefaultEvaluatorFactory.SINGLETON.createDynamicDisjunctionEvaluator(InstanceType.ARRAY, this);
        }
    }
}
