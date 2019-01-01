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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctiveItemsEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctiveItemsEvaluator;
import org.leadpony.justify.internal.keyword.ArrayKeyword;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * "items" combiner.
 * 
 * @author leadpony
 */
abstract class Items extends Combiner implements ArrayKeyword {
    
    @Override
    public String name() {
        return "items";
    }
    
    protected Evaluator createRedundantItemEvaluator(int itemIndex, JsonSchema subschema) {
        return new RedundantItemEvaluator(itemIndex, subschema);
    }

    /**
     * "items" keyword with single schema.
     * 
     * @author leadpony
     */
    static class BroadcastItems extends Items {

        private final JsonSchema subschema;
        
        BroadcastItems(JsonSchema subschema) {
            this.subschema = subschema;
        }

        @Override
        protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
            if (subschema == JsonSchema.FALSE) {
                return createForbiddenItemsEvaluator();
            } else {
                return createItemsEvaluator();
            }
        }

        @Override
        protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
            if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
                return createNegatedForbiddenItemsEvaluator();
            } else {
                return createNegatedItemsEvaluator();
            }
        }

        @Override
        public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            builder.add(name(), subschema.toJson());
        }
        
        @Override
        public boolean hasSubschemas() {
            return true;
        }

        @Override
        public Stream<JsonSchema> subschemas() {
            return Stream.of(subschema);
        }

        @Override
        public JsonSchema getSubschema(Iterator<String> jsonPointer) {
            return subschema;
        }
        
        private Evaluator createSubschemaEvaluator(Event event, JsonParser parser) {
            InstanceType type = ParserEvents.toInstanceType(event, parser);
            return subschema.createEvaluator(type);
        }
        
        private Evaluator createItemsEvaluator() {
            return new AbstractConjunctiveItemsEvaluator(this) {
                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        append(createSubschemaEvaluator(event, parser));
                    }
                }
            };
        }
        
        private Evaluator createNegatedSubschemaEvaluator(Event event, JsonParser parser) {
            InstanceType type = ParserEvents.toInstanceType(event, parser);
            return subschema.createNegatedEvaluator(type);
        }

        private Evaluator createNegatedItemsEvaluator() {
            return new AbstractDisjunctiveItemsEvaluator(this) {
                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        append(createNegatedSubschemaEvaluator(event, parser));
                    }
                }
            };
        }

        private Evaluator createForbiddenItemsEvaluator() {
            return new AbstractConjunctiveItemsEvaluator(this) {
                private int itemIndex;
                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        append(createRedundantItemEvaluator(itemIndex++, subschema));
                    }
                }
            };
        }
        
        private Evaluator createNegatedForbiddenItemsEvaluator() {
            return new AbstractDisjunctiveItemsEvaluator(this) {
                private int itemIndex;
                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        append(createRedundantItemEvaluator(itemIndex++, subschema));
                    }
                }
            };
        }
    }
    
    /**
     * "items" keyword with array of subschemas.
     * 
     * @author leadpony
     */
    static class SeparateItems extends Items {
        
        private final List<JsonSchema> subschemas;
        private AdditionalItems additionalItems = AdditionalItems.DEFAULT;
        
        SeparateItems(List<JsonSchema> subschemas) {
            this.subschemas = subschemas;
        }

        @Override
        protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
            return createItemsEvaluator();
        }
        
        @Override
        protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
            return createNegatedItemsEvaluator();
        }

        @Override
        public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            JsonArrayBuilder itemsBuilder = builderFactory.createArrayBuilder();
            subschemas.stream().map(JsonSchema::toJson).forEachOrdered(itemsBuilder::add);
            builder.add(name(), itemsBuilder.build());
        }
        
        @Override
        public void addToEvaluatables(List<Keyword> evaluatables, Map<String, Keyword> keywords) {
            if (keywords.containsKey("additionalItems")) {
                this.additionalItems = (AdditionalItems)keywords.get("additionalItems");
            }
            evaluatables.add(this);
        }
      
        @Override
        public boolean hasSubschemas() {
            return !subschemas.isEmpty();
        }
        
        @Override
        public Stream<JsonSchema> subschemas() {
            return this.subschemas.stream();
        }
        
        @Override
        public JsonSchema getSubschema(Iterator<String> jsonPointer) {
            if (jsonPointer.hasNext()) {
                try {
                    int index = Integer.parseInt(jsonPointer.next());
                    if (index < subschemas.size()) {
                        return subschemas.get(index);
                    }
                } catch (NumberFormatException e) {
                }
            }
            return null;
        }
        
        private JsonSchema findSubschemaAt(int itemIndex) {
            if (itemIndex < subschemas.size()) {
                return subschemas.get(itemIndex);
            } else {
                return additionalItems.getSubschema();
            }
        }
        
        private Evaluator createSubschemaEvaluator(int itemIndex, JsonSchema subschema, InstanceType type) {
            if (subschema == JsonSchema.FALSE) {
                return createRedundantItemEvaluator(itemIndex, subschema); 
            } else {
                return subschema.createEvaluator(type);
            }
        }

        private Evaluator createNegatedSubschemaEvaluator(int itemIndex, JsonSchema subschema, InstanceType type) {
            if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
                return createRedundantItemEvaluator(itemIndex, subschema); 
            } else {
                return subschema.createNegatedEvaluator(type);
            }
        }

        private Evaluator createItemsEvaluator() {
            return new AbstractConjunctiveItemsEvaluator(this) {
                private int itemIndex;
                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        InstanceType type = ParserEvents.toInstanceType(event, parser);
                        JsonSchema subschema = findSubschemaAt(itemIndex);
                        append(createSubschemaEvaluator(itemIndex, subschema, type));
                        ++itemIndex;
                    }
                }
            };
        }
        
        private Evaluator createNegatedItemsEvaluator() {
            return new AbstractDisjunctiveItemsEvaluator(this) {
                private int itemIndex;
                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        InstanceType type = ParserEvents.toInstanceType(event, parser);
                        JsonSchema subschema = findSubschemaAt(itemIndex);
                        append(createNegatedSubschemaEvaluator(itemIndex, subschema, type));
                        ++itemIndex;
                    }
                }
            };
        }
    }
}
