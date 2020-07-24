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
package org.leadpony.justify.internal.schema;

import static org.leadpony.justify.internal.base.json.JsonPointers.encode;

import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaVisitor;
import org.leadpony.justify.api.JsonSchemaVisitor.Result;
import org.leadpony.justify.api.keyword.Keyword;

/**
 * A walker of JSON schema tree.
 *
 * @author leadpony
 */
class JsonSchemaWalker {

    private final JsonSchemaVisitor visitor;

    JsonSchemaWalker(JsonSchemaVisitor visitor) {
        this.visitor = visitor;
    }

    void walkSchema(JsonSchema schema) {
        walkSchema(schema, "");
    }

    private Result walkSchema(JsonSchema schema, String pointer) {
        Result result = visitor.visitSchema(schema, pointer);
        if (result == Result.TERMINATE) {
            return result;
        }

        Map<String, Keyword> keywordMap = schema.getKeywordsAsMap();
        for (Keyword keyword : keywordMap.values()) {
            result = walkKeyword(keyword, pointer);
            if (result == Result.TERMINATE) {
                return result;
            }
        }

        return visitor.leaveSchema(schema, pointer);
    }

    private Result walkKeyword(Keyword keyword, String basePointer) {
        String pointer = basePointer + "/" + encode(keyword.name());
        Result result = visitor.visitKeyword(keyword, pointer);
        if (result == Result.TERMINATE) {
            return result;
        }
        if (keyword.containsSchemas()) {
            for (Map.Entry<String, JsonSchema> entry : keyword.getSchemasAsMap().entrySet()) {
                result = walkSchema(entry.getValue(), pointer + entry.getKey());
                if (result == Result.TERMINATE) {
                    return result;
                }
            }
        }
        return visitor.leaveKeyword(keyword, pointer);
    }
}
