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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.json.stream.JsonLocation;

import org.junit.jupiter.api.Test;
import org.leadpony.justify.internal.base.json.SimpleJsonLocation;

/**
 * A test class for testing problem printers created by {@link JsonValidationService}.
 *
 * @author leadpony
 */
public class ProblemPrinterTest {

    private static final JsonValidationService service = JsonValidationServices.get();

    @Test
    public void defaultPrinter_shouldPrintBoth() {
        List<Problem> problems = Arrays.asList(
                new MockProblem("hello problem.", 12, 34, "/foo")
                );
        List<String> lines = new ArrayList<>();
        ProblemHandler printer = service.createProblemPrinter(lines::add);
        printer.handleProblems(problems);

        assertThat(lines).hasSize(1);
        assertThat(lines.get(0)).isEqualTo("[12,34][#/foo] hello problem.");
    }

    @Test
    public void printerWithLocation_shouldPrintLocationOnly() {
        List<Problem> problems = Arrays.asList(
                new MockProblem("hello problem.", 12, 34, "/foo")
                );
        List<String> lines = new ArrayList<>();
        ProblemHandler printer = service.createProblemPrinter(lines::add, PrinterOption.INCLUDE_LOCATION);
        printer.handleProblems(problems);

        assertThat(lines).hasSize(1);
        assertThat(lines.get(0)).isEqualTo("[12,34] hello problem.");
    }

    @Test
    public void printerWithPointer_shouldPrintPointerOnly() {
        List<Problem> problems = Arrays.asList(
                new MockProblem("hello problem.", 12, 34, "/foo")
                );
        List<String> lines = new ArrayList<>();
        ProblemHandler printer = service.createProblemPrinter(lines::add, PrinterOption.INCLUDE_POINTER);
        printer.handleProblems(problems);

        assertThat(lines).hasSize(1);
        assertThat(lines.get(0)).isEqualTo("[#/foo] hello problem.");
    }

    private static class MockProblem implements Problem {

        private final String message;
        private final JsonLocation location;
        private final String pointer;

        MockProblem(String message, int lineNumber, int columnNumber, String pointer) {
            this.message = message;
            this.location = new SimpleJsonLocation(lineNumber, columnNumber, 0);
            this.pointer = pointer;
        }

        @Override
        public String getMessage(Locale locale) {
            return message;
        }

        @Override
        public String getContextualMessage(Locale locale) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonLocation getLocation() {
            return location;
        }

        @Override
        public String getPointer() {
            return pointer;
        }

        @Override
        public JsonSchema getSchema() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getKeyword() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, ?> parametersAsMap() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isResolvable() {
            throw new UnsupportedOperationException();
        }
    }
}
