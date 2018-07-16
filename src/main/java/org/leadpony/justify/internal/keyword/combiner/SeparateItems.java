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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.ExtendableLogicalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * "items" with array of subschemas.
 * 
 * @author leadpony
 */
class SeparateItems implements Items {
    
    private final List<JsonSchema> subschemas;
    private AdditionalItems additionalItems = AdditionalItems.DEFAULT;
    
    SeparateItems(List<JsonSchema> subschemas) {
        this.subschemas = subschemas;
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type == InstanceType.ARRAY) {
            appender.append(new ArrayItemSchemaEvaluator(createDynamicEvaluator()));
        }
    }
    
    @Override
    public SeparateItems negate() {
        return new Negated(this.subschemas);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonArrayBuilder itemsBuilder = builderFactory.createArrayBuilder();
        subschemas.stream().map(JsonSchema::toJson).forEachOrdered(itemsBuilder::add);
        builder.add(name(), itemsBuilder.build());
    }
    
    @Override
    public void link(Map<String, Keyword> siblings) {
        if (siblings.containsKey("additionalItems")) {
            this.additionalItems = (AdditionalItems)siblings.get("additionalItems");
        }
    }
    
    /**
     * Creates evaluator to combine the evaluations of children.
     * 
     * @return newly created evaluator.
     */
    protected ExtendableLogicalEvaluator createDynamicEvaluator() {
        return Evaluators.newConjunctionChildEvaluator(InstanceType.ARRAY);
    }
    
    private class ArrayItemSchemaEvaluator extends AbstractChildSchemaEvaluator {

        private int currentIndex;
        
        private ArrayItemSchemaEvaluator(ExtendableLogicalEvaluator dynamicEvaluator) {
            super(dynamicEvaluator);
            this.currentIndex = 0;
        }

        @Override
        protected void update(Event event, JsonParser parser, Reporter reporter) {
            if (ParserEvents.isValue(event)) {
                JsonSchema subschema = findSubschema(currentIndex);
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                Evaluator evaluator = subschema.createEvaluator(type);
                if (evaluator != null) {
                    appendChild(evaluator);
                }
                currentIndex++;
            }
        }
  
        private JsonSchema findSubschema(int itemIndex) {
            if (itemIndex < subschemas.size()) {
                return subschemas.get(itemIndex);
            } else {
                return additionalItems.getSubschemaAt(itemIndex);
            }
        }
    }

    private static class Negated extends SeparateItems {

        Negated(List<JsonSchema> subschemas) {
            super(negateSubschemas(subschemas));
        }
        
        @Override
        protected ExtendableLogicalEvaluator createDynamicEvaluator() {
            return Evaluators.newDisjunctionChildEvaluator(InstanceType.ARRAY);
        }

        private static List<JsonSchema> negateSubschemas(List<JsonSchema> subschemas) {
            return subschemas.stream()
                    .map(JsonSchema::negate)
                    .collect(Collectors.toList());
        }
    }
}
