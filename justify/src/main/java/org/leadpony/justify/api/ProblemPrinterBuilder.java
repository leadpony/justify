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

/**
 * A builder interface for building a problem printer instance.
 *
 * @author leadpony
 */
public interface ProblemPrinterBuilder {

    /**
     * Builds a new instance of problem printer which was configured through this
     * interface.
     *
     * @return newly created instance of problem printer as a
     *         {@code ProblemHandler}, never be {@code null}.
     */
    ProblemHandler build();

    /**
     * Specifies the target locale for which the messages to present will be
     * localized.
     *
     * <p>
     * The default locale obtained via {@link Locale#getDefault()} is used by
     * default.
     * </p>
     *
     * @param locale the target locale for which the messages will be localized.
     * @return this builder.
     * @throws NullPointerException if the specified {@code locale} is {@code null}.
     */
    ProblemPrinterBuilder withLocale(Locale locale);

    /**
     * Specifies whether problem locations should be presented in the form of line
     * and column numbers or not.
     *
     * <p>
     * The value of this option is set to {@code true} by default.
     * </p>
     *
     * @param present {@code true} if the locations should be presented,
     *                {@code false} if the locations should be omitted.
     * @return this builder.
     */
    ProblemPrinterBuilder withLocation(boolean present);

    /**
     * Specifies whether problem locations should be presented in the form of a JSON
     * pointer or not.
     *
     * <p>
     * The value of this option is set to {@code true} by default.
     * </p>
     *
     * @param present {@code true} if the pointers should be presented,
     *                {@code false} if the pointers should be omitted.
     * @return this builder.
     */
    ProblemPrinterBuilder withPointer(boolean present);
}
