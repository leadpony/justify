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

import java.util.Locale;
import java.util.Map;

import javax.json.stream.JsonLocation;

/**
 * A problem found during the validation.
 * <p>
 * If the problem represents alternative groups of problems,
 * actual type will be extended to be {@link BranchProblem}.
 * </p>
 *
 * @author leadpony
 * @see BranchProblem
 */
public interface Problem {

    /**
     * Return the message describing this problem.
     *
     * @return the message describing this problem, never be {@code null}.
     */
    default String getMessage() {
        return getMessage(Locale.getDefault());
    }

    /**
     * Return the message describing this problem,
     * which will be localized for the specified locale.
     *
     * @param locale the locale for which the message will be localized.
     * @return the message describing this problem, never be {@code null}.
     * @throws NullPointerException if the specified {@code locale} is {@code null}.
     */
    String getMessage(Locale locale);

    /**
     * Return the message describing this problem,
     * which includes the location where this problem is found in the input source.
     *
     * @return the message of this problem.
     */
    default String getContextualMessage() {
        return getContextualMessage(Locale.getDefault());
    }

    /**
     * Return the message describing this problem,
     * which includes the location where this problem is found in the input source.
     * The message will be localized for the specified locale.
     *
     * @param locale the locale for which the message will be localized.
     * @return the message of this problem.
     * @throws NullPointerException if the specified {@code locale} is {@code null}.
     */
    String getContextualMessage(Locale locale);

    /**
     * Returns the location where this problem is found in the input source.
     *
     * @return the location where this problem occurred.
     *         This can be {@code null} if the location is unknown.
     */
    JsonLocation getLocation();

    /**
     * Returns the JSON schema which provided the assertion.
     *
     * @return the JSON schema which provided the assertion, never be {@code null}.
     */
    JsonSchema getSchema();

    /**
     * Returns the keyword which provided the assertion.
     *
     * @return the keyword which provided the assertion. This may be {@code null}.
     */
    String getKeyword();

    /**
     * Returns all parameters of this problem as a map.
     *
     * @return the map containing all parameters this problem has.
     *         The map may be empty, but never be {@code null}.
     */
    Map<String, ?> parametersAsMap();

    /**
     * Checks if this problem is resolvable or not.
     * @return {@code true} if this problem can be resolved.
     *         {@code false} if this problem is inevitable and cannot be resolved.
     */
    boolean isResolvable();

    /**
     * Returns the same string as {@link #getContextualMessage()} for the default locale.
     *
     * @return the message describing this problem including the location, never be {@code null}.
     */
    @Override
    String toString();
}
