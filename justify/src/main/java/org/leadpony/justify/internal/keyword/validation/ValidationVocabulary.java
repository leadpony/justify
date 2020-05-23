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
package org.leadpony.justify.internal.keyword.validation;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.KeywordValueSetLoader;
import org.leadpony.justify.internal.keyword.DefaultVocabulary;

/**
 * A vocabulary for structural validation.
 *
 * @author leadpony
 */
public enum ValidationVocabulary implements DefaultVocabulary {
    DRAFT_04(
            Draft04Maximum.TYPE,
            Draft04Maximum.ExclusiveMaximum.TYPE,
            Draft04Minimum.TYPE,
            Draft04Minimum.ExclusiveMinimum.TYPE,
            Draft04Type.TYPE,
            Enum.TYPE,
            MaxItems.TYPE,
            MaxLength.TYPE,
            MaxProperties.TYPE,
            MinItems.TYPE,
            MinLength.TYPE,
            MinProperties.TYPE,
            MultipleOf.TYPE,
            Pattern.TYPE,
            Required.TYPE,
            UniqueItems.TYPE),

    DRAFT_06(
            Const.TYPE,
            Enum.TYPE,
            ExclusiveMaximum.TYPE,
            ExclusiveMinimum.TYPE,
            Maximum.TYPE,
            MaxItems.TYPE,
            MaxLength.TYPE,
            MaxProperties.TYPE,
            Minimum.TYPE,
            MinItems.TYPE,
            MinLength.TYPE,
            MinProperties.TYPE,
            MultipleOf.TYPE,
            Pattern.TYPE,
            Required.TYPE,
            Type.TYPE,
            UniqueItems.TYPE),

    DRAFT_07(DRAFT_06),

    DRAFT_2019_09("https://json-schema.org/draft/2019-09/vocab/validation",
            "https://json-schema.org/draft/2019-09/meta/validation",
            Const.TYPE,
            Enum.TYPE,
            ExclusiveMaximum.TYPE,
            ExclusiveMinimum.TYPE,
            Maximum.TYPE,
            MaxItems.TYPE,
            MaxLength.TYPE,
            MaxProperties.TYPE,
            Minimum.TYPE,
            MinItems.TYPE,
            MinLength.TYPE,
            MinProperties.TYPE,
            MultipleOf.TYPE,
            Pattern.TYPE,
            Required.TYPE,
            Type.TYPE,
            UniqueItems.TYPE);

    private final URI id;
    private final URI metaschemaId;
    private final List<KeywordType> keywordTypes;

    ValidationVocabulary(KeywordType... keywordTypes) {
        this("", "", Arrays.asList(keywordTypes));
    }

    ValidationVocabulary(ValidationVocabulary base) {
        this("", "", base.keywordTypes);
    }

    ValidationVocabulary(String id, String metaschemaId, KeywordType... keywordTypes) {
        this(id, metaschemaId, Arrays.asList(keywordTypes));
    }

    ValidationVocabulary(String id, String metaschemaId, List<KeywordType> keywordTypes) {
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
        return "validation";
    }
}
