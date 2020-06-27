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

import java.util.Map;
import java.util.stream.Stream;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.Maps;

/**
 * An applicator operating on single subschema.
 *
 * @author leadpony
 */
abstract class UnaryApplicator extends AbstractApplicatorKeyword {

    private final JsonSchema subschema;

    protected UnaryApplicator(JsonSchema subschema) {
        super(subschema.toJson());
        this.subschema = subschema;
    }

    JsonSchema getSubschema() {
        return subschema;
    }

    @Override
    public boolean containsSchemas() {
        return true;
    }

    @Override
    public Map<String, JsonSchema> getSchemasAsMap() {
        return Maps.of("", subschema);
    }

    @Override
    public Stream<JsonSchema> getSchemasAsStream() {
        return Stream.of(subschema);
    }
}
