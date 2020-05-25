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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
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

    private PatternProperties patternProperties;
    private Map<String, JsonValue> defaultValues;

    public Properties(JsonValue json, Map<String, JsonSchema> subschemas) {
        super(json, subschemas);
        for (Map.Entry<String, JsonSchema> entry : subschemas.entrySet()) {
            JsonSchema subschema = entry.getValue();
            if (subschema.containsKeyword("default")) {
                addDefaultValue(entry.getKey(), subschema.defaultValue());
            }
        }
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
        super.link(siblings);
        if (siblings.containsKey("patternProperties")) {
            this.patternProperties = (PatternProperties) siblings.get("patternProperties");
        }
    }

    @Override
    public Optional<JsonSchema> findSchema(String token) {
        if (propertyMap.containsKey(token)) {
            return Optional.of(propertyMap.get(token));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        return decorateEvaluator(super.createEvaluator(context, schema, type), context);
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        return decorateEvaluator(super.createNegatedEvaluator(context, schema, type), context);
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

    private void addDefaultValue(String key, JsonValue defaultValue) {
        if (defaultValues == null) {
            defaultValues = new LinkedHashMap<>();
        }
        defaultValues.put(key, defaultValue);
    }

    private Evaluator decorateEvaluator(Evaluator evaluator, EvaluatorContext context) {
        if (context.acceptsDefaultValues() && defaultValues != null) {
            return new PropertiesDefaultEvaluator(evaluator, context, defaultValues);
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

        private PropertiesDefaultEvaluator(Evaluator evaluator, EvaluatorContext context,
                Map<String, JsonValue> defaultValues) {
            super(evaluator, context);
            this.defaultValues = new LinkedHashMap<>(defaultValues);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            Result result = super.evaluate(event, depth, dispatcher);
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
