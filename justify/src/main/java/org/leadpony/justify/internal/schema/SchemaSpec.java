/*
 * Copyright 2018-2020 the Justify authors.
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
package org.leadpony.justify.internal.schema;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.KeywordValueSetLoader;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.Vocabulary;
import org.leadpony.justify.internal.keyword.MetaschemaSource;
import org.leadpony.justify.internal.keyword.applicator.ApplicatorVocabulary;
import org.leadpony.justify.internal.keyword.content.ContentVocabulary;
import org.leadpony.justify.internal.keyword.core.CoreVocabulary;
import org.leadpony.justify.internal.keyword.format.FormatVocabulary;
import org.leadpony.justify.internal.keyword.metadata.MetadataVocabulary;
import org.leadpony.justify.internal.keyword.validation.ValidationVocabulary;

/**
 * A standard JSON Schema specificaiton.
 *
 * @author leadpony
 */
public enum SchemaSpec implements MetaschemaSource {
    DRAFT_04(SpecVersion.DRAFT_04,
            ApplicatorVocabulary.DRAFT_04,
            CoreVocabulary.DRAFT_04,
            FormatVocabulary.DRAFT_04,
            MetadataVocabulary.DRAFT_04,
            ValidationVocabulary.DRAFT_04),

    DRAFT_06(SpecVersion.DRAFT_06,
            ApplicatorVocabulary.DRAFT_06,
            CoreVocabulary.DRAFT_06,
            FormatVocabulary.DRAFT_06,
            MetadataVocabulary.DRAFT_06,
            ValidationVocabulary.DRAFT_06),

    DRAFT_07(SpecVersion.DRAFT_07,
            ApplicatorVocabulary.DRAFT_07,
            ContentVocabulary.DRAFT_07,
            CoreVocabulary.DRAFT_07,
            FormatVocabulary.DRAFT_07,
            MetadataVocabulary.DRAFT_07,
            ValidationVocabulary.DRAFT_07);

    private static final Map<SpecVersion, SchemaSpec> SPEC_MAP;

    private final SpecVersion version;
    private final Collection<Vocabulary> vocabularies;
    private final Map<String, KeywordType> bareKeywordTypes;

    static {
        SPEC_MAP = new EnumMap<>(SpecVersion.class);
        for (SchemaSpec spec : values()) {
            SPEC_MAP.put(spec.getVersion(), spec);
        }
    }

    SchemaSpec(SpecVersion version, Vocabulary... vocabularies) {
        this.version = version;
        this.vocabularies = Arrays.asList(vocabularies);
        this.bareKeywordTypes = collectKeywordTypesAsMap(this.vocabularies);
    }

    @Override
    public String getMetaschemaName() {
        return "schema";
    }

    /**
     * Return the verison of this specification.
     *
     * @return the verison of this specification.
     */
    public SpecVersion getVersion() {
        return version;
    }

    /**
     * Returns all of the vocabularies defined by this spec.
     *
     * @return all of the vocabularies.
     */
    public Collection<Vocabulary> getVocabularies() {
        return vocabularies;
    }

    public Map<String, KeywordType> getBareKeywordTypes() {
        return bareKeywordTypes;
    }

    public static SchemaSpec get(SpecVersion version) {
        return SPEC_MAP.get(version);
    }

    private static Map<String, KeywordType> collectKeywordTypesAsMap(Collection<Vocabulary> vocabularies) {
        Map<String, Object> config = Collections.emptyMap();
        return vocabularies.stream()
                .flatMap(v -> v.getKeywordTypes(config, KeywordValueSetLoader.NEVER).stream())
                .collect(Collectors.toMap(KeywordType::name, Function.identity()));
    }
}
