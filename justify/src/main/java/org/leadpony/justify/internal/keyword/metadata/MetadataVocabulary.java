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

import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.KeywordValueSetLoader;
import org.leadpony.justify.internal.keyword.DefaultVocabulary;

/**
 * A vocabulary for basic metadata annotations.
 *
 * @author leadpony
 */
public enum MetadataVocabulary implements DefaultVocabulary {
    DRAFT_04(
            Default.TYPE,
            Description.TYPE,
            Title.TYPE),

    DRAFT_06(DRAFT_04),

    DRAFT_07(DRAFT_04),

    DRAFT_2019_09(
            "https://json-schema.org/draft/2019-09/vocab/meta-data",
            "https://json-schema.org/draft/2019-09/meta/meta-data",
            Default.TYPE,
            Description.TYPE,
            Title.TYPE);

    private final URI id;
    private final URI metaschemaId;
    private final List<KeywordType> keywordTypes;

    MetadataVocabulary(MetadataVocabulary base) {
        this("", "", base.keywordTypes);
    }

    MetadataVocabulary(KeywordType... keywordTypes) {
        this("", "", Arrays.asList(keywordTypes));
    }

    MetadataVocabulary(String id, String metaschemaId, KeywordType... keywordTypes) {
        this(id, metaschemaId, Arrays.asList(keywordTypes));
    }

    MetadataVocabulary(String id, String metaschemaId, List<KeywordType> keywordTypes) {
        this.id = URI.create(id);
        this.metaschemaId = URI.create(metaschemaId);
        this.keywordTypes = keywordTypes;
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public URI getMetaschemaId() {
        return metaschemaId;
    }

    @Override
    public List<KeywordType> getKeywordTypes(Map<String, Object> config, KeywordValueSetLoader valueSetLoader) {
        return keywordTypes;
    }

    @Override
    public String getMetaschemaName() {
        return "meta-data";
    }
}
