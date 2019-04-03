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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javax.json.spi.JsonProvider;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.assertion.content.ContentAttributes;
import org.leadpony.justify.internal.keyword.assertion.format.FormatAttributes;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * @author leadpony
 */
public class SchemaSpecRegistry {

    private static Map<SpecVersion, List<FormatAttribute>> formatAttriutes = findStandardFormatAttibutes();

    private final Map<SpecVersion, SimpleSpec> vanillaSpecs;
    private final Map<SpecVersion, SimpleSpec> fullSpecs;

    private final Map<String, ContentEncodingScheme> encodingSchemes;
    private final Map<String, ContentMimeType> mimeTypes;

    public SchemaSpecRegistry(JsonProvider jsonProvider) {
        this.vanillaSpecs = createVanillaSpecs();
        this.fullSpecs = createFullSpecs(this.vanillaSpecs);
        this.encodingSchemes = ContentAttributes.encodingSchemes();
        this.mimeTypes = ContentAttributes.mimeTypes(jsonProvider);
    }

    public SchemaSpec getSpec(SpecVersion version, boolean full) {
        if (full) {
            return fullSpecs.get(version);
        } else {
            return vanillaSpecs.get(version);
        }
    }

    private Map<SpecVersion, SimpleSpec> createVanillaSpecs() {
        Map<SpecVersion, SimpleSpec> specs = new EnumMap<>(SpecVersion.class);
        for (SpecVersion version : SpecVersion.values()) {
            SimpleSpec spec = new SimpleSpec();
            spec.addFormatAttributes(formatAttriutes.get(version));
            specs.put(version, spec);
        }
        return specs;
    }

    private Map<SpecVersion, SimpleSpec> createFullSpecs(Map<SpecVersion, SimpleSpec> vanillaSpecs) {
        Map<SpecVersion, SimpleSpec> specs = new EnumMap<>(SpecVersion.class);
        for (SpecVersion version : SpecVersion.values()) {
            specs.put(version, new SimpleSpec(vanillaSpecs.get(version)));
        }

        for (FormatAttribute attribute : ServiceLoader.load(FormatAttribute.class)) {
            for (SimpleSpec spec : specs.values()) {
                spec.addFormatAttribute(attribute);
            }
        }

        return specs;
    }

    private static Map<SpecVersion, List<FormatAttribute>> findStandardFormatAttibutes() {
        Map<SpecVersion, List<FormatAttribute>> map = new HashMap<>();
        for (SpecVersion version : SpecVersion.values()) {
            List<FormatAttribute> list = FormatAttributes.all().stream()
                    .filter(a->checkConformance(a, version))
                    .collect(Collectors.toList());
            map.put(version, list);
        }
        return map;
    }

    private static boolean checkConformance(FormatAttribute attribute, SpecVersion version) {
        Spec spec = attribute.getClass().getAnnotation(Spec.class);
        if (spec != null) {
            for (SpecVersion value : spec.value()) {
                if (value == version) {
                    return true;
                }
            }
        }
        return false;
    }

    private class SimpleSpec implements SchemaSpec {

        private final Map<String, FormatAttribute> formatAttributes;

        SimpleSpec() {
            formatAttributes = new HashMap<>();
        }

        SimpleSpec(SimpleSpec other) {
            formatAttributes = new HashMap<>(other.formatAttributes);
        }

        void addFormatAttribute(FormatAttribute attribute) {
            formatAttributes.put(attribute.name(), attribute);
        }

        void addFormatAttributes(Iterable<FormatAttribute> attributes) {
            for (FormatAttribute attribute : attributes) {
                addFormatAttribute(attribute);
            }
        }

        @Override
        public boolean supportsFormatAttribute(String name) {
            return formatAttributes.containsKey(name);
        }

        @Override
        public FormatAttribute getFormatAttribute(String name) {
            return formatAttributes.get(name);
        }

        @Override
        public boolean supportsEncodingScheme(String name) {
            return encodingSchemes.containsKey(name.toLowerCase());
        }

        @Override
        public ContentEncodingScheme getEncodingScheme(String name) {
            return encodingSchemes.get(name.toLowerCase());
        }

        @Override
        public boolean supportsMimeType(String value) {
            return mimeTypes.containsKey(value.toLowerCase());
        }

        @Override
        public ContentMimeType getMimeType(String value) {
            return mimeTypes.get(value.toLowerCase());
        }
    }
}
