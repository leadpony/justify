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

import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
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

    public SimpleContains(JsonSchema subschema) {
        super(subschema);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        return createSimpleItemsEvaluator(parent);
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        return createSimpleNegatedItemsEvaluator(parent);
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CHILD;
    }

    protected final Evaluator createSimpleItemsEvaluator(Evaluator parent) {
        final JsonSchema subschema = getSubschema();
        return new AbstractDisjunctiveItemsEvaluator(parent, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(subschema.createEvaluator(parent.getContext(), type));
                }
            }

            @Override
            protected Message getMessage() {
                return Message.INSTANCE_PROBLEM_CONTAINS;
            }
        };
    }

    protected final Evaluator createSimpleNegatedItemsEvaluator(Evaluator parent) {
        final JsonSchema subschema = getSubschema();
        return new AbstractConjunctiveItemsEvaluator(parent, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(subschema.createNegatedEvaluator(parent.getContext(), type));
                }
            }
        };
    }
 }
