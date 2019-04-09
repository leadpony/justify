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
package org.leadpony.justify.internal.schema;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaResolver;

/**
 *  A schema catalog.
 *
 * @author leadpony
 */
public class SchemaCatalog implements JsonSchemaResolver {

    private final Map<URI, JsonSchema> map = new HashMap<>();

    public void addSchema(JsonSchema schema) {
        if (schema.hasAbsoluteId()) {
            map.put(normalizeId(schema.id()), schema);
        }
    }

    @Override
    public JsonSchema resolveSchema(URI id) {
        if (id.isAbsolute()) {
            return map.get(normalizeId(id));
        } else {
            return null;
        }
    }

    private static URI normalizeId(URI id) {
        if (id.getFragment() == null) {
            return id.resolve("#");
        } else {
            return id;
        }
    }
}
