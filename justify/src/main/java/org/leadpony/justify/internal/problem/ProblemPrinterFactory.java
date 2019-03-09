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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.json.stream.JsonLocation;

import org.leadpony.justify.api.PrinterOption;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.internal.base.Message;

/**
 * A factory type for producing problem printers.
 *
 * @author leadpony
 */
public class ProblemPrinterFactory {

    private static Map<String, Object> args(Problem problem, Locale locale) {
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
        args.put("pointer", problem.getPointer());
        return args;
    }

    private static final ProblemFormatter SIMPLE_FORMAT = (problem, locale)->{
        return problem.getMessage(locale);
    };

    private static final ProblemFormatter LOCATION_ONLY_FORMAT = (problem, locale)->{
        return Message.LINE_WITH_LOCATION.format(args(problem, locale), locale);
    };

    private static final ProblemFormatter POINTER_ONLY_FORMAT = (problem, locale)->{
        return Message.LINE_WITH_POINTER.format(args(problem, locale), locale);
    };

    private static final ProblemFormatter FULL_FORMAT = (problem, locale)->{
        return Message.LINE_WITH_BOTH.format(args(problem, locale), locale);
    };

    public ProblemHandler createPrinter(Consumer<String> lineConsumer, Locale locale, PrinterOption... options) {
        Set<PrinterOption> optionSet = EnumSet.noneOf(PrinterOption.class);
        optionSet.addAll(Arrays.asList(options));
        ProblemFormatter formatter = createFormatter(optionSet);
        return new ProblemPrinter(lineConsumer, locale, formatter);
    }

    private static ProblemFormatter createFormatter(Set<PrinterOption> options) {
        if (options.contains(PrinterOption.INCLUDE_LOCATION)) {
            if (options.contains(PrinterOption.INCLUDE_POINTER)) {
                return FULL_FORMAT;
            } else {
                return LOCATION_ONLY_FORMAT;
            }
        } else if (options.contains(PrinterOption.INCLUDE_POINTER)) {
            return POINTER_ONLY_FORMAT;
        }
        return SIMPLE_FORMAT;
    }
}
