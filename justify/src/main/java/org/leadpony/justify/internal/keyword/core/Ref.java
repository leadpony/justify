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
package org.leadpony.justify.internal.keyword.core;

import java.net.URI;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.JsonSchemaReference;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.RefKeyword;
import org.leadpony.justify.api.keyword.SubschemaParser;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.AbstractKeyword;

/**
 * A keyword type representing "$ref" keyword.
 *
 * @author leadpony
 */
@KeywordClass("$ref")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.DRAFT_2019_09)
public class Ref extends AbstractKeyword implements RefKeyword {

    static class RefKeywordType implements KeywordType {

        @Override
        public String name() {
            return "$ref";
        }

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            return map(jsonValue, schemaParser.parseSchemaReference(jsonValue));
        }

        protected Keyword map(JsonValue jsonValue, JsonSchemaReference reference) {
            return new Ref(jsonValue, reference);
        }
    }

    static final KeywordType TYPE = new RefKeywordType();

    private final JsonSchemaReference reference;

    Ref(JsonValue jsonValue, JsonSchemaReference reference) {
        super(jsonValue);
        this.reference = reference;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean canEvaluate() {
        return true;
    }

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        JsonSchema schema = findtTargetSchema(parent, this.reference);
        return schema.createEvaluator(parent, type);
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        JsonSchema schema = findtTargetSchema(parent, this.reference);
        return schema.createNegatedEvaluator(parent, type);
    }

    /* As a RefKeyword */

    @Override
    public URI value() {
        return reference.getTargetId();
    }

    @Override
    public JsonSchemaReference getSchemaReference() {
        return reference;
    }

    protected JsonSchema findtTargetSchema(Evaluator parent, JsonSchemaReference reference) {
        return reference.getTargetSchema();
    }
}
