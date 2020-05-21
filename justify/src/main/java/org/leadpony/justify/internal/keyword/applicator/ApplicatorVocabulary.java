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
package org.leadpony.justify.internal.keyword.applicator;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.Vocabulary;

/**
 * @author leadpony
 */
public enum ApplicatorVocabulary implements Vocabulary {
    DRAFT_04("",
            AdditionalItems.TYPE,
            AdditionalProperties.TYPE,
            AllOf.TYPE,
            AnyOf.TYPE,
            Dependencies.TYPE,
            Items.TYPE,
            Not.TYPE,
            OneOf.TYPE,
            PatternProperties.TYPE,
            Properties.TYPE
            ),

    DRAFT_06("",
            AdditionalItems.TYPE,
            AdditionalProperties.TYPE,
            AllOf.TYPE,
            AnyOf.TYPE,
            Contains.TYPE,  // new
            Dependencies.TYPE,
            Items.TYPE,
            Not.TYPE,
            OneOf.TYPE,
            PatternProperties.TYPE,
            Properties.TYPE,
            PropertyNames.TYPE  // new
            ),

    DRAFT_07("",
            AdditionalItems.TYPE,
            AdditionalProperties.TYPE,
            AllOf.TYPE,
            AnyOf.TYPE,
            Contains.TYPE,
            Dependencies.TYPE,
            Else.TYPE,  // new
            If.TYPE,  // new
            Items.TYPE,
            Not.TYPE,
            OneOf.TYPE,
            PatternProperties.TYPE,
            Properties.TYPE,
            PropertyNames.TYPE,
            Then.TYPE  // new
            );

    private final URI id;
    private final List<KeywordType> keywordTypes;

    ApplicatorVocabulary(String id, KeywordType... keywordTypes) {
        this.id = URI.create(id);
        this.keywordTypes = Arrays.asList(keywordTypes);
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public List<KeywordType> getKeywordTypes(Map<String, Object> config, Map<String, Object> storage) {
        return keywordTypes;
    }
}