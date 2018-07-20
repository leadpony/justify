/*
 * Copyright 2018 the Justify authors.
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

package org.leadpony.justify.internal.base;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.leadpony.justify.core.JsonSchema;

/**
 * Utility methods operating on {@link JsonSchema} instances.
 * 
 * @author leadpony
 */
public final class JsonSchemas {
    
    private JsonSchemas() {
    }

    public static List<JsonSchema> negate(List<JsonSchema> schemaList) {
        if (schemaList == null) {
            return schemaList;
        }
        return schemaList.stream()
                .map(JsonSchema::negate).collect(Collectors.toList());
    }
    
    public static <K> Map<K, JsonSchema> negate(Map<K, JsonSchema> schemaMap) {
        if (schemaMap == null) {
            return schemaMap;
        }
        Map<K, JsonSchema> newMap = new LinkedHashMap<>(schemaMap);
        newMap.replaceAll((k, v)->v.negate());
        return newMap;
    }
}
