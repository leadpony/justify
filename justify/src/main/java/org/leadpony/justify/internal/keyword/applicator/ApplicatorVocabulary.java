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

import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.KeywordValueSetLoader;
import org.leadpony.justify.internal.keyword.DefaultVocabulary;

/**
 * A vocabulary for applying subschemas.
 *
 * @author leadpony
 */
public enum ApplicatorVocabulary implements DefaultVocabulary {
    DRAFT_04(
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

    DRAFT_06(
            AdditionalItems.TYPE,
            AdditionalProperties.TYPE,
            AllOf.TYPE,
            AnyOf.TYPE,
            SimpleContains.TYPE,  // added
            Dependencies.TYPE,
            Items.TYPE,
            Not.TYPE,
            OneOf.TYPE,
            PatternProperties.TYPE,
            Properties.TYPE,
            PropertyNames.TYPE  // added
            ),

    DRAFT_07(
            AdditionalItems.TYPE,
            AdditionalProperties.TYPE,
            AllOf.TYPE,
            AnyOf.TYPE,
            SimpleContains.TYPE,
            Dependencies.TYPE,
            Else.TYPE,  // added
            If.TYPE,  // added
            Items.TYPE,
            Not.TYPE,
            OneOf.TYPE,
            PatternProperties.TYPE,
            Properties.TYPE,
            PropertyNames.TYPE,
            Then.TYPE  // added
            ),

    DRAFT_2019_09(
            "https://json-schema.org/draft/2019-09/vocab/applicator",
            "https://json-schema.org/draft/2019-09/meta/applicator",
            AdditionalItems.TYPE,
            AdditionalProperties.TYPE,
            AllOf.TYPE,
            AnyOf.TYPE,
            Contains.TYPE,  // replaced
            Dependencies.TYPE,
            DependentSchemas.TYPE,
            Else.TYPE,
            If.TYPE,
            Items.TYPE,
            Not.TYPE,
            OneOf.TYPE,
            PatternProperties.TYPE,
            Properties.TYPE,
            PropertyNames.TYPE,
            Then.TYPE
            );

    private final URI id;
    private final URI metaschemaId;
    private final List<KeywordType> keywordTypes;

    ApplicatorVocabulary(KeywordType... keywordTypes) {
        this("", "", keywordTypes);
    }

    ApplicatorVocabulary(String id, String metaschemaId, KeywordType... keywordTypes) {
        this.id = URI.create(id);
        this.metaschemaId = URI.create(metaschemaId);
        this.keywordTypes = Arrays.asList(keywordTypes);
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
        return "applicator";
    }
}
