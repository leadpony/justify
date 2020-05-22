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
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.KeywordValuesLoader;
import org.leadpony.justify.api.Vocabulary;
import org.leadpony.justify.internal.keyword.format.Format.FormatType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * @author leadpony
 */
public enum FormatVocabulary implements Vocabulary {
    DRAFT_04("",
            DateTime.INSTANCE,
            Email.INSTANCE,
            Hostname.INSTANCE,
            Ipv4.INSTANCE,
            Ipv6.INSTANCE,
            Regex.INSTANCE,
            Uri.INSTANCE
            ),

    DRAFT_06("",
            DateTime.INSTANCE,
            Email.INSTANCE,
            Hostname.INSTANCE,
            Ipv4.INSTANCE,
            Ipv6.INSTANCE,
            JsonPointer.INSTANCE,
            Regex.INSTANCE,
            Uri.INSTANCE,
            UriReference.INSTANCE,
            UriTemplate.INSTANCE
            ),

    DRAFT_07("",
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
            UriTemplate.INSTANCE
            );

    private final URI id;
    private final List<KeywordType> defaultTypes;

    FormatVocabulary(String id, FormatAttribute... attributes) {
        this.id = URI.create(id);
        this.defaultTypes = Arrays.asList(new FormatType(attributes));
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public List<KeywordType> getKeywordTypes(Map<String, Object> config, KeywordValuesLoader valuesLoader) {
        return this.defaultTypes;
    }
}
