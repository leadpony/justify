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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.Vocabulary;
import org.leadpony.justify.spi.ContentEncodingScheme;

/**
 * @author leadpony
 */
public enum ContentVocabulary implements Vocabulary {
    DRAFT_07;

    private final URI id;
    private final String key;

    ContentVocabulary() {
        this("");
    }

    ContentVocabulary(String id) {
        this.id = URI.create(id);
        this.key = getClass().getName() + "." + name();
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public List<KeywordType> getKeywordTypes(Map<String, Object> config, Map<String, Object> storage) {
        if (storage.containsKey(this.key)) {
            @SuppressWarnings("unchecked")
            List<KeywordType> cached = (List<KeywordType>) storage.get(this.key);
            return cached;
        }
        List<KeywordType> types = new ArrayList<>();
        types.add(ContentEncoding.TYPE);
        types.add(ContentMediaType.TYPE);
        return types;
    }

    private static Map<String, ContentEncodingScheme> loadEncodingSchemes() {
        Map<String, ContentEncodingScheme> schemes = new HashMap<>();
        schemes.put(Base64.INSTANCE.name().toLowerCase(), Base64.INSTANCE);
        for (ContentEncodingScheme scheme : ServiceLoader.load(ContentEncodingScheme.class)) {
            schemes.put(scheme.name().toLowerCase(), scheme);
        }
        return schemes;
    }
}
