/*
 * Copyright 2018-2020 the Justify authors.
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

import org.leadpony.justify.api.EvaluatorContext;

/**
 * MIME type of the content of the JSON string.
 *
 * <p>
 * Each implementation of this type will be instantiated at startup through SPI,
 * and the single instance will be shared between multiple schemas and
 * validations.
 * </p>
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
     */
    default boolean test(String content) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks whether the specified content is of this MIME type or not with the
     * help of {@link EvaluatorContext}. By default, this method just calls
     * {@link #test(String)}.
     *
     * @param content the content to check, never be {@code null}.
     * @param context the context of the evaluators, never be {@code null}.
     * @return {@code true} if the specified content is of this MIME type,
     *         {@code false} otherwise.
     */
    default boolean test(String content, EvaluatorContext context) {
        return test(content);
    }

    /**
     * Checks whether the specified content is of this MIME type or not.
     *
     * @param decodedContent the content to check, never be {@code null}.
     * @param parameters     the parameters attached to this MIME type, never
     *                       {@code null}.
     * @return {@code true} if the specified content is of this MIME type,
     *         {@code false} otherwise.
     */
    default boolean test(byte[] decodedContent, Map<String, String> parameters) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks whether the specified content is of this MIME type or not with the
     * help of {@link EvaluatorContext}. By default, this method just calls
     * {@link #test(byte[], Map)}.
     *
     * @param decodedContent the content to check, never be {@code null}.
     * @param parameters     the parameters attached to this MIME type, never
     *                       {@code null}.
     * @param context        the context of the evaluators, never be {@code null}
     * @return {@code true} if the specified content is of this MIME type,
     *         {@code false} otherwise.
     */
    default boolean test(byte[] decodedContent, Map<String, String> parameters, EvaluatorContext context) {
        return test(decodedContent, parameters);
    }
}
