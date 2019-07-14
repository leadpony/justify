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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

/**
 * A base type of tests using {@link JsonValidationService}.
 *
 * @author leadpony
 */
public abstract class BaseTest {

    static final JsonValidationService SERVICE = JsonValidationService.newInstance();

    private static Logger log;
    private static ProblemHandler printer;

    @BeforeAll
    public static void setUpBaseOnce(TestInfo info) {
        Class<?> testClass = info.getTestClass().get();
        log = Logger.getLogger(testClass.getName());
        printer = SERVICE.createProblemPrinter(log::info);
    }

    static void print(String message) {
        log.info(message);
    }

    static void print(Throwable thrown) {
        log.info(thrown.toString());
    }

    static void print(List<Problem> problems) {
        if (!problems.isEmpty()) {
            printer.handleProblems(problems);
        }
    }

    static boolean isLoggable() {
        return log.isLoggable(Level.INFO);
    }
}
