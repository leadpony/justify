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

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * "additionalItems" keyword.
 * 
 * @author leadpony
 */
class AdditionalProperties extends UnaryCombiner {
    
    static final AdditionalProperties DEFAULT = new AdditionalProperties(JsonSchema.TRUE);
    private boolean enabled;
    
    AdditionalProperties(JsonSchema subschema) {
        this(subschema, false);
    }

    AdditionalProperties(JsonSchema subschema, boolean enabled) {
        super(subschema);
        this.enabled = enabled;
    }

    @Override
    public String name() {
        return "additionalProperties";
    }

    @Override
    public boolean canEvaluate() {
        return enabled;
    }
  
    @Override
    public boolean supportsType(InstanceType type) {
        return type == InstanceType.OBJECT;
    }

    @Override
    public Set<InstanceType> getSupportedTypes() {
        return EnumSet.of(InstanceType.OBJECT);
    }
    
    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        assert enabled;
        if (getSubschema() == JsonSchema.FALSE) {
            return createForbiddenPropertiesEvaluator();
        } else {
            return createPropertiesEvaluator();
        }
    }
    
    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        assert enabled;
        JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
            return createNegatedForbiddenPropertiesEvaluator();
        } else {
            return createNegatedPropertiesEvaluator();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * If there are neither "properties" nor "patternProperties",
     * make this keyword to be evaluated.
     * </p>
     */
    @Override
    public void link(Map<String, Keyword> siblings) {
        enabled = !siblings.containsKey("properties") &&
                  !siblings.containsKey("patternProperties");
    }
    
    private Evaluator createSubschemaEvaluator(Event event, JsonParser parser) {
        InstanceType type = ParserEvents.toInstanceType(event, parser);
        return getSubschema().createEvaluator(type);
    }
     
    private Evaluator createNegatedSubschemaEvaluator(Event event, JsonParser parser) {
        InstanceType type = ParserEvents.toInstanceType(event, parser);
        return getSubschema().createNegatedEvaluator(type);
    }

    /**
     * Create an evaluator which evaluates the subschema for all properties in the object.
     * @return newly created evaluator.
     */
    private Evaluator createPropertiesEvaluator() {
        return new AbstractConjunctivePropertiesEvaluator(this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    append(createSubschemaEvaluator(event, parser));
                }
            }
        };
    }

    /**
     * Create an evaluator which evaluates the negated subschema for all properties in the object.
     * @return newly created evaluator.
     */
    private Evaluator createNegatedPropertiesEvaluator() {
        return new AbstractDisjunctivePropertiesEvaluator(this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    append(createNegatedSubschemaEvaluator(event, parser));
                }
            }
        };
    }
    
    private Evaluator createForbiddenPropertiesEvaluator() {
        return new AbstractConjunctivePropertiesEvaluator(this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(createRedundantPropertyEvaluator(parser.getString()));
                }
            }
        };
    }
    
    private Evaluator createNegatedForbiddenPropertiesEvaluator() {
        return new AbstractDisjunctivePropertiesEvaluator(this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(createRedundantPropertyEvaluator(parser.getString()));
                }
            }
        };
    }

    private Evaluator createRedundantPropertyEvaluator(String keyName) {
        return new RedundantPropertyEvaluator(keyName, getSubschema());
    }
}
