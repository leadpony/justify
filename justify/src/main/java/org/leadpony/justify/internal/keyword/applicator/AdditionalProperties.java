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
import java.util.Optional;
import java.util.Set;

import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.EvaluatorSource;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
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
public final class AdditionalProperties extends UnaryApplicator {

    static final KeywordType TYPE = KeywordTypes.mappingSchema("additionalProperties",
            AdditionalProperties::of);

    private static final AdditionalProperties FALSE = new AdditionalProperties(JsonSchema.FALSE);

    public static AdditionalProperties of(JsonSchema schema) {
        if (schema == JsonSchema.FALSE) {
            return FALSE;
        } else {
            return new AdditionalProperties(schema);
        }
    }

    private AdditionalProperties(JsonSchema subschema) {
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
    public Optional<EvaluatorSource> getEvaluatorSource(Map<String, Keyword> siblings) {
        if (!siblings.containsKey("properties") && !siblings.containsKey("patternProperties")) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean canEvaluate() {
        return true;
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
        if (getSubschema() == JsonSchema.FALSE) {
            return createForbiddenPropertiesEvaluator(parent);
        } else {
            return createPropertiesEvaluator(parent);
        }
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        JsonSchema subschema = getSubschema();
        if (subschema == JsonSchema.TRUE || subschema == JsonSchema.EMPTY) {
            return createNegatedForbiddenPropertiesEvaluator(parent);
        } else {
            return createNegatedPropertiesEvaluator(parent);
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
    private Evaluator createPropertiesEvaluator(Evaluator parent) {
        JsonSchema subschema = getSubschema();
        return new AbstractConjunctivePropertiesEvaluator(parent, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(parent -> subschema.createEvaluator(parent, type));
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
    private Evaluator createNegatedPropertiesEvaluator(Evaluator parent) {
        JsonSchema subschema = getSubschema();
        return new AbstractDisjunctivePropertiesEvaluator(parent, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(parent -> subschema.createNegatedEvaluator(parent, type));
                }
            }
        };
    }

    private Evaluator createForbiddenPropertiesEvaluator(Evaluator parent) {
        return new AbstractConjunctivePropertiesEvaluator(parent, this) {
            private String keyName;

            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    keyName = parser.getString();
                } else if (ParserEvents.isValue(event)) {
                    append(parent -> createRedundantPropertyEvaluator(parent, keyName));
                }
            }
        };
    }

    private Evaluator createNegatedForbiddenPropertiesEvaluator(Evaluator parent) {
        return new AbstractDisjunctivePropertiesEvaluator(parent, this) {
            private String keyName;

            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (event == Event.KEY_NAME) {
                    keyName = parser.getString();
                } else if (ParserEvents.isValue(event)) {
                    append(parent -> createRedundantPropertyEvaluator(parent, keyName));
                }
            }
        };
    }

    private Evaluator createRedundantPropertyEvaluator(Evaluator parent, String keyName) {
        return new RedundantPropertyEvaluator(parent, getSubschema(), keyName);
    }
}
