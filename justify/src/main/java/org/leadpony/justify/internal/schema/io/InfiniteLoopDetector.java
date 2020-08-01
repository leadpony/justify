/*
 * Copyright 2018, 2020 the Justify authors.
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

import java.util.Collection;
import java.util.Set;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.ApplicatorKeyword;
import org.leadpony.justify.api.keyword.JsonSchemaReference;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.RefKeyword;
import org.leadpony.justify.internal.base.Sets;

/**
 * A detector of infinite recursive looping starting from a schema reference.
 *
 * @author leadpony
 */
class InfiniteLoopDetector {

    private final Set<JsonSchema> visited = Sets.newIdentitySet();

    boolean detectInfiniteLoop(JsonSchemaReference ref) {
        assert visited.isEmpty();
        return detectLoopFrom(ref.getTargetSchema());
    }

    private boolean detectLoopFrom(JsonSchema schema) {
        if (visited.contains(schema)) {
            return true;
        }

        try {
            visited.add(schema);
            return walkSubschemas(schema);
        } finally {
            visited.remove(schema);
        }
    }

    private boolean walkSubschemas(JsonSchema schema) {
        Collection<Keyword> keywords = schema.getKeywordsAsMap().values();
        return keywords.stream()
            .filter(k -> k instanceof ApplicatorKeyword)
            .map(k -> (ApplicatorKeyword) k)
            .filter(InfiniteLoopDetector::isInPlaceApplicator)
            .flatMap(Keyword::getSchemasAsStream)
            .anyMatch(this::detectLoopFrom);
    }

    private static boolean isInPlaceApplicator(ApplicatorKeyword keyword) {
        if (keyword instanceof RefKeyword) {
            RefKeyword ref = (RefKeyword) keyword;
            return ref.isDirect();
        } else {
            return keyword.isInPlace();
        }
    }
}
