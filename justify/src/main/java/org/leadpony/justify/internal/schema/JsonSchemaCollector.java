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

import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaVisitor;

/**
 * A collector of JSON schemas.
 *
 * @author leadpony
 */
class JsonSchemaCollector implements JsonSchemaVisitor {

    private final Map<String, JsonSchema> schemas;

    JsonSchemaCollector(Map<String, JsonSchema> schemas) {
        this.schemas = schemas;
    }

    @Override
    public Result visitSchema(JsonSchema schema, String pointer) {
        this.schemas.put(pointer, schema);
        return Result.CONTINUE;
    }
}
