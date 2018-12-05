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

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Evaluator.Result;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.AbstractConjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.Evaluators;

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
        final JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.FALSE) {
            return createForbiddenPropertiesEvaluator(subschema);
        } else {
            return createPropertiesEvaluator(subschema);
        }
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        final JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
            return Evaluators.alwaysFalse(subschema);
        } else {
            return createNegatedPropertiesEvaluator(subschema);
        }
    }
    
    private Evaluator createForbiddenPropertyEvaluator(JsonSchema subschema) {
        return (event, parser, depth, dispatcher)->{
            ProblemBuilder b = createProblemBuilder(parser)
                .withMessage("instance.problem.object.nonempty");
            dispatcher.dispatchProblem(b.build());
            return Result.FALSE;
        };
    }
    
    private Evaluator createForbiddenPropertiesEvaluator(JsonSchema subschema) {
        return new AbstractConjunctivePropertiesEvaluator(this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(createForbiddenPropertyEvaluator(subschema));
                }
            }
        };
    }
    
    private Evaluator createPropertiesEvaluator(JsonSchema subschema) {
        return new AbstractConjunctivePropertiesEvaluator(this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(subschema.createEvaluator(InstanceType.STRING));
                }
            }
        };
    }

    private Evaluator createNegatedPropertiesEvaluator(JsonSchema subschema) {
        return new AbstractDisjunctivePropertiesEvaluator(this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(subschema.createNegatedEvaluator(InstanceType.STRING));
                }
            }
        };
    }
}
