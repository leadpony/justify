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
package org.leadpony.justify.internal.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Media type which can contains additional parameters.
 *
 * @author leadpony
 */
public class MediaType {

    private final String mimeType;
    private final Map<String, String> parameters;

    public static MediaType valueOf(String value) {
        String[] tokens = value.split("\\s*;\\s*");
        if (tokens.length > 0) {
            String mimeType = tokens[0].trim();
            if (mimeType.isEmpty()) {
                throw newInvalidMediaTypeException("value");
            }
            Map<String, String> parameters = new HashMap<>();
            for (int i = 1; i < tokens.length; ++i) {
                String[] keyValue = tokens[i].split("\\s*=\\s*");
                if (keyValue.length != 2) {
                    throw newInvalidMediaTypeException("value");
                }
                parameters.put(keyValue[0], keyValue[1]);
            }
            return new MediaType(mimeType, parameters);
        }
        throw newInvalidMediaTypeException("value");
    }

    private static RuntimeException newInvalidMediaTypeException(String name) {
        return new IllegalArgumentException(name + " must be a media type.");
    }

    private MediaType(String mimeType, Map<String, String> parameters) {
        this.mimeType = mimeType;
        this.parameters = parameters;
    }

    public String mimeType() {
        return mimeType;
    }

    public Map<String, String> parameters() {
        return parameters;
    }
}
