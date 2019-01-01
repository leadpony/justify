/*
 * Copyright 2018 the Justify authors.
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
package org.leadpony.justify.internal.keyword.assertion.content;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.json.spi.JsonProvider;

import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;

/**
 * Registry of MIME types and encoding schemes.
 * 
 * @author leadpony
 */
public class ContentAttributeRegistry {

    private final Map<String, ContentEncodingScheme> encodingSchemes = new HashMap<>();
    private final Map<String, ContentMimeType> mimeTypes = new HashMap<>();

    private final JsonProvider jsonProvider;

    static final Base64 BASE64 = new Base64();

    public ContentAttributeRegistry(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    public boolean containsEncodingScheme(String name) {
        requireNonNull(name, "name");
        return encodingSchemes.containsKey(name.toLowerCase());
    }

    public ContentEncodingScheme findEncodingScheme(String name) {
        requireNonNull(name, "name");
        return encodingSchemes.get(name.toLowerCase());
    }

    public boolean containsMimeType(String value) {
        requireNonNull(value, "value");
        return mimeTypes.containsKey(value.toLowerCase());
    }

    public ContentMimeType findMimeType(String value) {
        requireNonNull(value, "value");
        return mimeTypes.get(value.toLowerCase());
    }

    public void registerEncodingScheme(ContentEncodingScheme encodingScheme) {
        requireNonNull(encodingScheme, "encodingScheme");
        String key = encodingScheme.name().toLowerCase();
        encodingSchemes.put(key, encodingScheme);
    }

    public void registerMimeType(ContentMimeType mimeType) {
        requireNonNull(mimeType, "mimeType");
        String key = mimeType.toString().toLowerCase();
        mimeTypes.put(key, mimeType);
    }

    /**
     * Registers all builtin MIME types and encoding schemes with this registry.
     * 
     * @return this registry.
     */
    public ContentAttributeRegistry registerDefault() {
        registerEncodingScheme(BASE64);
        registerMimeType(new JsonMimeType(jsonProvider));
        return this;
    }

    public ContentAttributeRegistry registerProvidedEncodingSchemes() {
        ServiceLoader.load(ContentEncodingScheme.class).forEach(this::registerEncodingScheme);
        return this;
    }

    public ContentAttributeRegistry registerProvidedMimeTypes() {
        ServiceLoader.load(ContentMimeType.class).forEach(this::registerMimeType);
        return this;
    }
}
