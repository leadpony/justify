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

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * Type representing "not" boolean logic.
 *
 * @author leadpony
 */
@KeywordClass("not")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Not extends UnaryApplicator {

    public static final KeywordType TYPE = KeywordTypes.mappingSchema("not", Not::new);

    public Not(JsonValue json, JsonSchema subschema) {
        super(subschema);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CURRENT;
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        return getSubschema().createNegatedEvaluator(context, type);
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        return getSubschema().createEvaluator(context, type);
    }
}