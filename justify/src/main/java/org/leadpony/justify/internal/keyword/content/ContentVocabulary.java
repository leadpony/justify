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
package org.leadpony.justify.internal.keyword.content;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.KeywordValuesLoader;
import org.leadpony.justify.api.Vocabulary;

/**
 * @author leadpony
 */
public enum ContentVocabulary implements Vocabulary {
    DRAFT_07;

    private final URI id;

    ContentVocabulary() {
        this("");
    }

    ContentVocabulary(String id) {
        this.id = URI.create(id);
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public List<KeywordType> getKeywordTypes(Map<String, Object> config, KeywordValuesLoader valuesLoader) {
        List<KeywordType> types = new ArrayList<>();
        types.add(ContentEncoding.TYPE);
        types.add(ContentMediaType.TYPE);
        return types;
    }
}
