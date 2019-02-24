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

import static org.leadpony.justify.cli.Message.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A skeletal implementation of {@link Command}.
 *
 * @author leadpony
 */
abstract class AbstractCommand implements Command {

    protected final Console console;
    private final Map<Option, List<Object>> options = new HashMap<>();

    private Status status = Status.VALID;

    protected AbstractCommand(Console console) {
        this.console = console;
    }

    Status getStatus() {
        return status;
    }

    /**
     * Assigns the status of this command.
     *
     * @param status the status of this command.
     */
    void setStatus(Status status) {
        this.status = status;
    }

    boolean containsOption(Option option) {
        return options.containsKey(option);
    }

    Object getOptionValue(Option option) {
        List<?> values = getOptionValues(option);
        return (values.isEmpty()) ? null : values.get(0);
    }

    List<?> getOptionValues(Option option) {
        if (options.containsKey(option)) {
            return options.get(option);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     *
     * @param args the arguments specified on the command line.
     * @throws CommandException if an error occurred while parsing arguments..
     */
    protected void parseCommandArguments(List<String> args) {
        List<String> nonOptionArgs = new ArrayList<>();

        while (!args.isEmpty()) {
            String arg = args.remove(0);
            if (arg.startsWith("-")) {
                parseOption(arg, args);
            } else {
                nonOptionArgs.add(arg);
            }
        }

        if (!nonOptionArgs.isEmpty()) {
            processNonOptionArguments(nonOptionArgs);
        }

        checkRequiredOptions();
    }

    protected void addOption(Option option) {
        options.put(option, Collections.emptyList());
    }

    protected void addOption(Option option, List<Object> args) {
        if (options.containsKey(option)) {
            options.get(option).addAll(args);
        } else {
            options.put(option, args);
        }
    }

    private void parseOption(String arg, List<String> remaining) {
        Option option = findOptionByName(arg);
        if (option.requiresArgument()) {
            if (remaining.isEmpty() || remaining.get(0).startsWith("-")) {
                throw new CommandException(OPTION_ARGUMENT_MISSING, arg);
            }
            addOption(option, parseOptionArguments(option, remaining));
        } else {
            addOption(option);
        }
    }

    private List<Object> parseOptionArguments(Option option, List<String> remaining) {
        List<Object> args = new ArrayList<>();
        while (!remaining.isEmpty()) {
            String arg = remaining.get(0);
            if (arg.startsWith("-")) {
                break;
            }
            remaining.remove(0);
            try {
                args.add(option.getTypedArgument(arg));
            } catch (IllegalArgumentException e) {
                throw new CommandException(OPTION_ARGUMENT_INVALID, option.preferredName(), arg);
            }
            if (!option.takesMultipleArguments()) {
                break;
            }
        }
        return args;
    }

    private void checkRequiredOptions() {
        for (Option option : getRequiredOptions()) {
            if (!options.containsKey(option)) {
                throw new CommandException(OPTION_MISSING, option.preferredName());
            }
        }
    }

    protected Option findOptionByName(String arg) {
        throw new CommandException(OPTION_UNRECOGNIZED, arg);
    }

    /**
     * Processes the non-option arguments.
     *
     * @param args the non-option arguments.
     */
    protected void processNonOptionArguments(List<String> args) {
    }

    protected Set<? extends Option> getRequiredOptions() {
        return Collections.emptySet();
    }
}
