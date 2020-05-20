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
package org.leadpony.justify.internal.schema;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaResolver;

/**
 *  A schema catalog.
 *
 * @author leadpony
 */
public class SchemaCatalog implements JsonSchemaResolver {

    private final Map<URI, Supplier<JsonSchema>> map = new HashMap<>();

    /**
     * Adds a schema to this catalog.
     *
     * @param schema the schema to add.
     */
    public void addSchema(JsonSchema schema) {
        if (schema.hasAbsoluteId()) {
            map.put(normalizeId(schema.id()), () -> schema);
        }
    }

    public void addSchema(URI id, Supplier<JsonSchema> schema) {
        if (id.isAbsolute()) {
            map.put(normalizeId(id), schema);
        }
    }

    @Override
    public JsonSchema resolveSchema(URI id) {
        if (id.isAbsolute()) {
            URI key = normalizeId(id);
            if (map.containsKey(key)) {
                return map.get(key).get();
            }
        }
        return null;
    }

    private static URI normalizeId(URI id) {
        if (id.getFragment() == null) {
            return id.resolve("#");
        } else {
            return id;
        }
    }
}
