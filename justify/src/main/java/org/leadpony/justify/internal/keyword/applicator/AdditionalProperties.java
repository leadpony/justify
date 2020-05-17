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
import java.util.Map;
import java.util.Set;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctivePropertiesEvaluator;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * A keyword representing "additionalItems".
 *
 * @author leadpony
 */
@KeywordClass("additionalProperties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class AdditionalProperties extends UnaryApplicator {

    public static final KeywordType TYPE = KeywordTypes.mappingSchema("additionalProperties",
            AdditionalProperties::new);

    private boolean alone;

    public AdditionalProperties(JsonValue json, JsonSchema subschema) {
        super(subschema);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If there are neither "properties" nor "patternProperties", the instance must
     * evaluated by this keyword.
     * </p>
     */
    @Override
    public Keyword link(Map<String, Keyword> siblings) {
        alone = !siblings.containsKey("properties") && !siblings.containsKey("patternProperties");
        return this;
    }

    @Override
    public boolean canEvaluate() {
        return alone;
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
        if (getSubschema() == JsonSchema.FALSE) {
            return createForbiddenPropertiesEvaluator(context, schema);
        } else {
            return createPropertiesEvaluator(context, schema);
        }
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
            return createNegatedForbiddenPropertiesEvaluator(context, schema);
        } else {
            return createNegatedPropertiesEvaluator(context, schema);
        }
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CHILD;
    }

    /**
     * Create an evaluator which evaluates the subschema for all properties in the
     * object.
     *
     * @return newly created evaluator.
     */
    private Evaluator createPropertiesEvaluator(EvaluatorContext context, JsonSchema schema) {
        JsonSchema subschema = getSubschema();
        return new AbstractConjunctivePropertiesEvaluator(context, schema, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(subschema.createEvaluator(getContext(), type));
                }
            }
        };
    }

    /**
     * Create an evaluator which evaluates the negated subschema for all properties
     * in the object.
     *
     * @return newly created evaluator.
     */
    private Evaluator createNegatedPropertiesEvaluator(EvaluatorContext context, JsonSchema schema) {
        JsonSchema subschema = getSubschema();
        return new AbstractDisjunctivePropertiesEvaluator(context, schema, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(subschema.createNegatedEvaluator(getContext(), type));
                }
            }
        };
    }

    private Evaluator createForbiddenPropertiesEvaluator(EvaluatorContext context, JsonSchema schema) {
        return new AbstractConjunctivePropertiesEvaluator(context, schema, this) {
            private String keyName;

            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    keyName = parser.getString();
                } else if (ParserEvents.isValue(event)) {
                    append(createRedundantPropertyEvaluator(context, keyName));
                }
            }
        };
    }

    private Evaluator createNegatedForbiddenPropertiesEvaluator(EvaluatorContext context, JsonSchema schema) {
        return new AbstractDisjunctivePropertiesEvaluator(context, schema, this) {
            private String keyName;

            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    keyName = parser.getString();
                } else if (ParserEvents.isValue(event)) {
                    append(createRedundantPropertyEvaluator(context, keyName));
                }
            }
        };
    }

    private Evaluator createRedundantPropertyEvaluator(EvaluatorContext context, String keyName) {
        return new RedundantPropertyEvaluator(context, getSubschema(), keyName);
    }
}
