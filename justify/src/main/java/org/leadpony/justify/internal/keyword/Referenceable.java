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

package org.leadpony.justify.internal.keyword;

import java.util.Optional;
import java.util.stream.Stream;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SchemaContainer;

/**
 * A keyword containing referenceable subschema.
 *
 * @author leadpony
 */
public class Referenceable extends AbstractKeyword implements SchemaContainer {

    private final JsonSchema subschema;

    /**
     * Constructs this keyword.
     *
     * @param name the name of this keyword.
     * @param subschema the subschema contained by this keyword.
     */
    public Referenceable(String name, JsonSchema subschema) {
        super(name, subschema.toJson());
        this.subschema = subschema;
    }

    @Override
    public boolean canEvaluate() {
        return false;
    }

    @Override
    public boolean containsSchemas() {
        return true;
    }

    @Override
    public Stream<JsonSchema> getSchemas() {
        return Stream.of(subschema);
    }

    @Override
    public Optional<JsonSchema> findSchema(String token) {
        if (token.isEmpty()) {
            return Optional.of(subschema);
        } else {
            return Optional.empty();
        }
    }
}
