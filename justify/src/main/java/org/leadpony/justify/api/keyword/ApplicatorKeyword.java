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
package org.leadpony.justify.api.keyword;

/**
 * A keyword which applies subschemas to JSON instances.
 *
 * @author leadpony
 */
public interface ApplicatorKeyword extends EvaluationKeyword {

    /**
     * The location where applicator applies schemas.
     *
     * @author leadpony
     */
    enum ApplicableLocation {
        /**
         * Schemas will be applied in-place to the current location.
         */
        CURRENT,
        /**
         * Schemas will be applied to a child location.
         */
        CHILD
    }

    /**
     * {@inheritDoc}
     *
     * An ApplicatorKeyword can evaluate JSON instances by default.
     */
    @Override
    default boolean canEvaluate() {
        return true;
    }

    /**
     * Return the location where this applicator applies schemas.
     *
     * @return the location.
     */
    ApplicableLocation getApplicableLocation();

    /**
     * Returns whether this keyword is an in-place applicator or not.
     *
     * @return {@code true} if this keyword is an in-place applicator, {@code false}
     *         otherwise.
     */
    default boolean isInPlace() {
        return getApplicableLocation() == ApplicableLocation.CURRENT;
    }
}
