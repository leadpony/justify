/*
 * Copyright 2018-2019 the Justify authors.
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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.SchemaKeyword;
import org.leadpony.justify.internal.keyword.ObjectKeyword;

/**
 * A skeletal implementation for "properties" and "patternProperties" keywords.
 *
 * @param <K> the type of schema keys.
 *
 * @author leadpony
 */
public abstract class AbstractProperties<K> extends Applicator implements ObjectKeyword {

    protected final Map<K, JsonSchema> propertyMap;
    private JsonSchema defaultSchema;

    protected AbstractProperties(JsonValue json, Map<K, JsonSchema> properties) {
        super(json);
        this.propertyMap = properties;
        this.defaultSchema = JsonSchema.TRUE;
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        return new PropertiesEvaluator(context, defaultSchema);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        return new NegatedPropertiesEvaluator(context, defaultSchema);
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, SchemaKeyword> keywords) {
        if (keywords.containsKey("additionalProperties")) {
            AdditionalProperties additionalProperties = (AdditionalProperties) keywords.get("additionalProperties");
            this.defaultSchema = additionalProperties.getSubschema();
        }
    }

    @Override
    public boolean hasSubschemas() {
        return !propertyMap.isEmpty();
    }

    @Override
    public Stream<JsonSchema> getSubschemas() {
        return propertyMap.values().stream();
    }

    protected abstract boolean findSubschemas(String keyName, Consumer<JsonSchema> consumer);

    /**
     * An evaluator of this keyword.
     *
     * @author leadpony
     */
    private class PropertiesEvaluator extends AbstractConjunctivePropertiesEvaluator implements Consumer<JsonSchema> {

        private final JsonSchema defaultSchema;
        private String currentKeyName;
        private InstanceType currentType;

        PropertiesEvaluator(EvaluatorContext context, JsonSchema defaultSchema) {
            super(context);
            this.defaultSchema = defaultSchema;
        }

        @Override
        public void updateChildren(Event event, JsonParser parser) {
            if (event == Event.KEY_NAME) {
                currentKeyName = parser.getString();
            } else if (ParserEvents.isValue(event)) {
                currentType = ParserEvents.toBroadInstanceType(event);
                if (!findSubschemas(currentKeyName, this)) {
                    accept(defaultSchema);
                }
            }
        }

        /* Consumer */

        @Override
        public void accept(JsonSchema subschema) {
            if (subschema == JsonSchema.FALSE) {
                append(new RedundantPropertyEvaluator(getContext(), currentKeyName, JsonSchema.FALSE));
            } else {
                append(subschema.createEvaluator(getContext(), currentType));
            }
        }
    }

    /**
     * An evaluator of the negeted version of this keyword.
     *
     * @author leadpony
     */
    private class NegatedPropertiesEvaluator extends AbstractDisjunctivePropertiesEvaluator
            implements Consumer<JsonSchema> {

        private final JsonSchema defaultSchema;
        private String currentKeyName;
        private InstanceType currentType;

        NegatedPropertiesEvaluator(EvaluatorContext context, JsonSchema defaultSchema) {
            super(context, AbstractProperties.this);
            this.defaultSchema = defaultSchema;
        }

        @Override
        public void updateChildren(Event event, JsonParser parser) {
            if (event == Event.KEY_NAME) {
                currentKeyName = parser.getString();
            } else if (ParserEvents.isValue(event)) {
                currentType = ParserEvents.toBroadInstanceType(event);
                if (!findSubschemas(currentKeyName, this)) {
                    accept(defaultSchema);
                }
            }
        }

        /* Consumer */

        @Override
        public void accept(JsonSchema subschema) {
            if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
                append(new RedundantPropertyEvaluator(getContext(), currentKeyName, subschema));
            } else {
                append(subschema.createNegatedEvaluator(getContext(), currentType));
            }
        }
    }
}
