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

package org.leadpony.justify.internal.base;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.leadpony.justify.api.Localizable;

/**
 * Formatter of message.
 *
 * @author leadpony
 */
class MessageFormatter {

    private final String input;
    private final ResourceBundle bundle;
    private int offset;

    private Map<String, Object> arguments;

    /**
     * Constructs this formatter.
     *
     * @param input  the original message.
     * @param bundle the resource bundle to be used for localization.
     */
    MessageFormatter(String input, ResourceBundle bundle) {
        this.input = input;
        this.bundle = bundle;
        this.offset = 0;
    }

    /**
     * Formats the message.
     *
     * @param arguments the values for variables.
     * @return the formatted message.
     */
    String format(Map<String, Object> arguments) {
        this.arguments = arguments;
        StringBuilder builder = new StringBuilder();
        while (hasNext()) {
            char c = next();
            if (c == '{') {
                builder.append(placeholder());
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private String placeholder() {
        StringBuilder builder = new StringBuilder();
        while (hasNext()) {
            char c = next();
            if (c == '}') {
                break;
            } else {
                builder.append(c);
            }
        }
        return expandVariable(builder.toString());
    }

    private String expandVariable(String spec) {
        String[] tokens = spec.split("\\|");
        String name = tokens[0];
        Function<String, String> modifier = Function.identity();
        for (int i = 1; i < tokens.length; i++) {
            modifier = modifier.andThen(Modifier.byName(tokens[i]));
        }
        Object value = resolveVariable(name);
        return stringify(value, modifier);
    }

    private Object resolveVariable(String name) {
        if (arguments.containsKey(name)) {
            return arguments.get(name);
        }
        throw new IllegalArgumentException("variable \"" + name + "\" is undefined.");
    }

    private String stringify(Object object, Function<String, String> modifier) {
        String string = null;
        if (object == null) {
            string = "null";
        } else if (object instanceof Collection<?>) {
            return collectionToString(object, modifier);
        } else if (object instanceof Localizable) {
            string = localizedToString(object);
        } else if (object instanceof Enum<?>) {
            string = enumToString(object);
        } else if (object instanceof String) {
            string = stringToString(object);
        } else {
            string = object.toString();
        }
        return modifier.apply(string);
    }

    private boolean hasNext() {
        return offset < input.length();
    }

    private char next() {
        if (offset < input.length()) {
            return input.charAt(offset++);
        } else {
            throw new NoSuchElementException();
        }
    }

    private String stringToString(Object object) {
        return (String) object;
    }

    private String localizedToString(Object object) {
        Localizable localized = (Localizable) object;
        return localized.getLocalized(bundle.getLocale());
    }

    private String enumToString(Object object) {
        Enum<?> actual = (Enum<?>) object;
        String className = actual.getClass().getSimpleName();
        String key = className + "." + actual.name();
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return actual.name();
        }
    }

    private String collectionToString(Object object, Function<String, String> modifier) {
        Collection<?> actual = (Collection<?>) object;
        StringBuilder sb = new StringBuilder();
        return sb.append("[")
                .append(actual.stream()
                        .map(item -> stringify(item, modifier))
                        .collect(Collectors.joining(", ")))
                .append("]")
                .toString();
    }

    /**
     * Variable modifier.
     *
     * @author leadpony
     */
    private enum Modifier implements Function<String, String> {

        CAPITALIZE() {
            @Override
            public String apply(String t) {
                if (t.isEmpty()) {
                    return t;
                }
                char[] chars = t.toCharArray();
                chars[0] = Character.toUpperCase(chars[0]);
                return new String(chars);
            }
        },

        QUOTE() {
            @Override
            public String apply(String t) {
                return new StringBuilder()
                        .append('"')
                        .append(t)
                        .append('"')
                        .toString();
            }
        };

        @Override
        public String apply(String t) {
            return t;
        }

        public static Modifier byName(String name) {
            return valueOf(name.toUpperCase());
        }
    }
}
