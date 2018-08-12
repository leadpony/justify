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
import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.evaluator.DynamicChildrenEvaluator;
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
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, 
            JsonBuilderFactory builderFactory, boolean affirmative) {
        assert enabled;
        if (type == InstanceType.OBJECT) {
            appender.append(new ProperySchemaEvaluator(affirmative, this));
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
    
    void findSubshcmeas(String keyName, Collection<JsonSchema> subschemas) {
        assert subschemas.isEmpty();
        subschemas.add(getSubschema());
    }
    
    class ProperySchemaEvaluator extends DynamicChildrenEvaluator {

        private JsonSchema nextSubschema;
        
        ProperySchemaEvaluator(boolean affirmative, ProblemBuilderFactory problemBuilderFactory) {
            super(affirmative, InstanceType.OBJECT, problemBuilderFactory);
        }

        @Override
        protected void update(Event event, JsonParser parser, Consumer<Problem> reporter) {
            if (event == Event.KEY_NAME) {
                findSubschema(parser.getString());
            } else if (ParserEvents.isValue(event)) {
                if (nextSubschema != null) {
                    InstanceType type = ParserEvents.toInstanceType(event, parser);
                    append(nextSubschema, type);
                    nextSubschema = null;
                }
            }
        }
        
        private void findSubschema(String keyName) {
            JsonSchema subschema = getSubschema();
            if (subschema == getSchemaToFail()) {
                append(new RedundantPropertyEvaluator(keyName, subschema));
            } else {
                nextSubschema = subschema;
            }
        }
    }
}
