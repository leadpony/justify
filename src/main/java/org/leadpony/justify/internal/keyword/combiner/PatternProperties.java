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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.DynamicLogicalEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * @author leadpony
 */
public class PatternProperties extends BaseProperties<Pattern> {
    
    private boolean enabled;
    
    PatternProperties() {
        this(new LinkedHashMap<>(), AdditionalProperties.DEFAULT, false);
    }

    PatternProperties(Map<Pattern, JsonSchema> propertyMap, 
            AdditionalProperties additionalProperties,
            boolean enabled) {
        super(propertyMap, additionalProperties);
        this.enabled = enabled;
    }

    @Override
    public String name() {
        return "patternProperties";
    }

    @Override
    public boolean canEvaluate() {
        return enabled;
    }
    
    @Override
    public PatternProperties negate() {
        return new Negated(this);
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
        super.link(siblings);
        enabled = !siblings.containsKey("properties");
    }
    
    @Override
    protected JsonSchema findSubschemas(String keyName, Collection<JsonSchema> subschemas) {
        for (Pattern pattern : propertyMap.keySet()) {
            Matcher m = pattern.matcher(keyName);
            if (m.find()) {
                subschemas.add(propertyMap.get(pattern));
            }
        }
        if (subschemas.isEmpty()) {
            return super.findSubschemas(keyName, subschemas);
        } else {
            return null;
        }
    }
    
    private class Negated extends PatternProperties {
        
        private Negated(PatternProperties original) {
            super(negateSchemaMap(original.propertyMap),
                  original.additionalProperties.negate(),
                  original.enabled); 
        }
        
        public PatternProperties negate() {
            return PatternProperties.this;
        }
  
        protected DynamicLogicalEvaluator createDynamicEvaluator() {
            return Evaluators.newDisjunctionChildEvaluator(InstanceType.OBJECT);
        }
    }
}
