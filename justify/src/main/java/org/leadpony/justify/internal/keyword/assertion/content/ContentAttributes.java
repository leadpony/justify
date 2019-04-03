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
package org.leadpony.justify.internal.keyword.assertion.content;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.json.spi.JsonProvider;

import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;

/**
 * @author leadpony
 */
public class ContentAttributes {

    static final Base64 BASE64 = new Base64();

    public static Map<String, ContentEncodingScheme> encodingSchemes() {
        Map<String, ContentEncodingScheme> result = new HashMap<>();
        addEncodingScheme(result, BASE64);
        for (ContentEncodingScheme scheme : ServiceLoader.load(ContentEncodingScheme.class)) {
            addEncodingScheme(result, scheme);
        }
        return result;
    }

    public static Map<String, ContentMimeType> mimeTypes(JsonProvider jsonProvider) {
        Map<String, ContentMimeType> result = new HashMap<>();
        addMimeType(result, new JsonMimeType(jsonProvider));
        for (ContentMimeType mimeType : ServiceLoader.load(ContentMimeType.class)) {
            addMimeType(result, mimeType);
        }
        return result;
    }

    private static void addEncodingScheme(Map<String, ContentEncodingScheme> schemes, ContentEncodingScheme scheme) {
        schemes.put(scheme.name().toLowerCase(), scheme);
    }

    private static void addMimeType(Map<String, ContentMimeType> mimeTypes, ContentMimeType mimeType) {
        mimeTypes.put(mimeType.toString().toLowerCase(), mimeType);
    }

    private ContentAttributes() {
    }
}
