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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.base.json.DefaultPointerAwareJsonParser;
import org.leadpony.justify.internal.base.json.JsonService;
import org.leadpony.justify.internal.base.json.PointerAwareJsonParser;
import org.leadpony.justify.internal.keyword.assertion.content.ContentAttributes;
import org.leadpony.justify.internal.keyword.assertion.format.FormatAttributes;
import org.leadpony.justify.internal.schema.binding.KeywordBinder;
import org.leadpony.justify.internal.schema.binding.KeywordBinders;
import org.leadpony.justify.internal.schema.io.GenericSchemaReader;
import org.leadpony.justify.internal.schema.io.SchemaSpec;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A standard JSON Schema specificaiton.
 *
 * @author leadpony
 */
abstract class StandardSchemaSpec implements SchemaSpec {

    private final SpecVersion version;
    private final Map<String, KeywordBinder> keywordBinders;
    private final Map<String, FormatAttribute> formatAttributes;

    private final JsonSchema metaschema;

    private final Map<String, ContentEncodingScheme> encodingSchemes = new HashMap<>();
    private final Map<String, ContentMimeType> mimeTypes = new HashMap<>();

    /**
     * Returns all of the published standard specifications.
     *
     * @param jsonService the JSON service.
     * @return all of the published standard specifications.
     */
    static SchemaSpec[] values(JsonService jsonService) {
        return new SchemaSpec[] {
                new Draft04SchemaSpec(jsonService),
                new Draft06SchemaSpec(jsonService),
                new Draft07SchemaSpec(jsonService)
        };
    }

    protected StandardSchemaSpec(SpecVersion version, JsonService jsonService) {
        this.version = version;
        this.keywordBinders = KeywordBinders.getBinders(version);
        this.formatAttributes = FormatAttributes.getAttributes(version);
        this.metaschema = loadMetaschema(version, jsonService);
        this.encodingSchemes.putAll(ContentAttributes.encodingSchemes());
        this.mimeTypes.putAll(ContentAttributes.mimeTypes(jsonService.getJsonProvider()));
    }

    @Override
    public SpecVersion getVersion() {
        return version;
    }

    @Override
    public JsonSchema getMetaschema() {
        return metaschema;
    }

    @Override
    public Map<String, KeywordBinder> getKeywordBinders() {
        return keywordBinders;
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

    private JsonSchema loadMetaschema(SpecVersion version, JsonService jsonService) {
        String name = version.toString().toLowerCase() + ".json";
        InputStream in = getClass().getResourceAsStream(name);
        JsonProvider jsonProvider = jsonService.getJsonProvider();
        JsonParser realParser = jsonProvider.createParser(in);
        PointerAwareJsonParser parser = new DefaultPointerAwareJsonParser(realParser, jsonProvider);
        try (JsonSchemaReader reader = new GenericSchemaReader(parser, jsonService, this, Collections.emptyMap())) {
            return reader.read();
        }
    }

    /**
     * The specification of Draft-04.
     *
     * @author leadpony
     */
    private static class Draft04SchemaSpec extends StandardSchemaSpec {

        Draft04SchemaSpec(JsonService jsonService) {
            super(SpecVersion.DRAFT_04, jsonService);
        }
    }

    /**
     * The specification of Draft-06.
     *
     * @author leadpony
     */
    private static class Draft06SchemaSpec extends StandardSchemaSpec {

        Draft06SchemaSpec(JsonService jsonService) {
            super(SpecVersion.DRAFT_06, jsonService);
        }
    }

    /**
     * The specification of Draft-07.
     *
     * @author leadpony
     */
    private static class Draft07SchemaSpec extends StandardSchemaSpec {

        Draft07SchemaSpec(JsonService jsonService) {
            super(SpecVersion.DRAFT_07, jsonService);
        }
    }
}
