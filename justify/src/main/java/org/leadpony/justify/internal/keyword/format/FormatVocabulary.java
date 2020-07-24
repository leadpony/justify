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
package org.leadpony.justify.internal.keyword.format;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.KeywordValueSetLoader;
import org.leadpony.justify.internal.keyword.DefaultVocabulary;
import org.leadpony.justify.internal.keyword.format.Format.FormatType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A vocabulary for semantic content With "format".
 *
 * @author leadpony
 */
public enum FormatVocabulary implements DefaultVocabulary {
    DRAFT_04(
            DateTime.INSTANCE,
            Email.INSTANCE,
            Hostname.INSTANCE,
            Ipv4.INSTANCE,
            Ipv6.INSTANCE,
            Regex.INSTANCE,
            Uri.INSTANCE),

    DRAFT_06(
            DateTime.INSTANCE,
            Email.INSTANCE,
            Hostname.INSTANCE,
            Ipv4.INSTANCE,
            Ipv6.INSTANCE,
            JsonPointer.INSTANCE,
            Regex.INSTANCE,
            Uri.INSTANCE,
            UriReference.INSTANCE,
            UriTemplate.INSTANCE),

    DRAFT_07(
            Date.INSTANCE,
            DateTime.INSTANCE,
            Email.INSTANCE,
            Hostname.INSTANCE,
            Ipv4.INSTANCE,
            Ipv6.INSTANCE,
            IdnEmail.INSTANCE,
            IdnHostname.INSTANCE,
            Iri.INSTANCE,
            IriReference.INSTANCE,
            JsonPointer.INSTANCE,
            Regex.INSTANCE,
            RelativeJsonPointer.INSTANCE,
            Time.INSTANCE,
            Uri.INSTANCE,
            UriReference.INSTANCE,
            UriTemplate.INSTANCE),

    DRAFT_2019_09(
            "https://json-schema.org/draft/2019-09/vocab/format",
            "https://json-schema.org/draft/2019-09/meta/format",
            Date.INSTANCE,
            DateTime.INSTANCE,
            Email.INSTANCE,
            Hostname.INSTANCE,
            Ipv4.INSTANCE,
            Ipv6.INSTANCE,
            IdnEmail.INSTANCE,
            IdnHostname.INSTANCE,
            Iri.INSTANCE,
            IriReference.INSTANCE,
            JsonPointer.INSTANCE,
            Regex.INSTANCE,
            RelativeJsonPointer.INSTANCE,
            Time.INSTANCE,
            Uri.INSTANCE,
            UriReference.INSTANCE,
            UriTemplate.INSTANCE,
            Uuid.INSTANCE // added
            );

    private final URI id;
    private final URI metaschemaId;

    private final Map<String, FormatAttribute> defaultAttributs;
    private final FormatType defaultFormatType;

    FormatVocabulary(FormatAttribute... attributes) {
        this("", "", attributes);
    }

    FormatVocabulary(String id, String metaschemaId, FormatAttribute... attributes) {
        this.id = URI.create(id);
        this.metaschemaId = URI.create(metaschemaId);
        this.defaultAttributs = buildMap(new HashMap<>(), Arrays.asList(attributes));
        this.defaultFormatType = new FormatType(defaultAttributs);
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
        return Arrays.asList(createFormatType(config, valueSetLoader));
    }

    @Override
    public String getMetaschemaName() {
        return "format";
    }

    public Map<String, FormatAttribute> getDefaultFormatAttributes() {
        return defaultAttributs;
    }

    private KeywordType createFormatType(Map<String, Object> config, KeywordValueSetLoader valueSetLoader) {
        if (testCustomFormats(config)) {
            Collection<FormatAttribute> attributes = valueSetLoader.loadKeywordValueSet(FormatAttribute.class);
            if (!attributes.isEmpty()) {
                Map<String, FormatAttribute> map = buildMap(new HashMap<>(defaultAttributs), attributes);
                return new FormatType(map);
            }
        }
        return defaultFormatType;
    }

    private static Map<String, FormatAttribute> buildMap(Map<String, FormatAttribute> map,
            Collection<FormatAttribute> attributes) {
        for (FormatAttribute attribute : attributes) {
            map.put(attribute.name(), attribute);
        }
        return Collections.unmodifiableMap(map);
    }

    private static boolean testCustomFormats(Map<String, Object> config) {
        return config.get(JsonSchemaReader.CUSTOM_FORMATS) == Boolean.TRUE;
    }
}
