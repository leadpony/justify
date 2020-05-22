/*
 * Copyright 2018-2019 the Justify authors.
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

import java.util.Map;

import org.leadpony.justify.api.JsonSchemaBuilderFactory;
import org.leadpony.justify.internal.base.json.JsonService;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * The default implementation of {@link JsonSchemaBuilderFactory}.
 *
 * @author leadpony
 */
public class DefaultJsonSchemaBuilderFactory implements JsonSchemaBuilderFactory {

    private final JsonService jsonService;
    private final Map<String, FormatAttribute> formatAttributes;
    private final Map<String, ContentEncodingScheme> encodingSchemes;
    private final Map<String, ContentMimeType> mimeTypes;


    /**
     * Constructs this factory.
     *
     * @param jsonService      the JSON service.
     * @param formatAttributes the value set for "format" keyword.
     * @param encodingSchemes  the value set for "contentEncoding" keyword.
     * @param mimeTypes        the value set for "contentMediaType" keyword.
     */
    public DefaultJsonSchemaBuilderFactory(JsonService jsonService,
            Map<String, FormatAttribute> formatAttributes,
            Map<String, ContentEncodingScheme> encodingSchemes,
            Map<String, ContentMimeType> mimeTypes) {
        this.jsonService = jsonService;
        this.formatAttributes = formatAttributes;
        this.encodingSchemes = encodingSchemes;
        this.mimeTypes = mimeTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultJsonSchemaBuilder createBuilder() {
        return new DefaultJsonSchemaBuilder(jsonService,
                formatAttributes,
                encodingSchemes,
                mimeTypes);
    }
}
