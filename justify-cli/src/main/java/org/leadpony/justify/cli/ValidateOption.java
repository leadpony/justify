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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Options which can be specified on the command line.
 *
 * @author leadpony
 */
enum ValidateOption implements Option {
    HELP("-h"),
    SCHEMA("-s"),
    INSTANCE("-i"),
    REFERENCE("-r"),
    CATALOG("-catalog"),
    STRICT("-strict"),
    ;

    private final String[] names;

    private static final Map<String, ValidateOption> options = new HashMap<>();

    static {
        for (ValidateOption value : values()) {
            for (String name : value.names) {
                options.put(name, value);
            }
        }
    }

    /**
     * Constructs this option.
     *
     * @param names the names of this option.
     */
    private ValidateOption(String... names) {
        this.names = names;
    }

    @Override
    public boolean isRequired() {
        return this == SCHEMA;
    }

    @Override
    public String preferredName() {
        return names[0];
    }

    @Override
    public boolean requiresArgument() {
        switch (this) {
        case SCHEMA:
        case INSTANCE:
        case REFERENCE:
        case CATALOG:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean takesMultipleArguments() {
        switch (this) {
        case INSTANCE:
        case REFERENCE:
            return true;
        default:
            return false;
        }
    }

    @Override
    public Object getTypedArgument(String string) {
        switch (this) {
        case SCHEMA:
        case INSTANCE:
        case REFERENCE:
        case CATALOG:
            return Location.at(string);
        default:
            return string;
        }
    }

    /**
     * Returns the option specified by the name.
     *
     * @param name the name of the option.
     * @return the found option.
     * @throws NoSuchElementException if no such option exists.
     */
    static ValidateOption byName(String name) {
        if (options.containsKey(name)) {
            return options.get(name);
        } else {
            throw new NoSuchElementException();
        }
    }
}
