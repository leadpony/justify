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
import java.util.stream.Collectors;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.evaluator.EvaluatorDecorator;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * An assertion keyword representing "properties".
 *
 * @author leadpony
 */
@KeywordClass("properties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Properties extends AbstractProperties<String> {

    public static final KeywordType TYPE = KeywordTypes.mappingSchemaMap("properties", Properties::new);

    private final PatternProperties patternProperties;
    private final Map<String, JsonValue> defaultValues;

    public Properties(JsonValue json, Map<String, JsonSchema> propertyMap) {
        this(json, propertyMap, null, null);
    }

    public Properties(JsonValue json, Map<String, JsonSchema> propertyMap,
            PatternProperties patternProperties,
            AdditionalProperties additionalProperties
            ) {
        super(json, propertyMap, additionalProperties);
        this.patternProperties = patternProperties;
        this.defaultValues = generateDefaultValueMap(propertyMap);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Keyword withKeywords(Map<String, Keyword> siblings) {
        PatternProperties patternProperties = null;
        if (siblings.containsKey("patternProperties")) {
            Keyword sibling = siblings.get("patternProperties");
            if (sibling instanceof PatternProperties) {
                patternProperties = (PatternProperties) sibling;
            }
        }

        AdditionalProperties additionalProperties = getAdditionalProperties(siblings);

        if (patternProperties != null || additionalProperties != null) {
            return new Properties(getValueAsJson(), propertyMap,
                    patternProperties,
                    additionalProperties);
        } else {
            return this;
        }
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        return decorateEvaluator(super.createEvaluator(parent, type));
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        return decorateEvaluator(super.createNegatedEvaluator(parent, type));
    }

    @Override
    protected boolean findSubschemas(String keyName, Consumer<JsonSchema> consumer) {
        boolean found = false;
        if (propertyMap.containsKey(keyName)) {
            consumer.accept(propertyMap.get(keyName));
            found = true;
        }
        if (patternProperties != null) {
            if (patternProperties.findSubschemas(keyName, consumer)) {
                found = true;
            }
        }
        return found;
    }

    private static Map<String, JsonValue> generateDefaultValueMap(Map<String, JsonSchema> propertyMap) {
        return propertyMap.entrySet().stream()
            .filter(entry -> entry.getValue().containsKeyword("default"))
            .collect(Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> entry.getValue().defaultValue()));
    }

    private Evaluator decorateEvaluator(Evaluator evaluator) {
        EvaluatorContext context = evaluator.getContext();
        if (context.acceptsDefaultValues() && !defaultValues.isEmpty()) {
            return new PropertiesDefaultEvaluator(evaluator, defaultValues);
        }
        return evaluator;
    }

    /**
     * An evaluator supplying default values.
     *
     * @author leadpony
     */
    private static final class PropertiesDefaultEvaluator extends EvaluatorDecorator {

        private final Map<String, JsonValue> defaultValues;

        private PropertiesDefaultEvaluator(Evaluator evaluator, Map<String, JsonValue> defaultValues) {
            super(evaluator);
            this.defaultValues = new LinkedHashMap<>(defaultValues);
        }

        @Override
        public Result evaluate(Event event, int depth) {
            Result result = super.evaluate(event, depth);
            if (depth == 1 && event == Event.KEY_NAME) {
                defaultValues.remove(getContext().getParser().getString());
            } else if (depth == 0 && event == Event.END_OBJECT) {
                if (!defaultValues.isEmpty()) {
                    supplyDefaultValues();
                }
                return result;
            }
            return Result.PENDING;
        }

        private void supplyDefaultValues() {
            getContext().putDefaultProperties(defaultValues);
        }
    }
}
