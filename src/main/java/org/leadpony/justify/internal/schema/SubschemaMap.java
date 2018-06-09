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

package org.leadpony.justify.internal.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.leadpony.justify.core.JsonSchema;

/**
 * @author leadpony
 */
@SuppressWarnings("serial")
class SubschemaMap extends HashMap<String, JsonSchema> {

    private static final Set<String> PREFIXES = new HashSet<String>() {{
        add("properties");
        add("patternProperties");
        add("definitions");
    }};
    
    JsonSchema lookUp(String jsonPointer) {
        int endIndex = jsonPointer.indexOf('/', 1);
        String token = extractToken(jsonPointer, 1, endIndex);
        if (PREFIXES.contains(token) ||
            (token.equals("items") && !containsKey("items"))) {
            int beginIndex = endIndex + 1;
            endIndex = jsonPointer.indexOf('/', beginIndex);
            token = token + "/" + extractToken(jsonPointer, beginIndex, endIndex);
        }
        JsonSchema schema = get(token);
        if (schema != null && endIndex >= 0) {
            String nextPointer = jsonPointer.substring(endIndex); 
            schema = schema.find(nextPointer);
        }
        return schema;
    }
    
    private static String extractToken(String jsonPointer, int beginIndex, int endIndex) {
        String token = (endIndex < 0) ?
                jsonPointer.substring(beginIndex) :
                jsonPointer.substring(beginIndex, endIndex);
        return unescape(token);
    }
    
    private static String unescape(String token) {
        return token.replaceAll("~0", "~").replaceAll("~1", "/");
    }
}
