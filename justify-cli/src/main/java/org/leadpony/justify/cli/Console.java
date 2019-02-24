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

import java.io.PrintStream;

/**
 * A console to which messages will be outputted.
 *
 * @author leadpony
 */
class Console {

    /**
     * Color of messages.
     *
     * @author leadpony
     */
    enum Color {
        DEFAULT(""),
        SUCCESS("\u001b[92m"),
        WARNING("\u001b[93m"),
        DANGER("\u001b[91m"),
        ;

        private final String code;

        private Color(String code) {
            this.code = code;
        }

        String code() {
            return code;
        }
    }

    private final PrintStream stdout;
    private final PrintStream stderr;

    /**
     * Constructs this console.
     *
     * @param out the standard output.
     * @param err the error output.
     */
    Console(PrintStream out, PrintStream err) {
        this.stdout = out;
        this.stderr = err;
    }

    /**
     * Specifies the color of the next line.
     *
     * @param color the color of the next line, cannot be {@code null}.
     * @return this console.
     */
    Console withColor(Color color) {
        return this;
    }

    /**
     * Prints a blank line.
     * @return this console.
     */
    Console print() {
        stdout.println();
        return this;
    }

    /**
     * Prints a line.
     *
     * @param line the line to print.
     * @return this console.
     */
    Console print(String line) {
        stdout.println(decorate(line));
        return this;
    }

    /**
     * Prints a message.
     *
     * @param message the message to print.
     * @return this console.
     */
    Console print(Message message) {
        return print(message.toString());
    }

    /**
     * Prints a message formatted with arguments.
     *
     * @param message the message to print.
     * @param arguments the arguments filling the format.
     * @return this console.
     */
    Console print(Message message, Object... arguments) {
        return print(message.format(arguments));
    }

    /**
     * Prints an exception.
     *
     * @param exception the exception to print.
     * @return this console.
     */
    Console print(Exception exception) {
        print(exception.getLocalizedMessage());
        return this;
    }

    /**
     * Prints an error line.
     *
     * @param line the line to print.
     * @return this console.
     */
    Console error(String line) {
        stderr.println(decorate(line));
        return this;
    }

    Console error(Exception exception) {
        return error(exception.getLocalizedMessage());
    }

    protected String decorate(String line) {
        return line;
    }
}
