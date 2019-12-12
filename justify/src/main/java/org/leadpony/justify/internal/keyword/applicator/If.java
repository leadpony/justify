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

import java.util.List;
import java.util.Map;

import javax.json.JsonValue;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.evaluator.ConditionalEvaluator;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.SchemaKeyword;

/**
 * "If" conditional keyword.
 *
 * @author leadpony
 */
@KeywordType("if")
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
public class If extends Conditional {

    private JsonSchema thenSchema;
    private JsonSchema elseSchema;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromSchema mapper = If::new;
        return mapper;
    }

    public If(JsonValue json, JsonSchema schema) {
        super(schema);
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        Evaluator ifEvaluator = getSubschema().createEvaluator(context, type);
        Evaluator thenEvaluator = thenSchema != null ? thenSchema.createEvaluator(context, type)
                : Evaluator.ALWAYS_TRUE;
        Evaluator elseEvaluator = elseSchema != null ? elseSchema.createEvaluator(context, type)
                : Evaluator.ALWAYS_TRUE;
        return new ConditionalEvaluator(context, ifEvaluator, thenEvaluator, elseEvaluator);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        Evaluator ifEvaluator = getSubschema().createEvaluator(context, type);
        Evaluator thenEvaluator = thenSchema != null ? thenSchema.createNegatedEvaluator(context, type)
                : getSubschema().createNegatedEvaluator(context, type);
        Evaluator elseEvaluator = elseSchema != null ? elseSchema.createNegatedEvaluator(context, type)
                : getSubschema().createEvaluator(context, type);
        return new ConditionalEvaluator(context, ifEvaluator, thenEvaluator, elseEvaluator);
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, SchemaKeyword> keywords) {
        if (keywords.containsKey("then")) {
            SchemaKeyword thenKeyword = keywords.get("then");
            if (thenKeyword instanceof UnaryCombiner) {
                thenSchema = ((UnaryCombiner) thenKeyword).getSubschema();
            }
        }
        if (keywords.containsKey("else")) {
            SchemaKeyword elseKeyword = keywords.get("else");
            if (elseKeyword instanceof UnaryCombiner) {
                elseSchema = ((UnaryCombiner) elseKeyword).getSubschema();
            }
        }
        if (thenSchema != null || elseSchema != null) {
            evaluatables.add(this);
        }
    }
}
