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

package org.leadpony.justify.internal.keyword.core;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.json.JsonValue;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.SchemaContainer;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.AbstractKeyword;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * @author leadpony
 */
@KeywordClass("definitions")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Definitions extends AbstractKeyword implements SchemaContainer {

    public static final KeywordType TYPE = KeywordTypes.mappingSchemaMap("definitions", Definitions::new);

    private final Map<String, JsonSchema> definitionMap;

    public Definitions(JsonValue json, Map<String, JsonSchema> definitionMap) {
        super(json);
        this.definitionMap = definitionMap;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean containsSchemas() {
        return !definitionMap.isEmpty();
    }

    @Override
    public Stream<JsonSchema> getSchemas() {
        return this.definitionMap.values().stream();
    }

    @Override
    public Optional<JsonSchema> findSchema(String token) {
        if (definitionMap.containsKey(token)) {
            return Optional.of(definitionMap.get(token));
        }
        return Optional.empty();
    }
}