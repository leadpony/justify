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

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctiveItemsEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctiveItemsEvaluator;
import org.leadpony.justify.internal.keyword.ArrayEvaluatorSource;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * An applicator keyword representing "contains" keyword.
 *
 * @author leadpony
 */
@KeywordClass("contains")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class SimpleContains extends UnaryApplicator implements ArrayEvaluatorSource {

    public static final KeywordType TYPE = KeywordTypes.mappingSchema("contains", SimpleContains::new);

    public SimpleContains(JsonValue json, JsonSchema subschema) {
        super(subschema);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, InstanceType type, ObjectJsonSchema schema) {
        return createSimpleItemsEvaluator(context, schema);
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type, ObjectJsonSchema schema) {
        return createSimpleNegatedItemsEvaluator(context, schema);
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CHILD;
    }

    protected final Evaluator createSimpleItemsEvaluator(EvaluatorContext context, JsonSchema schema) {
        final JsonSchema subschema = getSubschema();
        return new AbstractDisjunctiveItemsEvaluator(context, schema, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(subschema.createEvaluator(context, type));
                }
            }

            @Override
            protected Message getMessage() {
                return Message.INSTANCE_PROBLEM_CONTAINS;
            }
        };
    }

    protected final Evaluator createSimpleNegatedItemsEvaluator(EvaluatorContext context, JsonSchema schema) {
        final JsonSchema subschema = getSubschema();
        return new AbstractConjunctiveItemsEvaluator(context, schema, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(subschema.createNegatedEvaluator(context, type));
                }
            }
        };
    }
 }
