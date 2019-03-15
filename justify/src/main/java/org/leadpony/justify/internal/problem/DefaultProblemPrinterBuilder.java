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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.stream.JsonLocation;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ProblemPrinterBuilder;
import org.leadpony.justify.internal.base.Message;

/**
 * The default implementation of {@link ProblemPrinterBuilder}.
 *
 * @author leadpony
 */
public class DefaultProblemPrinterBuilder implements ProblemPrinterBuilder {

    private static final ProblemFormatter SIMPLE_FORMAT = (problem, locale)->{
        return problem.getMessage(locale);
    };

    private static final ProblemFormatter LOCATION_ONLY_FORMAT = (problem, locale)->{
        return Message.LINE_WITH_LOCATION.format(formatArgs(problem, locale), locale);
    };

    private static final ProblemFormatter POINTER_ONLY_FORMAT = (problem, locale)->{
        return Message.LINE_WITH_POINTER.format(formatArgs(problem, locale), locale);
    };

    private static final ProblemFormatter LOCATION_AND_POINTER_FORMAT = (problem, locale)->{
        return Message.LINE_WITH_BOTH.format(formatArgs(problem, locale), locale);
    };

    private final Consumer<String> lineConsumer;
    private Locale locale = Locale.getDefault();
    private boolean location = true;
    private boolean pointer = true;

    public DefaultProblemPrinterBuilder(Consumer<String> lineConsumer) {
        this.lineConsumer = lineConsumer;
    }

    @Override
    public ProblemHandler build() {
        return new ProblemPrinter(lineConsumer, locale, createFormatter());
     }

    @Override
    public ProblemPrinterBuilder withLocale(Locale locale) {
        requireNonNull(locale, "locale");
        this.locale = locale;
        return this;
    }

    @Override
    public ProblemPrinterBuilder withLocation(boolean present) {
        this.location = present;
        return this;
    }

    @Override
    public ProblemPrinterBuilder withPointer(boolean present) {
        this.pointer = present;
        return this;
    }

    private static Map<String, Object> formatArgs(Problem problem, Locale locale) {
        Map<String, Object> args = new HashMap<>();
        args.put("message", problem.getMessage(locale));
        JsonLocation location = problem.getLocation();
        if (location == null) {
            args.put("row", "?");
            args.put("col", "?");
        } else {
            args.put("row", location.getLineNumber());
            args.put("col", location.getColumnNumber());
        }
        String pointer = problem.getPointer();
        if (pointer == null) {
            pointer = "?";
        }
        args.put("pointer", pointer);
        return args;
    }

    private ProblemFormatter createFormatter() {
        if (location) {
            if (pointer) {
                return LOCATION_AND_POINTER_FORMAT;
            } else {
                return LOCATION_ONLY_FORMAT;
            }
        } else if (pointer) {
            return POINTER_ONLY_FORMAT;
        }
        return SIMPLE_FORMAT;
    }
}
