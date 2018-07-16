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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.ExtendableLogicalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * "properties" subschema combiner.
 * 
 * @author leadpony
 */
public class Properties extends AbstractProperties<String> {

    private PatternProperties patternProperties;

    Properties() {
        this(new LinkedHashMap<>(), AdditionalProperties.DEFAULT);
    }
    
    Properties(Map<String, JsonSchema> propertyMap, AdditionalProperties additionalProperties) {
        super(propertyMap, additionalProperties);
    }
    
    @Override
    public String name() {
        return "properties";
    }

    @Override
    public Properties negate() {
        return new Negated(this);
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
        super.link(siblings);
        if (siblings.containsKey("patternProperties")) {
            this.patternProperties = (PatternProperties)siblings.get("patternProperties");
        }
    }
    
    @Override
    protected void findSubschemas(String keyName, List<JsonSchema> subschemas) {
        if (propertyMap.containsKey(keyName)) {
            subschemas.add(propertyMap.get(keyName));
        }
        if (patternProperties != null) {
            patternProperties.findSubschemas(keyName, subschemas);
        }
    }

    private static class Negated extends Properties {
        
        private Negated(Properties original) {
            super(negateSchemaMap(original.propertyMap),
                    original.additionalProperties.negate()); 
        }
        
        protected ExtendableLogicalEvaluator createDynamicEvaluator() {
            return Evaluators.newDisjunctionChildEvaluator(InstanceType.OBJECT);
        }
    }
}
