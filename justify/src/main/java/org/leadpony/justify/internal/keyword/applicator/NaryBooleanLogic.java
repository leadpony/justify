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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.json.JsonValue;
import org.leadpony.justify.api.JsonSchema;

/**
 * N-ary boolean logic. This class is the abstract base class for {@link AllOf},
 * {@link AnyOf} and {@link OneOf}.
 *
 * @author leadpony
 */
abstract class NaryBooleanLogic extends AbstractApplicatorKeyword {

    private final List<JsonSchema> subschemas;
    private final List<JsonSchema> distinctSubschemas;

    protected NaryBooleanLogic(JsonValue json, Collection<JsonSchema> subschemas) {
        super(json);
        this.subschemas = new ArrayList<>(subschemas);
        this.distinctSubschemas = subschemas.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public ApplicableLocation getApplicableLocation() {
        return ApplicableLocation.CURRENT;
    }

    @Override
    public boolean containsSchemas() {
        return !subschemas.isEmpty();
    }

    @Override
    public Map<String, JsonSchema> getSchemasAsMap() {
        Map<String, JsonSchema> map = new LinkedHashMap<>();
        for (int i = 0; i < subschemas.size(); i++) {
            map.put(String.valueOf(i), subschemas.get(i));
        }
        return map;
    }

    @Override
    public Stream<JsonSchema> getSchemasAsStream() {
        return this.subschemas.stream();
    }

    protected final Iterable<JsonSchema> getSubschemas() {
        return subschemas;
    }

    protected final Iterable<JsonSchema> getDistinctSubschemas() {
        return distinctSubschemas;
    }
}
