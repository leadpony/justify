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
package org.leadpony.justify.internal.base.json;

/**
 * @author leadpony
 */
public final class JsonPointers {

    /**
     * Encodes a reference token.
     *
     * @param token the reference token to encode.
     * @return encoded reference token.
     */
    public static String encode(String token) {
        return token.replaceAll("~", "~0").replaceAll("/", "~1");
    }

    /**
     * Decodes a reference token.
     *
     * @param token the reference token to decode.
     * @return decoded reference token.
     */
    public static String decode(String token) {
        return token.replaceAll("~1", "/").replaceAll("~0", "~");
    }

    private JsonPointers() {
    }
}
