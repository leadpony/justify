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

import java.util.Map;

/**
 * MIME type of the content of the JSON string.
 *
 * @author leadpony
 */
public interface ContentMimeType {

    /**
     * Returns the string representation of this MIME type.
     *
     * @return the string representation of this MIME type.
     */
    @Override
    String toString();

    /**
     * Checks whether the specified content is of this MIME type or not.
     *
     * @param content the content to check, never be {@code null}.
     * @return {@code true} if the specified content is of this MIME type,
     *         {@code false} otherwise.
     * @throws NullPointerException if the specified {@code content} was
     *                              {@code null}.
     */
    boolean test(String content);

    /**
     * Checks whether the specified content is of this MIME type or not.
     *
     * @param decodedContent the content to check, never be {@code null}.
     * @param parameters     the parameters attached to this MIME type, never
     *                       {@code null}.
     * @return {@code true} if the specified content is of this MIME type,
     *         {@code false} otherwise.
     * @throws NullPointerException if the specified {@code decodedContent} or
     *                              {@code parameters} was {@code null}.
     */
    boolean test(byte[] decodedContent, Map<String, String> parameters);
}
