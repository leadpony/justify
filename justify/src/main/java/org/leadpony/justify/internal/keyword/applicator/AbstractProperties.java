/*
 * Copyright 2018, 2020 the Justify authors.
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctivePropertiesEvaluator;

/**
 * A skeletal implementation for "properties" and "patternProperties" keywords.
 *
 * @param <K> the type of schema keys.
 *
 * @author leadpony
 */
public abstract class AbstractProperties<K> extends AbstractObjectApplicatorKeyword {

    protected final Map<K, JsonSchema> propertyMap;
    private final JsonSchema defaultSchema;

    protected AbstractProperties(JsonValue json,
            Map<K, JsonSchema> propertyMap,
            AdditionalProperties additionalProperties) {
        super(json);
        this.propertyMap = propertyMap;
        this.defaultSchema = (additionalProperties != null)
                ? additionalProperties.getSubschema() : JsonSchema.TRUE;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType typep) {
        return new PropertiesEvaluator(parent, this, this.defaultSchema);
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        return new NegatedPropertiesEvaluator(parent, this, this.defaultSchema);
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CHILD;
    }

    @Override
    public boolean containsSchemas() {
        return !propertyMap.isEmpty();
    }

    @Override
    public Map<String, JsonSchema> getSchemasAsMap() {
        Map<String, JsonSchema> map = new LinkedHashMap<>();
        propertyMap.forEach((k, v) -> map.put(k.toString(), v));
        return map;
    }

    @Override
    public Stream<JsonSchema> getSchemasAsStream() {
        return propertyMap.values().stream();
    }

    protected final AdditionalProperties getAdditionalProperties(Map<String, Keyword> siblings) {
        if (siblings.containsKey("additionalProperties")) {
            return (AdditionalProperties) siblings.get("additionalProperties");
        }
        return null;
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

        PropertiesEvaluator(Evaluator parent, Keyword keyword, JsonSchema defaultSchema) {
            super(parent, keyword);
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
                append(parent -> new RedundantPropertyEvaluator(parent, subschema, currentKeyName));
            } else {
                append(parent -> subschema.createEvaluator(parent, currentType));
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

        NegatedPropertiesEvaluator(Evaluator parent, Keyword keyword,
                JsonSchema defaultSchema) {
            super(parent, keyword);
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
                append(parent -> new RedundantPropertyEvaluator(parent, subschema, currentKeyName));
            } else {
                append(parent -> subschema.createNegatedEvaluator(parent, currentType));
            }
        }
    }
}
