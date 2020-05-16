/*
 * Copyright 2018-2019 the Justify authors.
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

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.AbstractConjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractKeywordAwareEvaluator;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * Combiner representing "propertyNames" keyword.
 *
 * @author leadpony
 */
@KeywordType("propertyNames")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class PropertyNames extends UnaryApplicator {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromSchema mapper = PropertyNames::new;
        return mapper;
    }

    public PropertyNames(JsonValue json, JsonSchema subschema) {
        super(subschema);
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
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        final JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.FALSE) {
            return createForbiddenPropertiesEvaluator(context, schema, subschema);
        } else {
            return createPropertiesEvaluator(context, schema, subschema);
        }
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        final JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
            return context.createAlwaysFalseEvaluator(subschema);
        } else {
            return createNegatedPropertiesEvaluator(context, schema, subschema);
        }
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CURRENT;
    }

    private Evaluator createForbiddenPropertyEvaluator(EvaluatorContext context, JsonSchema schema,
            JsonSchema subschema) {
        return new AbstractKeywordAwareEvaluator(context, schema, this) {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                ProblemBuilder b = newProblemBuilder()
                        .withMessage(Message.INSTANCE_PROBLEM_OBJECT_NONEMPTY);
                dispatcher.dispatchProblem(b.build());
                return Result.FALSE;
            }
        };
    }

    private Evaluator createForbiddenPropertiesEvaluator(EvaluatorContext context, JsonSchema schema,
            JsonSchema subschema) {
        return new AbstractConjunctivePropertiesEvaluator(context, schema, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(createForbiddenPropertyEvaluator(context, schema, subschema));
                }
            }
        };
    }

    private Evaluator createPropertiesEvaluator(EvaluatorContext context, JsonSchema schema, JsonSchema subschema) {
        return new AbstractConjunctivePropertiesEvaluator(context, schema, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(subschema.createEvaluator(context, InstanceType.STRING));
                }
            }
        };
    }

    private Evaluator createNegatedPropertiesEvaluator(EvaluatorContext context, JsonSchema schema,
            JsonSchema subschema) {
        return new AbstractDisjunctivePropertiesEvaluator(context, schema, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    append(subschema.createNegatedEvaluator(context, InstanceType.STRING));
                }
            }
        };
    }
}
