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
package org.leadpony.justify.spi;

/**
 * Encoding scheme of the content of the JSON string.
 *
 * <p>
 * Each implementation of this type will be instantiated at startup through SPI,
 * and the single instance will be shared between multiple schemas and
 * validations.
 * </p>
 *
 * @author leadpony
 */
public interface ContentEncodingScheme {

    /**
     * Returns the name of this encoding scheme.
     *
     * @return the name of this encoding scheme, cannot be {@code null}.
     */
    String name();

    /**
     * Checks whether the specified string is encoded in this scheme or not.
     *
     * @param src the string to check.
     * @return {@code true} if the specified {@code src} is encoded in this scheme,
     *         {@code false} otherwise.
     * @throws NullPointerException if the specified {@code src} is {@code null}.
     */
    boolean canDecode(String src);

    /**
     * Decodes the encoded string into a newly allocated byte array using this
     * encoding scheme.
     *
     * @param src the string to decode.
     * @return newly allocated byte array containing the decoded bytes.
     * @throws NullPointerException     if the specified {@code src} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code src} cannot be
     *                                  decoded.
     */
    byte[] decode(String src);
}
