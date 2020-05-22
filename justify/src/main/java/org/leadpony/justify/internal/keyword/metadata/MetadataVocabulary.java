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
package org.leadpony.justify.internal.keyword.metadata;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.KeywordValuesLoader;
import org.leadpony.justify.api.Vocabulary;

/**
 * @author leadpony
 */
public enum MetadataVocabulary implements Vocabulary {
    DRAFT_04("",
            Default.TYPE,
            Description.TYPE,
            Title.TYPE),

    DRAFT_06("", DRAFT_04.keywordTypes),

    DRAFT_07("", DRAFT_04.keywordTypes);

    private final URI id;
    private final List<KeywordType> keywordTypes;

    MetadataVocabulary(String id, KeywordType... keywordTypes) {
        this(id, Arrays.asList(keywordTypes));
    }

    MetadataVocabulary(String id, List<KeywordType> keywordTypes) {
        this.id = URI.create(id);
        this.keywordTypes = keywordTypes;
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public List<KeywordType> getKeywordTypes(Map<String, Object> config, KeywordValuesLoader valuesLoader) {
        return keywordTypes;
    }

}
