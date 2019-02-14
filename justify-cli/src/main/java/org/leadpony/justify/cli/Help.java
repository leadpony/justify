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
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * A command implementation for "help" command.
 *
 * @author leadpony
 */
class Help extends AbstractCommand {

    /**
     * Constructs this command.
     *
     * @param console the console to which messages will be outputted.
     */
    Help(Console console) {
        super(console);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status execute(List<String> args) {
        printUsage();
        return Status.VALID;
    }

    /**
     * Prints the help message which explains the usage of this program.
     */
     private void printUsage() {
        InputStream in = findUsageResourceAsStream();
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                reader.lines().forEach(console::info);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
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
