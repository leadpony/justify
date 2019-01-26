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
enum Option {
    HELP("h"),
    STRICT("strict");

    private final String name;

    private static final Map<String, Option> names = new HashMap<>();

    static {
        for (Option value : values()) {
            names.put(value.name, value);
        }
    }

    private Option(String name) {
        this.name = name;
    }

    /**
     * Returns the option specified by the name.
     *
     * @param name the name of the option.
     * @return the found option.
     * @throws NoSuchElementException if no such option exists.
     */
    static Option byName(String name) {
        if (names.containsKey(name)) {
            return names.get(name);
        } else {
            throw new NoSuchElementException();
        }
    }
}
