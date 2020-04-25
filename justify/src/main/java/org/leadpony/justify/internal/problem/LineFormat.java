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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.json.stream.JsonLocation;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.Message;

/**
 * A line format of a problem message.
 *
 * @author leadpony
 */
enum LineFormat {
    MINIMAL() {
        @Override
        String format(Problem problem, Locale locale) {
            return problem.getMessage(locale);
        }
    },

    LOCATION_ONLY() {
        @Override
        String format(Problem problem, Locale locale) {
            return Message.LINE_WITH_LOCATION.format(formatArgs(problem, locale), locale);
        }
    },

    POINTER_ONLY() {
        @Override
        String format(Problem problem, Locale locale) {
            return Message.LINE_WITH_POINTER.format(formatArgs(problem, locale), locale);
        }
    },

    FULL() {
        @Override
        String format(Problem problem, Locale locale) {
            return Message.LINE_WITH_BOTH.format(formatArgs(problem, locale), locale);
        }
    };

    static LineFormat get(boolean location, boolean pointer) {
        if (location) {
            if (pointer) {
                return FULL;
            } else {
                return LOCATION_ONLY;
            }
        } else if (pointer) {
            return POINTER_ONLY;
        }
        return MINIMAL;
    }

    abstract String format(Problem problem, Locale locale);

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
}
