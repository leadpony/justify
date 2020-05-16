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

import java.util.Collection;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * Boolean logic specified with "allOf" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("allOf")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class AllOf extends NaryBooleanLogic {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromSchemaList mapper = AllOf::new;
        return mapper;
    }

    public AllOf(JsonValue json, Collection<JsonSchema> subschemas) {
        super(json, subschemas);
    }

    @Override
    public LogicalEvaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.conjunctive(type);
        getSchemas().distinct()
                .map(s -> s.createEvaluator(context, type))
                .forEach(evaluator::append);
        return evaluator;
    }

    @Override
    public LogicalEvaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema,
            InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.disjunctive(context, schema, this, type);
        getSchemas().distinct()
                .map(s -> s.createNegatedEvaluator(context, type))
                .forEach(evaluator::append);
        return evaluator;
    }
}
