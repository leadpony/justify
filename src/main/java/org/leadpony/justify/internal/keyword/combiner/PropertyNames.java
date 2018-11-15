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
import java.util.Set;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.AbstractChildrenEvaluator;

/**
 * Combiner representing "propertyNames" keyword.
 * 
 * @author leadpony
 */
class PropertyNames extends UnaryCombiner {

    PropertyNames(JsonSchema subschema) {
        super(subschema);
    }

    @Override
    public String name() {
        return "propertyNames";
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
        return new SubschemaEvaluator(true, this, getSubschema());
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new SubschemaEvaluator(false, this, getSubschema());
    }

    private static class SubschemaEvaluator extends AbstractChildrenEvaluator {

        private final JsonSchema subschema;
        
        SubschemaEvaluator(boolean affirmative, ProblemBuilderFactory problemFactory, JsonSchema subschema) {
            super(affirmative, InstanceType.OBJECT, problemFactory);
            this.subschema = subschema;
        }

        @Override
        protected void update(Event event, JsonParser parser, ProblemDispatcher dispatcher) {
            if (event == Event.KEY_NAME) {
                append(subschema, InstanceType.STRING);
            }
        }
    }
}
