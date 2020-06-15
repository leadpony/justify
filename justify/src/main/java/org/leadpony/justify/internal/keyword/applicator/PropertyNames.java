/*
 * Copyright 2018-2020 the Justify authors.
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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.EnumSet;
import java.util.Set;

import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractConjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractKeywordBasedEvaluator;
import org.leadpony.justify.internal.keyword.KeywordTypes;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * Combiner representing "propertyNames" keyword.
 *
 * @author leadpony
 */
@KeywordClass("propertyNames")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class PropertyNames extends UnaryApplicator {

    public static final KeywordType TYPE = KeywordTypes.mappingSchema("propertyNames", PropertyNames::new);

    public PropertyNames(JsonSchema subschema) {
        super(subschema);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
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
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        final JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.FALSE) {
            return createForbiddenPropertiesEvaluator(parent, subschema);
        } else {
            return createPropertiesEvaluator(parent, subschema);
        }
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        final JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
            return parent.getContext().createAlwaysFalseEvaluator(subschema);
        } else {
            return createNegatedPropertiesEvaluator(parent, subschema);
        }
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CURRENT;
    }

    private Evaluator createForbiddenPropertyEvaluator(Evaluator parent, JsonSchema subschema) {
        return new AbstractKeywordBasedEvaluator(parent, this) {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                ProblemBuilder b = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_OBJECT_NONEMPTY);
                dispatcher.dispatchProblem(b.build());
                return Result.FALSE;
            }
        };
    }

    private Evaluator createForbiddenPropertiesEvaluator(Evaluator parent, JsonSchema subschema) {
        return new AbstractConjunctivePropertiesEvaluator(parent, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(createForbiddenPropertyEvaluator(parent, subschema));
                }
            }
        };
    }

    private Evaluator createPropertiesEvaluator(Evaluator parent, JsonSchema subschema) {
        return new AbstractConjunctivePropertiesEvaluator(parent, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(subschema.createEvaluator(getContext(), InstanceType.STRING));
                }
            }
        };
    }

    private Evaluator createNegatedPropertiesEvaluator(Evaluator parent, JsonSchema subschema) {
        return new AbstractDisjunctivePropertiesEvaluator(parent, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(subschema.createNegatedEvaluator(getContext(), InstanceType.STRING));
                }
            }
        };
    }
}
