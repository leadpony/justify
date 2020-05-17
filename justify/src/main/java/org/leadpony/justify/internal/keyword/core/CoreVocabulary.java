/*
 * Copyright 2020 the Justify authors.
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

import java.net.URI;
import java.util.Map;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.Vocabulary;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * The JSON Schema Core Vocabulary.
 *
 * @author leadpony
 */
public enum CoreVocabulary implements Vocabulary {
    DRAFT_4("https://json-schema.org/draft/4/vocab/core"),

    DRAFT_6("https://json-schema.org/draft/6/vocab/core"),

    DRAFT_7("https://json-schema.org/draft/7/vocab/core"),

    DRAFT_2019_09("https://json-schema.org/draft/2019-09/vocab/core");

    private final URI id;
    private final Map<String, KeywordType> typeMap;

    CoreVocabulary(String id, KeywordType... types) {
        this.id = URI.create(id);
        this.typeMap = KeywordTypes.toMap(types);
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public Map<String, KeywordType> asMap() {
        return typeMap;
    }
}
