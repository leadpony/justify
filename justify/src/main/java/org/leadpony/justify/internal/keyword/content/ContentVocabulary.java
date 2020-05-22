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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.KeywordValueSetLoader;
import org.leadpony.justify.api.Vocabulary;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;

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
    public List<KeywordType> getKeywordTypes(Map<String, Object> config, KeywordValueSetLoader valueSetLoader) {
        return Arrays.asList(
                createContentEncoding(valueSetLoader),
                createContentMediaType(valueSetLoader));
    }

    public Map<String, ContentEncodingScheme> getEncodingSchemes(KeywordValueSetLoader valueSetLoader) {
        return Stream
                .concat(Stream.of(Base64.INSTANCE),
                        valueSetLoader.loadKeywordValueSet(ContentEncodingScheme.class).stream())
                .collect(Collectors.toMap(ContentEncodingScheme::name, Function.identity()));
    }

    public Map<String, ContentMimeType> getMimeTypes(KeywordValueSetLoader valueSetLoader) {
        return Stream
                .concat(Stream.of(JsonMimeType.INSTANCE),
                        valueSetLoader.loadKeywordValueSet(ContentMimeType.class).stream())
                .collect(Collectors.toMap(ContentMimeType::toString, Function.identity()));
    }

    private static KeywordType createContentEncoding(KeywordValueSetLoader valueSetLoader) {
        Collection<ContentEncodingScheme> additional = valueSetLoader.loadKeywordValueSet(ContentEncodingScheme.class);
        return ContentEncoding.TYPE.withSchemes(additional);
    }

    private static KeywordType createContentMediaType(KeywordValueSetLoader valueSetLoader) {
        Collection<ContentMimeType> additional = valueSetLoader.loadKeywordValueSet(ContentMimeType.class);
        return ContentMediaType.TYPE.withMimeTypes(additional);
    }
}
