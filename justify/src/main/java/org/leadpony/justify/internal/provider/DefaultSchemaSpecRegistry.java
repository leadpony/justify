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
import java.util.ServiceLoader;

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
import org.leadpony.justify.internal.schema.SchemaCatalog;
import org.leadpony.justify.internal.schema.binding.KeywordBinder;
import org.leadpony.justify.internal.schema.binding.KeywordBinders;
import org.leadpony.justify.internal.schema.io.GenericSchemaReader;
import org.leadpony.justify.internal.schema.io.SchemaSpec;
import org.leadpony.justify.internal.schema.io.SchemaSpecRegistry;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * The default implementation of {@link SchemaSpecRegistry}.
 *
 * @author leadpony
 */
class DefaultSchemaSpecRegistry implements SchemaSpecRegistry {

    private final Map<SpecVersion, SimpleSpec> simpleSpecs = new HashMap<>();
    private final Map<SpecVersion, SimpleSpec> customSpecs = new HashMap<>();

    private final Map<String, ContentEncodingScheme> encodingSchemes = new HashMap<>();
    private final Map<String, ContentMimeType> mimeTypes = new HashMap<>();

    private final SchemaCatalog catalog = new SchemaCatalog();

    /**
     * Loads new instance of this class.
     *
     * @param jsonService the JSON service.
     * @return newly created instance.
     */
    static SchemaSpecRegistry load(JsonService jsonService) {
        DefaultSchemaSpecRegistry registry = new DefaultSchemaSpecRegistry();
        registry.createSpecs(jsonService);
        return registry;
    }

    private DefaultSchemaSpecRegistry() {
    }

    private void createSpecs(JsonService jsonService) {
        buildSimpleSpecs(jsonService);
        buildCustomSpecs();
        this.encodingSchemes.putAll(ContentAttributes.encodingSchemes());
        this.mimeTypes.putAll(ContentAttributes.mimeTypes(jsonService.getJsonProvider()));
    }

    /* As a SchemaSpecRegistry */

    @Override
    public SchemaSpec getSpec(SpecVersion version, boolean full) {
        if (full) {
            return customSpecs.get(version);
        } else {
            return simpleSpecs.get(version);
        }
    }

    @Override
    public SchemaCatalog getMetaschemaCatalog() {
        return catalog;
    }

    /* */

    private void buildSimpleSpecs(JsonService jsonService) {
        for (SpecVersion version : SpecVersion.values()) {
            SimpleSpec spec = new SimpleSpec(
                    version,
                    KeywordBinders.getBinders(version),
                    FormatAttributes.getAttributes(version));
            catalog.addSchema(spec.loadMetaschema(jsonService));
            simpleSpecs.put(version, spec);
        }
    }

    private void buildCustomSpecs() {
        for (SpecVersion version : SpecVersion.values()) {
            customSpecs.put(version, new SimpleSpec(simpleSpecs.get(version)));
        }

        for (FormatAttribute attribute : ServiceLoader.load(FormatAttribute.class)) {
            for (SimpleSpec spec : customSpecs.values()) {
                spec.addFormatAttribute(attribute);
            }
        }
    }

    private class SimpleSpec implements SchemaSpec {

        private final SpecVersion version;
        private final Map<String, KeywordBinder> keywordBinders;
        private final Map<String, FormatAttribute> formatAttributes;
        private JsonSchema metaschema;

        SimpleSpec(SpecVersion version,
                Map<String, KeywordBinder> keywordBinders,
                Map<String, FormatAttribute> formatAttributes) {
            this.version = version;
            this.keywordBinders = keywordBinders;
            this.formatAttributes = formatAttributes;
        }

        SimpleSpec(SimpleSpec other) {
            this.version = other.version;
            this.keywordBinders = other.keywordBinders;
            this.formatAttributes = new HashMap<>(other.formatAttributes);
            this.metaschema = other.metaschema;
        }

        JsonSchema loadMetaschema(JsonService jsonService) {
            String name = getVersion().toString().toLowerCase() + ".json";
            InputStream in = getClass().getResourceAsStream(name);
            JsonProvider jsonProvider = jsonService.getJsonProvider();
            JsonParser realParser = jsonProvider.createParser(in);
            PointerAwareJsonParser parser = new DefaultPointerAwareJsonParser(realParser, jsonProvider);
            try (JsonSchemaReader reader = new GenericSchemaReader(parser, jsonService, this, Collections.emptyMap())) {
                this.metaschema = reader.read();
            }
            return this.metaschema;
        }

        void addFormatAttribute(FormatAttribute attribute) {
            formatAttributes.put(attribute.name(), attribute);
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
            return encodingSchemes.get(name.toLowerCase());
        }

        @Override
        public ContentMimeType getMimeType(String value) {
            return mimeTypes.get(value.toLowerCase());
        }

        @Override
        public String toString() {
            return version.toString();
        }
    }
}
