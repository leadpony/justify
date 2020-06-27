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
package org.leadpony.justify.internal.keyword;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.json.JsonPointers;

/**
 * @author leadpony
 */
@SuppressWarnings("serial")
public class JsonSchemaMap extends LinkedHashMap<String, JsonSchema> {

    public Optional<JsonSchema> findSchema(String jsonPointer) {
        requireNonNull(jsonPointer, "jsonPointer");

        if (jsonPointer.isEmpty()) {
            return Optional.empty();
        }

        int next = jsonPointer.indexOf('/', 1);
        if (next < 0) {
            return Optional.ofNullable(get(jsonPointer));
        }

        JsonSchema schema = get(jsonPointer.substring(0, next));
        if (schema == null) {
            return Optional.empty();
        }

        return schema.findSchema(jsonPointer.substring(next));
    }

    public static JsonSchemaMap of(JsonSchema schema) {
        return new JsonSchemaMap() {

            {
                put("", schema);
            }

            @Override
            public Optional<JsonSchema> findSchema(String jsonPointer) {
                requireNonNull(jsonPointer, "jsonPointer");
                if (jsonPointer.isEmpty()) {
                    return Optional.of(schema);
                } else {
                    return schema.findSchema(jsonPointer);
                }
            }
        };
    }

    public static JsonSchemaMap of(Map<?, JsonSchema> schemas) {
        JsonSchemaMap map = new JsonSchemaMap();
        schemas.forEach((k, v)
                -> map.put("/" + JsonPointers.encode(k.toString()), v));
        return map;
    }

    public static JsonSchemaMap of(List<JsonSchema> schemas) {
        JsonSchemaMap map = new JsonSchemaMap();
        for (int i = 0; i < schemas.size(); i++) {
            map.put("/" + String.valueOf(i), schemas.get(i));
        }
        return map;
    }
}
