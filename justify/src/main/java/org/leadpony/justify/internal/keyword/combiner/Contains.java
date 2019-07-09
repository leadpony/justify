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

package org.leadpony.justify.internal.keyword.combiner;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.evaluator.AbstractConjunctiveItemsEvaluator;
import org.leadpony.justify.internal.evaluator.AbstractDisjunctiveItemsEvaluator;
import org.leadpony.justify.internal.keyword.ArrayKeyword;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * Combiner representing "contains" keyword.
 *
 * @author leadpony
 */
@KeywordType("contains")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Contains extends UnaryCombiner implements ArrayKeyword {

    private int min;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromSchema mapper = Contains::new;
        return mapper;
    }

    public Contains(JsonSchema subschema) {
        super(subschema);
        this.min = 1;
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        if (this.min == 1) {
            return createItemsEvaluator(context);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        if (this.min == 1) {
            return createNegatedItemsEvaluator(context);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Evaluator createItemsEvaluator(EvaluatorContext context) {
        final JsonSchema subschema = getSubschema();
        return new AbstractDisjunctiveItemsEvaluator(context, this) {
            @Override
            public void updateChildren(Event event, JsonParser parser) {
                if (ParserEvents.isValue(event)) {
                    InstanceType type = ParserEvents.toBroadInstanceType(event);
                    append(subschema.createEvaluator(context, type));
                }
            }
        };
    }

    private Evaluator createNegatedItemsEvaluator(EvaluatorContext context) {
        final JsonSchema subschema = getSubschema();
        return new AbstractConjunctiveItemsEvaluator(context) {
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
