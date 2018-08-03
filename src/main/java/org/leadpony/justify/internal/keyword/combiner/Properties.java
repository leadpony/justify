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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.base.JsonSchemas;
import org.leadpony.justify.internal.evaluator.DynamicLogicalEvaluator;
import org.leadpony.justify.internal.evaluator.DefaultEvaluatorFactory;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * "properties" subschema combiner.
 * 
 * @author leadpony
 */
public class Properties extends BaseProperties<String> {

    private PatternProperties patternProperties;

    Properties() {
        super(new LinkedHashMap<>(), AdditionalProperties.DEFAULT);
    }
    
    Properties(Map<String, JsonSchema> propertyMap, 
            AdditionalProperties additionalProperties,
            PatternProperties patternProperties) {
        super(propertyMap, additionalProperties);
        this.patternProperties = patternProperties;
    }
    
    @Override
    public String name() {
        return "properties";
    }

    @Override
    public Properties negate() {
        Properties original = this;
        return new Properties(
                JsonSchemas.negate(this.propertyMap),
                this.additionalProperties.negate(),
                this.patternProperties != null ?
                        this.patternProperties.negate() : null
                ) {
            
            @Override
            public Properties negate() {
                return original;
            }

            @Override
            protected DynamicLogicalEvaluator createDynamicEvaluator() {
                return DefaultEvaluatorFactory.SINGLETON.createDynamicDisjunctionEvaluator(InstanceType.OBJECT, original);
            }
        };
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
        super.link(siblings);
        if (siblings.containsKey("patternProperties")) {
            this.patternProperties = (PatternProperties)siblings.get("patternProperties");
        }
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
    protected JsonSchema findSubschemas(String keyName, Collection<JsonSchema> subschemas) {
        if (propertyMap.containsKey(keyName)) {
            subschemas.add(propertyMap.get(keyName));
        }
        if (patternProperties != null) {
            return patternProperties.findSubschemas(keyName, subschemas);
        } else if (subschemas.isEmpty()) {
            return super.findSubschemas(keyName, subschemas);
        } else {
            return null;
        }
    }
}
