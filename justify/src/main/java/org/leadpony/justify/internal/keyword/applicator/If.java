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

import java.util.Map;

import jakarta.json.JsonValue;

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
import org.leadpony.justify.internal.evaluator.ConditionalEvaluator;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * "If" conditional keyword.
 *
 * @author leadpony
 */
@KeywordClass("if")
@Spec(SpecVersion.DRAFT_07)
public class If extends Conditional {

    public static final KeywordType TYPE = KeywordTypes.mappingSchema("if", If::new);

    private JsonSchema thenSchema;
    private JsonSchema elseSchema;

    public If(JsonValue json, JsonSchema schema) {
        super(schema);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Keyword link(Map<String, Keyword> siblings) {
        if (siblings.containsKey("then")) {
            Keyword thenKeyword = siblings.get("then");
            if (thenKeyword instanceof UnaryApplicator) {
                thenSchema = ((UnaryApplicator) thenKeyword).getSubschema();
            }
        }
        if (siblings.containsKey("else")) {
            Keyword elseKeyword = siblings.get("else");
            if (elseKeyword instanceof UnaryApplicator) {
                elseSchema = ((UnaryApplicator) elseKeyword).getSubschema();
            }
        }
        return this;
    }

    @Override
    public boolean canEvaluate() {
        return thenSchema != null || elseSchema != null;
    }

    @Override
    public Evaluator createEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        Evaluator ifEvaluator = getSubschema().createEvaluator(context, type);
        Evaluator thenEvaluator = thenSchema != null ? thenSchema.createEvaluator(context, type)
                : Evaluator.ALWAYS_TRUE;
        Evaluator elseEvaluator = elseSchema != null ? elseSchema.createEvaluator(context, type)
                : Evaluator.ALWAYS_TRUE;
        return new ConditionalEvaluator(ifEvaluator, thenEvaluator, elseEvaluator);
    }

    @Override
    public Evaluator createNegatedEvaluator(EvaluatorContext context, ObjectJsonSchema schema, InstanceType type) {
        Evaluator ifEvaluator = getSubschema().createEvaluator(context, type);
        Evaluator thenEvaluator = thenSchema != null ? thenSchema.createNegatedEvaluator(context, type)
                : getSubschema().createNegatedEvaluator(context, type);
        Evaluator elseEvaluator = elseSchema != null ? elseSchema.createNegatedEvaluator(context, type)
                : getSubschema().createEvaluator(context, type);
        return new ConditionalEvaluator(ifEvaluator, thenEvaluator, elseEvaluator);
    }
}
