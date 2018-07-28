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
import java.util.Map;

import javax.json.JsonBuilderFactory;
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
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        assert enabled;
        if (type == InstanceType.OBJECT) {
            appender.append(new ProperySchemaEvaluator(createDynamicEvaluator()));
        }
    }
    
    @Override
    public AdditionalProperties negate() {
        return new Negated(this);
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
        enabled = !siblings.containsKey("properties") &&
                  !siblings.containsKey("patternProperties");
    }
    
    JsonSchema findSubshcmeas(String keyName, Collection<JsonSchema> subschemas) {
        assert subschemas.isEmpty();
        JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.FALSE) {
            return new RedundantPropertySchema(keyName, this);
        } else {
            subschemas.add(subschema);
            return null;
        }
    }

    protected DynamicLogicalEvaluator createDynamicEvaluator() {
        return Evaluators.newConjunctionChildEvaluator(InstanceType.OBJECT);
    }
    
    class ProperySchemaEvaluator extends AbstractChildSchemaEvaluator {

        private JsonSchema nextSubschema;
        
        ProperySchemaEvaluator(DynamicLogicalEvaluator dynamicEvaluator) {
            super(dynamicEvaluator);
        }

        @Override
        protected void update(Event event, JsonParser parser, Reporter reporter) {
            if (event == Event.KEY_NAME) {
                findSubschema(parser.getString());
            } else if (ParserEvents.isValue(event)) {
                if (nextSubschema != null) {
                    InstanceType type = ParserEvents.toInstanceType(event, parser);
                    appendChild(nextSubschema.createEvaluator(type));
                    nextSubschema = null;
                }
            }
        }
        
        private void findSubschema(String keyName) {
            JsonSchema subschema = getSubschema();
            if (subschema == JsonSchema.FALSE) {
                subschema = new RedundantPropertySchema(keyName, AdditionalProperties.this);
                appendChild(subschema.createEvaluator(InstanceType.OBJECT));
            } else {
                nextSubschema = subschema;
            }
        }
    }
    
    private static class Negated extends AdditionalProperties {

        Negated(AdditionalProperties original) {
            super(original.getSubschema().negate(), original.enabled);
        }

        protected DynamicLogicalEvaluator createDynamicEvaluator() {
            return Evaluators.newDisjunctionChildEvaluator(InstanceType.OBJECT);
        }
    }
}
