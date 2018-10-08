/*
 * Copyright 2018 the Justify authors.
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
import java.util.stream.Collectors;

import org.leadpony.justify.core.Localized;

/**
 * Formatter of message.
 * 
 * @author leadpony
 */
class MessageFormatter {

    private final String input;
    private final ResourceBundle bundle;
    private int offset;
   
    private Map<String, Object> parameters;
    
    /**
     * Constructs this formatter.
     * 
     * @param input the original message.
     * @param bundle the resource bundle to be used for localization.
     */
    MessageFormatter(String input, ResourceBundle bundle) {
        this.input = input;
        this.bundle = bundle;
        this.offset= 0;
    }
  
    /**
     * Formats the message.
     * @param parameters the values for variables.
     * @return the formatted message.
     */
    String format(Map<String, Object> parameters) {
        this.parameters = parameters;
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
        String modifier = (tokens.length > 1) ? tokens[1] : null;
        Object value = resolveVariable(name);
        return stringify(value, modifier);
    }
    
    private Object resolveVariable(String name) {
        if (parameters.containsKey(name)) {
            return parameters.get(name);
        }
        return bundle.getObject(name);
    }
    
    private String stringify(Object object, String modifier) {
        String string = null;
        if (object instanceof Collection<?>) {
            return collectionToString(object, modifier);
        } else if (object instanceof Enum<?>) {
            string = enumToString(object);
        } else if (object instanceof Localized) {
            string = localizedToString(object);
        } else if (object instanceof String) {
            string = stringToString(object);
        } else {
            string = object.toString();
        }
        return modify(string, modifier);
    }
    
    private String modify(String source, String modifier) {
        if ("capitalize".equals(modifier)) {
            return capitalizeFirst(source);
        }
        return source;
    }

    private static String capitalizeFirst(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        char[] chars = string.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
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
        return (String)object;
    }
    
    private String localizedToString(Object object) {
        Localized localized = (Localized)object;
        return localized.getLocalized(bundle.getLocale());
    }

    private String enumToString(Object object) {
        Enum<?> actual = (Enum<?>)object;
        String className = actual.getClass().getSimpleName();
        String key = className + "." + actual.name();
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return actual.name();
        }
    }

    private String collectionToString(Object object, String modifier) {
        Collection<?> actual = (Collection<?>)object;
        StringBuilder sb = new StringBuilder();
        return sb.append("[")
                 .append(actual.stream()
                               .map(item->stringify(item, modifier))
                               .collect(Collectors.joining(", ")))
                 .append("]")
                 .toString();
    }
}
