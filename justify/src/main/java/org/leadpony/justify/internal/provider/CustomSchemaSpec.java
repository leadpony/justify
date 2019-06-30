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
package org.leadpony.justify.internal.provider;

import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.schema.binding.KeywordBinder;
import org.leadpony.justify.internal.schema.io.SchemaSpec;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A custom JSON Schema specification.
 *
 * @author leadpony
 */
class CustomSchemaSpec implements SchemaSpec {

    private final SchemaSpec baseSpec;
    private final Map<String, FormatAttribute> customFormatAttributes;

    /**
     * Constructs this object.
     *
     * @param baseSpec the base specification.
     * @param formatAttributes the format attributes to be added.
     */
    CustomSchemaSpec(SchemaSpec baseSpec, Map<String, FormatAttribute> formatAttributes) {
        this.baseSpec = baseSpec;
        this.customFormatAttributes = formatAttributes;
    }

    @Override
    public SpecVersion getVersion() {
        return baseSpec.getVersion();
    }

    @Override
    public JsonSchema getMetaschema() {
        return baseSpec.getMetaschema();
    }

    @Override
    public Map<String, KeywordBinder> getKeywordBinders() {
        return baseSpec.getKeywordBinders();
    }

    @Override
    public FormatAttribute getFormatAttribute(String name) {
        FormatAttribute attribute = customFormatAttributes.get(name);
        if (attribute == null) {
            attribute = baseSpec.getFormatAttribute(name);
        }
        return attribute;
    }

    @Override
    public ContentEncodingScheme getEncodingScheme(String name) {
        return baseSpec.getEncodingScheme(name);
    }

    @Override
    public ContentMimeType getMimeType(String value) {
        return baseSpec.getMimeType(value);
    }
}
