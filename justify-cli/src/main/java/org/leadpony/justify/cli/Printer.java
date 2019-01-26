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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * A printer for printing messages.
 *
 * @author leadpony
 */
class Printer {

    private final PrintStream stream;
    private long lines;

    /**
     * Constructs this printer.
     *
     * @param stream the stream to output characters.
     */
    Printer(PrintStream stream) {
        this.stream = stream;
    }

    /**
     * Returns the number of lines this printer printed.
     *
     * @return the number of lines this printer printed.
     */
    long linesPrinted() {
        return lines;
    }

    /**
     * Prints a blank line.
     */
    void print() {
        stream.println();
        ++lines;
    }

    /**
     * Prints a line.
     *
     * @param line the string to print.
     */
    void print(String line) {
        stream.println(line);
        ++lines;
    }

    /**
     * Prints a message.
     *
     * @param message the message to print.
     */
    void print(Message message) {
        print(message.get());
    }

    /**
     * Prints a message formatted with arguments.
     *
     * @param message the message to print.
     * @param arguments the arguments filling the format.
     */
    void print(Message message, Object... arguments) {
        print(message.get(arguments));
    }

    /**
     * Prints an exception.
     *
     * @param exception the exception to print.
     */
    void print(Exception exception) {
        print(exception.getLocalizedMessage());
    }

    /**
     * Prints messages explaining the usage of this program.
     */
    void printUsage() {
        InputStream in = findUsageResourceAsStream();
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                reader.lines().forEach(this::print);
            } catch (IOException e) {
            }
        }
    }

    private InputStream findUsageResourceAsStream() {
        Locale locale = Locale.getDefault();
        String name = "usage_" + locale.getLanguage() + ".txt";
        InputStream stream = getClass().getResourceAsStream(name);
        if (stream != null) {
            return stream;
        }
        return getClass().getResourceAsStream("usage.txt");
    }
}
