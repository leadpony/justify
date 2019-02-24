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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A command implementation executing "help" command.
 *
 * @author leadpony
 */
class Help extends AbstractCommand {

    private static final String BUNDLE_BASE_NAME = "org.leadpony.justify.cli.usage";
    private final ResourceBundle bundle;

    /**
     * Constructs this command.
     *
     * @param console the console to which messages will be outputted.
     */
    Help(Console console) {
        super(console);
        this.bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.getDefault());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status execute(List<String> args) {
        printUsage();
        return Status.VALID;
    }

    private String getMessage(String key) {
        return bundle.getString(key);
    }

    private void printUsage() {
        console.print(getMessage("title"));
        console.print(getMessage("usage"));
        console.print("  " + getMessage("command"));
        console.print();
        printAllOptions();
    }

    private void printAllOptions() {
        console.print(getMessage("options"));
        List<Option> options = sortOptions(ValidateOption.values());
        for (Option option : options) {
            printOption(option);
        }
    }

    private void printOption(Option option) {
        String name = option.toString();
        StringBuilder builder = new StringBuilder();
        builder.append("  ").append(option.preferredName());
        if (option.requiresArgument()) {
            String arg = getMessage(name + ".arg");
            builder.append(" <").append(arg).append(">");
            if (option.takesMultipleArguments()) {
                builder.append(" ...");
            }
        }
        if (option.isRequired()) {
            builder.append(" *").append(getMessage("required"));
        }
        console.print(builder.toString());
        console.print(formatDescription(getMessage(name)));
    }

    private static String formatDescription(String description) {
        String replaced = description.replaceAll("\n", "\n      ");
        return "      " + replaced;
    }

    private static List<Option> sortOptions(Option[] options) {
        List<Option> sorted = new ArrayList<>(Arrays.asList(options));
        Collections.sort(sorted, Help::compareOption);
        return sorted;
    }

    private static int compareOption(Option a, Option b) {
        return a.preferredName().compareTo(b.preferredName());
    }
}
