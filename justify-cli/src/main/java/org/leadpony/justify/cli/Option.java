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
package org.leadpony.justify.cli;

/**
 * An command-line option interface.
 *
 * @author leadpony
 */
interface Option {

    /**
     * Returns whether this option is mandatory or not.
     *
     * @return {@code true} if this option is mandatory, {@code false} otherwise.
     */
    boolean isRequired();

    /**
     * Returns the preferred name for this option.
     *
     * @return the preferred name for this option.
     */
    String preferredName();

    /**
     * Checks whether this option requires an argument or not.
     *
     * @return {@code true} if this option requires any argument, {@code false}
     *         otherwise.
     */
    boolean requiresArgument();

    /**
     * Checks whether this option can take multiple arguments or not.
     *
     * @return {@code true} if this option can take multiple arguments,
     *         {@code false} otherwise.
     */
    boolean takesMultipleArguments();

    /**
     * Returns the typed argument.
     *
     * @param string the original argument.
     * @return the typed argument.
     */
    default Object getTypedArgument(String string) {
        return string;
    }
}
