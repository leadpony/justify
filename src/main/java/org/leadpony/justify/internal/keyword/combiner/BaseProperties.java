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

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractChildrenEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractNegatedChildrenEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.ObjectKeyword;

/**
 * Skeletal implementation for "properties" and "patternProperties" keywords.
 * 
 * @author leadpony
 */
public abstract class BaseProperties<K> extends Combiner implements ObjectKeyword {
    
    protected final Map<K, JsonSchema> propertyMap;
    protected AdditionalProperties additionalProperties;
    
    protected BaseProperties() {
        this.propertyMap = new LinkedHashMap<>();
        this.additionalProperties = AdditionalProperties.DEFAULT;
    }
    
    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new PropertiesEvaluator();
    }
    
    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new NegatedPropertiesEvaluator();
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonObjectBuilder propertiesBuilder = builderFactory.createObjectBuilder();
        propertyMap.forEach((key, value)->propertiesBuilder.add(key.toString(), value.toJson()));
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

    protected abstract void findSubschemasFor(String keyName, Collection<JsonSchema> subschemas);
    
    private JsonSchema getDefaultSchema() {
        return additionalProperties.getSubschema();
    }
    
    private class PropertiesEvaluator extends AbstractChildrenEvaluator {

        private final List<JsonSchema> subschemas = new ArrayList<>();
        
        PropertiesEvaluator() {
            super(InstanceType.OBJECT, BaseProperties.this);
        }
        
        @Override
        public void updateChildren(Event event, JsonParser parser, ProblemDispatcher dispatcher) {
            if (event == Event.KEY_NAME) {
                findSubschemaFor(parser.getString());
            } else if (ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                appendEvaluators(type);
            }
        }
        
        private void findSubschemaFor(String keyName) {
            BaseProperties.this.findSubschemasFor(keyName, subschemas);
            if (subschemas.isEmpty()) {
                findDefaultSchemaFor(keyName);
            }
        }
        
        private void findDefaultSchemaFor(String keyName) {
            JsonSchema subschema = getDefaultSchema();
            if (subschema == JsonSchema.FALSE) {
                append(new RedundantPropertyEvaluator(keyName, subschema));
            } else {
                subschemas.add(subschema);
            }
        }

        private void appendEvaluators(InstanceType type) {
            for (JsonSchema subschema : this.subschemas) {
                append(subschema.createEvaluator(type));
            }
            this.subschemas.clear();
        }
    }

    private class NegatedPropertiesEvaluator extends AbstractNegatedChildrenEvaluator {

        private final List<JsonSchema> subschemas = new ArrayList<>();
        
        NegatedPropertiesEvaluator() {
            super(InstanceType.OBJECT, BaseProperties.this);
        }
        
        @Override
        public void updateChildren(Event event, JsonParser parser, ProblemDispatcher dispatcher) {
            if (event == Event.KEY_NAME) {
                findSubschemaFor(parser.getString());
            } else if (ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                appendEvaluators(type);
            }
        }
        
        private void findSubschemaFor(String keyName) {
            BaseProperties.this.findSubschemasFor(keyName, subschemas);
            if (subschemas.isEmpty()) {
                findDefaultSchemaFor(keyName);
            }
        }
        
        private void findDefaultSchemaFor(String keyName) {
            JsonSchema subschema = getDefaultSchema();
            if (subschema == JsonSchema.TRUE) {
                append(new RedundantPropertyEvaluator(keyName, subschema));
            } else {
                subschemas.add(subschema);
            }
        }

        private void appendEvaluators(InstanceType type) {
            for (JsonSchema subschema : this.subschemas) {
                append(subschema.createNegatedEvaluator(type));
            }
            this.subschemas.clear();
        }
    }
}
