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
import org.leadpony.justify.api.KeywordValuesLoader;
import org.leadpony.justify.api.Vocabulary;

/**
 * @author leadpony
 */
public enum ValidationVocabulary implements Vocabulary {
    DRAFT_04("",
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

    DRAFT_06("",
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

    DRAFT_07("", DRAFT_06.keywordTypes);

    private final URI id;
    private final List<KeywordType> keywordTypes;

    ValidationVocabulary(String id, KeywordType... keywordTypes) {
        this(id, Arrays.asList(keywordTypes));
    }

    ValidationVocabulary(String id, List<KeywordType> keywordTypes) {
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
