/*
 * Copyright 2018-2020 the Justify authors.
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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordParser;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Maps;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctiveItemsEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctiveItemsEvaluator;
import org.leadpony.justify.internal.evaluator.EvaluatorDecorator;

/**
 * A keyword type representing "items".
 *
 * @author leadpony
 */
@KeywordClass("items")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public abstract class Items extends AbstractArrayApplicatorKeyword  {

    public static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return "items";
        }

        @Override
        public Keyword parse(KeywordParser parser, JsonBuilderFactory factory) {
            switch (parser.next()) {
            case START_ARRAY:
                JsonArrayBuilder builder = factory.createArrayBuilder();
                List<JsonSchema> schemas = new ArrayList<>();
                while (parser.hasNext() && parser.next() != Event.END_ARRAY) {
                    if (parser.canGetSchema()) {
                        JsonSchema schema = parser.getSchema();
                        schemas.add(schema);
                        builder.add(schema.toJson());
                    } else {
                        failed(parser, builder);
                    }
                }
                return Items.of(builder.build(), schemas);
            case START_OBJECT:
            case VALUE_TRUE:
            case VALUE_FALSE:
                JsonSchema schema = parser.getSchema();
                return Items.of(schema.toJson(), schema);
            default:
                return failed(parser);
            }
        }
    };

    public static Items of(JsonValue json, JsonSchema subschema) {
        return new BroadcastItems(json, subschema);
    }

    public static Items of(JsonValue json, List<JsonSchema> subschemas) {
        return new DiscreteItems(json, subschemas);
    }

    protected Items(JsonValue json) {
        super(json);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CHILD;
    }

    /**
     * "items" keyword with single schema.
     *
     * @author leadpony
     */
    static class BroadcastItems extends Items {

        private final JsonSchema subschema;

        BroadcastItems(JsonValue json, JsonSchema subschema) {
            super(json);
            this.subschema = subschema;
        }

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            if (subschema == JsonSchema.FALSE) {
                return createForbiddenItemsEvaluator(parent);
            } else {
                return createItemsEvaluator(parent);
            }
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
                return createNegatedForbiddenItemsEvaluator(parent);
            } else {
                return createNegatedItemsEvaluator(parent);
            }
        }

        @Override
        public boolean containsSchemas() {
            return true;
        }

        @Override
        public Map<String, JsonSchema> getSchemasAsMap() {
            return Maps.of("", subschema);
        }

        @Override
        public Stream<JsonSchema> getSchemasAsStream() {
            return Stream.of(subschema);
        }

        private Evaluator createItemsEvaluator(Evaluator parent) {
            JsonSchema subschema = this.subschema;
            return new AbstractConjunctiveItemsEvaluator(parent, this) {
                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        InstanceType type = ParserEvents.toBroadInstanceType(event);
                        append(p -> subschema.createEvaluator(p, type));
                    }
                }
            };
        }

        private Evaluator createNegatedItemsEvaluator(Evaluator parent) {
            JsonSchema subschema = this.subschema;
            return new AbstractDisjunctiveItemsEvaluator(parent, this) {
                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        InstanceType type = ParserEvents.toBroadInstanceType(event);
                        append(p -> subschema.createNegatedEvaluator(p, type));
                    }
                }
            };
        }

        private Evaluator createForbiddenItemsEvaluator(Evaluator parent) {
            return new AbstractConjunctiveItemsEvaluator(parent, this) {
                private int itemIndex;

                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        append(p -> new RedundantItemEvaluator(p, subschema, itemIndex++));
                    }
                }
            };
        }

        private Evaluator createNegatedForbiddenItemsEvaluator(Evaluator parent) {
            return new AbstractDisjunctiveItemsEvaluator(parent, this) {
                private int itemIndex;

                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        append(p -> new RedundantItemEvaluator(p, subschema, itemIndex++));
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
    static class DiscreteItems extends Items {

        private final List<JsonSchema> subschemas;
        private final JsonSchema defaultSchema;
        private final List<JsonValue> defaultValues;

        DiscreteItems(JsonValue json, List<JsonSchema> subschemas) {
            this(json, subschemas, JsonSchema.TRUE);
        }

        DiscreteItems(JsonValue json, List<JsonSchema> subschemas, JsonSchema defaultSchema) {
            super(json);
            this.subschemas = subschemas;
            this.defaultSchema = defaultSchema;
            this.defaultValues = findDefaultValues(subschemas);
        }

        @Override
        public Keyword withKeywords(Map<String, Keyword> siblings) {
            if (siblings.containsKey("additionalItems")) {
                AdditionalItems additionalItems = (AdditionalItems) siblings.get("additionalItems");
                JsonSchema defaultSchema = additionalItems.getSubschema();
                return new DiscreteItems(getValueAsJson(), this.subschemas, defaultSchema);
            } else {
                return this;
            }
        }

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            return decorateEvaluator(createItemsEvaluator(parent));
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            return decorateEvaluator(createNegatedItemsEvaluator(parent));
        }

        @Override
        public boolean containsSchemas() {
            return !subschemas.isEmpty();
        }

        @Override
        public Map<String, JsonSchema> getSchemasAsMap() {
            Map<String, JsonSchema> map = new LinkedHashMap<>();
            for (int i = 0; i < subschemas.size(); i++) {
                map.put(String.valueOf(i), subschemas.get(i));
            }
            return map;
        }

        @Override
        public Stream<JsonSchema> getSchemasAsStream() {
            return this.subschemas.stream();
        }

        private JsonSchema findSubschemaAt(int itemIndex) {
            if (itemIndex < subschemas.size()) {
                return subschemas.get(itemIndex);
            } else {
                return defaultSchema;
            }
        }

        private Evaluator createItemEvaluator(Evaluator parent, int itemIndex, JsonSchema subschema,
                InstanceType type) {
            if (subschema == JsonSchema.FALSE) {
                return new RedundantItemEvaluator(parent, subschema, itemIndex);
            } else {
                return subschema.createEvaluator(parent, type);
            }
        }

        private Evaluator createNegatedItemEvaluator(Evaluator parent, int itemIndex, JsonSchema subschema,
                InstanceType type) {
            if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
                return new RedundantItemEvaluator(parent, subschema, itemIndex);
            } else {
                return subschema.createNegatedEvaluator(parent, type);
            }
        }

        private Evaluator createItemsEvaluator(Evaluator parent) {
            return new AbstractConjunctiveItemsEvaluator(parent, this) {
                private int itemIndex;

                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        InstanceType type = ParserEvents.toBroadInstanceType(event);
                        JsonSchema subschema = findSubschemaAt(itemIndex);
                        append(p -> createItemEvaluator(p, itemIndex, subschema, type));
                        ++itemIndex;
                    }
                }
            };
        }

        private Evaluator createNegatedItemsEvaluator(Evaluator parent) {
            return new AbstractDisjunctiveItemsEvaluator(parent, this) {
                private int itemIndex;

                @Override
                public void updateChildren(Event event, JsonParser parser) {
                    if (ParserEvents.isValue(event)) {
                        InstanceType type = ParserEvents.toBroadInstanceType(event);
                        JsonSchema subschema = findSubschemaAt(itemIndex);
                        append(p -> createNegatedItemEvaluator(p, itemIndex, subschema, type));
                        ++itemIndex;
                    }
                }
            };
        }

        private List<JsonValue> findDefaultValues(List<JsonSchema> subschemas) {
            for (int i = subschemas.size() - 1; i >= 0; i--) {
                if (subschemas.get(i).containsKeyword("default")) {
                    List<JsonValue> values = new ArrayList<>(i + 1);
                    for (int j = 0; j <= i; j++) {
                        values.add(subschemas.get(j).defaultValue());
                    }
                    return values;
                }
            }
            return null;
        }

        private Evaluator decorateEvaluator(Evaluator evaluator) {
            EvaluatorContext context = evaluator.getContext();
            if (context.acceptsDefaultValues() && defaultValues != null) {
                evaluator = new ItemsDefaultEvaluator(evaluator, defaultValues);
            }
            return evaluator;
        }
    }

    /**
     * An evaluator which will inject default values of items.
     *
     * @author leadpony
     */
    private static class ItemsDefaultEvaluator extends EvaluatorDecorator {

        private final List<JsonValue> defaultValues;
        private int size;

        ItemsDefaultEvaluator(Evaluator evaluator, List<JsonValue> defaultValues) {
            super(evaluator);
            this.defaultValues = defaultValues;
        }

        @Override
        public Result evaluate(Event event, int depth) {
            Result result = super.evaluate(event, depth);
            if (depth == 1 && ParserEvents.isValue(event)) {
                ++size;
            } else if (depth == 0 && event == Event.END_ARRAY) {
                supplyDefaultValues(size);
                return result;
            }
            return Result.PENDING;
        }

        private void supplyDefaultValues(int size) {
            if (size < defaultValues.size()) {
                List<JsonValue> valuesToPut = new ArrayList<>();
                int i = size;
                while (i < defaultValues.size()) {
                    JsonValue value = defaultValues.get(i++);
                    if (value != null) {
                        valuesToPut.add(value);
                    } else {
                        break;
                    }
                }
                if (!valuesToPut.isEmpty()) {
                    getContext().putDefaultItems(valuesToPut);
                }
            }
        }
    }
}
