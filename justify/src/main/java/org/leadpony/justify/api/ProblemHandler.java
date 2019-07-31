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

package org.leadpony.justify.api;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A handler interface for handling the problems found while validating a JSON
 * document.
 *
 * @author leadpony
 */
@FunctionalInterface
public interface ProblemHandler {

    /**
     * A constant which indicates that the validator should throw a
     * {@link JsonValidatingException} instead of calling a problem handler.
     *
     * <p>
     * Note that calling {@link #handleProblems(List)} method of this instance does
     * nothing actually.
     * </p>
     *
     * @since 1.1
     */
    ProblemHandler THROWING = problems -> {
    };

    /**
     * Handles the problems found while validating a JSON document.
     *
     * @param problems the problems found, cannot be {@code null}.
     */
    void handleProblems(List<Problem> problems);

    /**
     * Creates a problem handler which will store problems into the specified
     * collection.
     *
     * @param collection the collection into which problems will be stored.
     * @return newly created instance of problem handler.
     * @throws NullPointerException if the specified {@code collection} is
     *                              {@code null}.
     */
    static ProblemHandler collectingTo(Collection<Problem> collection) {
        Objects.requireNonNull(collection, "collection must not be null.");
        return collection::addAll;
    }

    /**
     * Returns a problem handler which indicates that the validator should throw a
     * {@link JsonValidatingException} instead of calling a handler.
     *
     * <p>
     * Note that this method does not return an actual problem handler which is able
     * to throw an exception.
     * </p>
     *
     * @return {@link #THROWING}
     * @see #THROWING
     * @see JsonValidatingException
     */
    static ProblemHandler throwing() {
        return THROWING;
    }
}
