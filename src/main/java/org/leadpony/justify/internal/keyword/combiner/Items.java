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
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.DynamicChildrenEvaluator;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * "items" combiner.
 * 
 * @author leadpony
 */
abstract class Items extends Combiner {
    
    @Override
    public String name() {
        return "items";
    }

    /**
     * @author leadpony
     */
    static class BroadcastItems extends Items {

        private final JsonSchema subschema;
        
        BroadcastItems(JsonSchema subschema) {
            this.subschema = subschema;
        }

        @Override
        public void createEvaluator(InstanceType type, EvaluatorAppender appender, 
                JsonBuilderFactory builderFactory, boolean affirmative) {
            if (type == InstanceType.ARRAY) {
                appender.append(new SingleSchemaEvaluator(affirmative, this, this.subschema));
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
    }
    
    private static abstract class DynamicItemsEvaluator extends DynamicChildrenEvaluator {
        
        protected int currentItemIndex;
        
        protected DynamicItemsEvaluator(boolean affirmative, ProblemBuilderFactory problemBuilderFactory) {
            super(affirmative, Event.END_ARRAY, problemBuilderFactory);
        }
        
        @Override
        protected void append(JsonSchema schema, InstanceType type) {
            if (schema == getSchemaToFail()) {
                append(new RedundantItemEvaluator(currentItemIndex, schema));
            } else {
                super.append(schema, type);
            }
            currentItemIndex++;
        }
    }

    private static class SingleSchemaEvaluator extends DynamicItemsEvaluator {
        
        private final JsonSchema subschema;

        SingleSchemaEvaluator(boolean affirmative, ProblemBuilderFactory problemBuilderFactory, JsonSchema subschema) {
            super(affirmative, problemBuilderFactory);
            this.subschema = subschema;
        }
        
        @Override
        protected void update(Event event, JsonParser parser, Consumer<Problem> reporter) {
            if (ParserEvents.isValue(event)) {
                append(this.subschema, ParserEvents.toInstanceType(event, parser));
            }
        }
    }

    /**
     * "items" with array of subschemas.
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
        public void createEvaluator(InstanceType type, EvaluatorAppender appender, 
                JsonBuilderFactory builderFactory, boolean affirmative) {
            if (type == InstanceType.ARRAY) {
                appender.append(new SeparateSchemaEvaluator(affirmative, this));
            }
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
        
        private class SeparateSchemaEvaluator extends DynamicItemsEvaluator {

            private SeparateSchemaEvaluator(boolean affirmative, ProblemBuilderFactory problemBuilderFactory) {
                super(affirmative, problemBuilderFactory);
            }

            @Override
            protected void update(Event event, JsonParser parser, Consumer<Problem> reporter) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toInstanceType(event, parser);
                    appendSubschema(type);
                }
            }
      
            protected void appendSubschema(InstanceType type) {
                JsonSchema subschema = null;
                if (currentItemIndex < subschemas.size()) {
                    subschema = subschemas.get(currentItemIndex);
                } else {
                    subschema = additionalItems.getSubschema();
                }
                append(subschema, type);
            }
        }
    }
}
