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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.SchemaKeyword;

/**
 * @author leadpony
 */
@KeywordType("definitions")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Definitions extends Combiner {

    private final Map<String, JsonSchema> definitionMap;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromSchemaMap mapper = Definitions::new;
        return mapper;
    }

    public Definitions(Map<String, JsonSchema> definitionMap) {
        this.definitionMap = definitionMap;
    }

    @Override
    public String name() {
        return "definitions";
    }

    @Override
    public JsonValue getValueAsJson(JsonProvider jsonProvider) {
        JsonObjectBuilder builder = jsonProvider.createObjectBuilder();
        this.definitionMap.forEach((k, v) -> builder.add(k, v.toJson()));
        return builder.build();
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, SchemaKeyword> keywords) {
    }

    @Override
    public boolean hasSubschemas() {
        return !definitionMap.isEmpty();
    }

    @Override
    public Stream<JsonSchema> getSubschemas() {
        return this.definitionMap.values().stream();
    }

    @Override
    public JsonSchema getSubschema(Iterator<String> jsonPointer) {
        if (jsonPointer.hasNext()) {
            return definitionMap.get(jsonPointer.next());
        }
        return null;
    }
}
