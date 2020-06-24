/*
 * Copyright 2018, 2020 the Justify authors.
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

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
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

    static final KeywordType TYPE = KeywordTypes.mappingSchema("if", If::new);

    private final JsonSchema thenSchema;
    private final JsonSchema elseSchema;

    public If(JsonSchema schema) {
        this(schema, null, null);
    }

    public If(JsonSchema schema, JsonSchema thenSchema, JsonSchema elseSchema) {
        super(schema);
        this.thenSchema = thenSchema;
        this.elseSchema = elseSchema;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public Keyword withKeywords(Map<String, Keyword> siblings) {
        JsonSchema thenSchema = null;
        JsonSchema elseSchema = null;

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

        if (thenSchema != null || elseSchema != null) {
            return new If(getSubschema(), thenSchema, elseSchema);
        } else {
            return this;
        }
    }

    @Override
    public boolean canEvaluate() {
        return !isAlone();
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        return ConditionalEvaluator.of(getSubschema(), thenSchema, elseSchema, parent, type);
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        return ConditionalEvaluator.ofNegated(getSubschema(), thenSchema, elseSchema, parent, type);
    }

    private boolean isAlone() {
        return thenSchema == null && elseSchema == null;
    }
}
