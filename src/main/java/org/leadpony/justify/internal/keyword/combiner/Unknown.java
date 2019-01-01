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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * Unknown keyword containing subschema.
 *
 * @author leadpony
 */
class Unknown extends Combiner {

    private final String name;
    private final JsonSchema subschema;

    Unknown(String name, JsonSchema subschema) {
        this.name = name;
        this.subschema = subschema;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), subschema.toJson());
    }

    @Override
    public void addToEvaluatables(List<Keyword> evaluatables, Map<String, Keyword> keywords) {
    }

    @Override
    public boolean hasSubschemas() {
        return true;
    }

    @Override
    public Stream<JsonSchema> subschemas() {
        return Stream.of(subschema);
    }

    @Override
    public JsonSchema getSubschema(Iterator<String> jsonPointer) {
        return subschema;
    }
}
