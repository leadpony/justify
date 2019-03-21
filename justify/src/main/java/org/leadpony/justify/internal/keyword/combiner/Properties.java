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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.JsonValue;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.evaluator.EvaluatorDecorator;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * An assertion keyword representing "properties".
 *
 * @author leadpony
 */
public class Properties extends AbstractProperties<String> {

    private PatternProperties patternProperties;
    private Map<String, JsonValue> defaultValues;

    Properties() {
    }

    @Override
    public String name() {
        return "properties";
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, Keyword> keywords) {
        super.addToEvaluatables(evaluatables, keywords);
        if (keywords.containsKey("patternProperties")) {
            this.patternProperties = (PatternProperties) keywords.get("patternProperties");
        }
        evaluatables.add(this);
    }

    @Override
    public JsonSchema getSubschema(Iterator<String> jsonPointer) {
        if (jsonPointer.hasNext()) {
            return propertyMap.get(jsonPointer.next());
        } else {
            return null;
        }
    }

    @Override
    public void addProperty(String key, JsonSchema subschema) {
        super.addProperty(key, subschema);
        if (subschema.containsKeyword("default")) {
            addDefaultValue(key, subschema.defaultValue());
        }
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        return decorateEvaluator(super.doCreateEvaluator(context, type), context);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        return decorateEvaluator(super.doCreateNegatedEvaluator(context, type), context);
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
        if (defaultValues != null) {
            return new PropertiesDefaultEvaluator(evaluator, context, defaultValues);
        }
        return evaluator;
    }

    /**
     * An evaluator supplying default values.
     *
     * @author leadpony
     */
    private static class PropertiesDefaultEvaluator extends EvaluatorDecorator {

        private final Map<String, JsonValue> defaultValues;

        private PropertiesDefaultEvaluator(Evaluator evaluator, EvaluatorContext context, Map<String, JsonValue> defaultValues) {
            super(evaluator, context);
            this.defaultValues = new LinkedHashMap<>(defaultValues);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            Result result = super.evaluate(event, depth, dispatcher);
            if (depth == 1 && event == Event.KEY_NAME) {
                defaultValues.remove(getParser().getString());
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
