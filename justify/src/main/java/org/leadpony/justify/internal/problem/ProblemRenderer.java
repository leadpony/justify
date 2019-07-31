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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/**
 * @author leadpony
 */
public enum ProblemRenderer implements JsonValidatingException.Renderer {
    DEFAULT_RENDERER;

    @Override
    public String render(List<Problem> problems, Locale locale) {
        requireNonNull(problems, "problems");
        requireNonNull(locale, "locale");
        StringBuilder builder = new StringBuilder();
        Consumer<String> consumer = line -> {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(line);
        };
        ProblemHandler printer = new DefaultProblemPrinterBuilder(consumer)
            .withLocale(locale)
            .build();
        printer.handleProblems(problems);
        return builder.toString();
    }
}
