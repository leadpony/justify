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
package org.leadpony.justify.internal.provider;

import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.Vocabulary;
import org.leadpony.justify.internal.keyword.KeywordFactory;
import org.leadpony.justify.internal.keyword.applicator.ApplicatorVocabulary;
import org.leadpony.justify.internal.keyword.content.ContentVocabulary;
import org.leadpony.justify.internal.keyword.content.ContentAttributes;
import org.leadpony.justify.internal.keyword.core.CoreVocabulary;
import org.leadpony.justify.internal.keyword.format.FormatAttributes;
import org.leadpony.justify.internal.keyword.format.FormatVocabulary;
import org.leadpony.justify.internal.keyword.metadata.MetadataVocabulary;
import org.leadpony.justify.internal.keyword.validation.ValidationVocabulary;
import org.leadpony.justify.internal.schema.SchemaSpec;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A standard JSON Schema specificaiton.
 *
 * @author leadpony
 */
enum StandardSchemaSpec implements SchemaSpec, Iterable<Vocabulary> {
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

    private static final Map<SpecVersion, StandardSchemaSpec> SPEC_MAP;

    private final SpecVersion version;
    private final List<Vocabulary> vocabularies;

    private final Map<String, FormatAttribute> formatAttributes;

    private final KeywordFactory keywordFactory;

    private final Map<String, ContentEncodingScheme> encodingSchemes = new HashMap<>();
    private final Map<String, ContentMimeType> mimeTypes = new HashMap<>();


    static {
        SPEC_MAP = new EnumMap<>(SpecVersion.class);
        for (StandardSchemaSpec spec : values()) {
            SPEC_MAP.put(spec.getVersion(), spec);
        }
    }

    StandardSchemaSpec(SpecVersion version, Vocabulary... vocabularies) {
        this.version = version;
        this.vocabularies = Arrays.asList(vocabularies);
        this.formatAttributes = FormatAttributes.getAttributes(version);
        this.keywordFactory = new StandardKeywordFactory(version);
        this.encodingSchemes.putAll(ContentAttributes.encodingSchemes());
        this.mimeTypes.putAll(ContentAttributes.mimeTypes());
    }

    @Override
    public SpecVersion getVersion() {
        return version;
    }

    @Override
    public InputStream getMetaschemaAsStream() {
        String name = getVersion().toString().toLowerCase() + ".json";
        return getClass().getResourceAsStream(name);
    }

    @Override
    public KeywordFactory getKeywordFactory() {
        return keywordFactory;
    }

    @Override
    public FormatAttribute getFormatAttribute(String name) {
        return formatAttributes.get(name);
    }

    @Override
    public ContentEncodingScheme getEncodingScheme(String name) {
        return encodingSchemes.get(name);
    }

    @Override
    public ContentMimeType getMimeType(String value) {
        return mimeTypes.get(value);
    }

    @Override
    public Iterator<Vocabulary> iterator() {
        return vocabularies.iterator();
    }

    public static StandardSchemaSpec get(SpecVersion version) {
        return SPEC_MAP.get(version);
    }
}
