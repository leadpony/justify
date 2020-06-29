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
package org.leadpony.justify.internal.schema.io;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.JsonSchemaReference;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.RefKeyword;

/**
 * A detector of infinite recursive looping starting from a schema reference.
 *
 * @author leadpony
 */
class InfiniteLoopDetector {

    private static final String REFERENCE_KEYWORD = "$ref";

    private final Set<JsonSchemaReference> checkPoints = new HashSet<>();

    boolean detectInfiniteLoop(JsonSchemaReference ref) {
        try {
            return detectLoopFrom(ref);
        } finally {
            checkPoints.clear();
        }
    }

    private boolean detectLoopFrom(JsonSchema schema) {

        if (schema.containsKeyword(REFERENCE_KEYWORD)) {
            Keyword keyword = schema.asObjectJsonSchema().get(REFERENCE_KEYWORD);
            if (keyword instanceof RefKeyword) {
                RefKeyword ref = (RefKeyword) keyword;
                if (detectLoopFrom(ref.getSchemaReference())) {
                    return true;
                }
            }
        }

        Iterator<JsonSchema> it = schema.getInPlaceSubschemas().iterator();
        while (it.hasNext()) {
            if (detectLoopFrom(it.next())) {
                return true;
            }
        }
        return false;
    }

    private boolean detectLoopFrom(JsonSchemaReference ref) {
        if (checkPoints.contains(ref)) {
            return true;
        }
        checkPoints.add(ref);
        boolean result = detectLoopFrom(ref.getReferencedSchema());
        checkPoints.remove(ref);
        return result;
    }
}
