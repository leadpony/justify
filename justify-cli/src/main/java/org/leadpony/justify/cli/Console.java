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
     * Prints a blank line.
     */
    void info() {
        stdout.println();
    }

    /**
     * Prints a line.
     *
     * @param line the string to print.
     */
    void info(String line) {
        stdout.println(line);
    }

    /**
     * Prints a message.
     *
     * @param message the message to print.
     */
    void info(Message message) {
        info(message.get());
    }

    /**
     * Prints a message formatted with arguments.
     *
     * @param message the message to print.
     * @param arguments the arguments filling the format.
     */
    void info(Message message, Object... arguments) {
        info(message.get(arguments));
    }

    /**
     * Prints an exception.
     *
     * @param exception the exception to print.
     */
    void info(Exception exception) {
        info(exception.getLocalizedMessage());
    }

    void error(String line) {
        stderr.println(line);
    }

    void error(Exception exception) {
        error(exception.getLocalizedMessage());
    }

    void warn(Message message, Object... arguments) {
        StringBuilder b = new StringBuilder(Message.WARNING.get());
        b.append(message.get(arguments));
        info(b.toString());
    }
}
