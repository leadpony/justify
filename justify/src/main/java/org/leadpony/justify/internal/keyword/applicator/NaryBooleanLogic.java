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
import java.util.List;
import java.util.Optional;
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

    protected NaryBooleanLogic(JsonValue json, Collection<JsonSchema> subschemas) {
        super(json);
        this.subschemas = new ArrayList<>(subschemas);
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
    public Stream<JsonSchema> getSchemas() {
        return this.subschemas.stream();
    }

    @Override
    public Optional<JsonSchema> findSchema(String token) {
        try {
            int index = Integer.parseInt(token);
            if (index < subschemas.size()) {
                return Optional.of(subschemas.get(index));
            }
        } catch (NumberFormatException e) {
        }
        return Optional.empty();
    }
}
