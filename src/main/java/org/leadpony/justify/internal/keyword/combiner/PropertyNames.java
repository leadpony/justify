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

import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.AbstractChildrenEvaluator;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

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
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, 
            JsonBuilderFactory builderFactory, boolean affirmative) {
        if (type == InstanceType.OBJECT) {
            appender.append(new SubschemaEvaluator(affirmative, this, getSubschema()));
        }
    }

    private static class SubschemaEvaluator extends AbstractChildrenEvaluator {

        private final JsonSchema subschema;
        
        SubschemaEvaluator(boolean affirmative, ProblemBuilderFactory problemFactory, JsonSchema subschema) {
            super(affirmative, InstanceType.OBJECT, problemFactory);
            this.subschema = subschema;
        }

        @Override
        protected void update(Event event, JsonParser parser, Consumer<Problem> reporter) {
            if (event == Event.KEY_NAME) {
                append(subschema, InstanceType.STRING);
            }
        }
    }
}
