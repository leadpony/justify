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

package org.leadpony.justify.internal.problem;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/**
 * A problem handler which will print the problems.
 *
 * @author leadpony
 */
final class ProblemPrinter implements ProblemHandler {

    private final ProblemRenderer renderer;
    private final Consumer<String> lineConsumer;
    private final Locale locale;

    /**
     * Constructs this object.
     *
     * @param renderer     the problem renderer.
     * @param lineConsumer the object which will output the line to somewhere.
     * @param locale       the locale for which the problem messages will be
     *                     localized.
     */
    ProblemPrinter(ProblemRenderer renderer, Consumer<String> lineConsumer, Locale locale) {
        this.renderer = renderer;
        this.lineConsumer = lineConsumer;
        this.locale = locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleProblems(List<Problem> problems) {
        for (Problem problem : problems) {
            renderer.render(problem, locale, lineConsumer);
        }
    }
}
