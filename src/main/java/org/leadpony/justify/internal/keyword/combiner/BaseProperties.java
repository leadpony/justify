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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.DynamicLogicalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * Skeletal implementation for "properties" and "patternProperties" keywords.
 * 
 * @author leadpony
 */
public abstract class BaseProperties<K> implements Combiner {
    
    protected final Map<K, JsonSchema> propertyMap;
    protected AdditionalProperties additionalProperties;
    
    protected BaseProperties() {
        this(new LinkedHashMap<>(), AdditionalProperties.DEFAULT);
    }
    
    protected BaseProperties(Map<K, JsonSchema> propertyMap, AdditionalProperties additionalProperties) {
        this.propertyMap = propertyMap;
        this.additionalProperties = additionalProperties;
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type == InstanceType.OBJECT) {
            appender.append(new ProperySchemaEvaluator(createDynamicEvaluator()));
        }
    }
    
    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonObjectBuilder propertiesBuilder = builderFactory.createObjectBuilder();
        propertyMap.forEach((key, value)->builder.add(key.toString(), value.toJson()));
        builder.add(name(), propertiesBuilder.build());
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
        if (siblings.containsKey("additionalProperties")) {
            this.additionalProperties = (AdditionalProperties)siblings.get("additionalProperties");
        }
    }
    
    @Override
    public boolean hasSubschemas() {
        return !propertyMap.isEmpty();
    }
    
    @Override
    public Stream<JsonSchema> subschemas() {
        return propertyMap.values().stream();
    }
    
    public void addProperty(K key, JsonSchema subschema) {
        propertyMap.put(key, subschema);
    }

    protected DynamicLogicalEvaluator createDynamicEvaluator() {
        return Evaluators.newConjunctionChildEvaluator(InstanceType.OBJECT);
    }
    
    protected JsonSchema findSubschemas(String keyName, Collection<JsonSchema> subschemas) {
        assert subschemas.isEmpty();
        return additionalProperties.findSubshcmeas(keyName, subschemas);
    }
    
    private class ProperySchemaEvaluator extends AbstractChildSchemaEvaluator {

        private final List<JsonSchema> subschemas = new ArrayList<>();
        
        ProperySchemaEvaluator(DynamicLogicalEvaluator dynamicEvaluator) {
            super(dynamicEvaluator);
        }

        @Override
        protected void update(Event event, JsonParser parser, Reporter reporter) {
            if (event == Event.KEY_NAME) {
                findSubschemas(parser.getString());
            } else if (ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                appendEvaluators(type);
            }
        }
        
        private void findSubschemas(String keyName) {
            JsonSchema immediate = BaseProperties.this.findSubschemas(keyName, this.subschemas);
            if (immediate != null) {
                appendChild(immediate.createEvaluator(InstanceType.OBJECT));
            }
        }
        
        private void appendEvaluators(InstanceType type) {
            for (JsonSchema subschema : this.subschemas) {
                appendChild(subschema.createEvaluator(type));
            }
            this.subschemas.clear();
        }
    }
}
