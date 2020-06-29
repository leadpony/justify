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
package org.leadpony.justify.api.keyword;

import org.leadpony.justify.api.JsonSchema;

import jakarta.json.JsonValue;

/**
 * A parser of subschemas.
 *
 * @author leadpony
 */
public interface SubschemaParser {

    /**
     * Parses a subschema.
     *
     * @param jsonValue the original JSON value to parse.
     * @return newly created subschema.
     * @throws InvalidKeywordException if the input value is not a schema.
     */
    default JsonSchema parseSubschema(JsonValue jsonValue) {
        return parseSubschemaAt("", jsonValue);
    }

    JsonSchema parseSubschema(JsonValue jsonValue, Object... tokens);

    JsonSchema parseSubschemaAt(String jsonPointer, JsonValue jsonValue);

    JsonSchemaReference parseSchemaReference(JsonValue jsonValue);
}
