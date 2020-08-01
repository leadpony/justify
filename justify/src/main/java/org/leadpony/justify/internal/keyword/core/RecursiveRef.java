/*
 * Copyright 2020 the Justify authors.
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

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.InvalidKeywordException;
import org.leadpony.justify.api.keyword.JsonSchemaReference;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.SubschemaParser;
import org.leadpony.justify.internal.annotation.Spec;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * A keyword representing "$recursiveRef" keyword.
 *
 * @author leadpony
 */
@Spec(SpecVersion.DRAFT_2019_09)
public class RecursiveRef extends Ref {

    static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return "$recursiveRef";
        }

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() != ValueType.STRING) {
                throw new InvalidKeywordException("Must be a string");
            }
            JsonString string = (JsonString) jsonValue;
            if (!string.getString().equals("#")) {
                throw new InvalidKeywordException("Must be an empty fragment");
            }
            return map(jsonValue, schemaParser.parseSchemaReference(jsonValue));
        }

        private Keyword map(JsonValue jsonValue, JsonSchemaReference reference) {
            return new RecursiveRef(jsonValue, reference);
        }
    };

    protected RecursiveRef(JsonValue jsonValue, JsonSchemaReference reference) {
        super(jsonValue, reference);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    protected JsonSchema findtTargetSchema(Evaluator parent, JsonSchemaReference reference) {
        final JsonSchema direct = reference.getTargetSchema();
        if (testSchemaIsTarget(direct)) {
            return findTargetSchemaRecursively(parent, direct);
        } else {
            return direct;
        }
    }

    private static JsonSchema findTargetSchemaRecursively(Evaluator parent, JsonSchema defaultSchema) {
        JsonSchema targetSchema = defaultSchema;
        for (Evaluator evaluator = parent; evaluator != null; evaluator = evaluator.getParent()) {
            if (evaluator.isBasedOnSchema()) {
                JsonSchema schema = evaluator.getSchema();
                if (testSchemaIsTarget(schema)) {
                    targetSchema = schema;
                }
            }
        }
        return targetSchema;
    }

    private static boolean testSchemaIsTarget(JsonSchema schema) {
        return schema.getKeywordsAsMap().get(RecursiveAnchor.NAME) == RecursiveAnchor.TRUE;
    }
}
