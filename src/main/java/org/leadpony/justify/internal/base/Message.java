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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Message contained in resource bundle for this library.
 * 
 * @author leadpony
 */
public class Message {
    
    private static final String BUNDLE_BASE_NAME = "org.leadpony.justify.internal.message";

    private final String pattern;
    private final ResourceBundle bundle;
    private Map<String, Object> parameters;
    
    public static Message get(String key) {
        return get(key, Locale.getDefault());
    }

    public static Message get(String key, Locale locale) {
        ResourceBundle bundle = getBundle(locale);
        try {
            return new Message(bundle.getString(key), bundle);
        } catch (MissingResourceException e) {
            throw e;
        }
    }
    
    public static String getAsString(String key) {
        return getAsString(key, Locale.getDefault());
    }

    public static String getAsString(String key, Locale locale) {
        ResourceBundle bundle = getBundle(locale);
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
    
    private Message(String pattern, ResourceBundle bundle) {
        this.pattern = pattern;
        this.bundle = bundle;
    }
    
    public Message withParameter(String name, Object value) {
        addParameter(name, value);
        return this;
    }

    public Message withParameter(String name, Message message) {
        addParameter(name, message);
        return this;
    }

    public Message withParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }
    
    @Override
    public String toString() {
        if (parameters == null || parameters.isEmpty()) {
            return pattern;
        } else {
            MessageFormatter formatter  = new MessageFormatter(pattern, this::resolve);
            return formatter.format();
        }
    }

    private static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);    
    }
    
    private void addParameter(String name, Object value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(name, value);
    }
    
    private String resolve(String name) {
        return mapToString(replace(name));
    }
    
    private Object replace(String name) {
        Object replacement = parameters.get(name);
        if (replacement == null) {
            replacement = Message.get(name);
        }
        return replacement;
    }
    
    private String mapToString(Object value) {
        if (value instanceof Collection) {
            return mapToString((Collection<?>)value);
        } else if (value instanceof Enum) {
            Enum<?> enumType = ((Enum<?>)value);
            String className = enumType.getClass().getSimpleName();
            String key = className + "." + enumType.name();
            if (bundle.containsKey(key)) {
                return bundle.getString(key);
            } else {
                return enumType.name();
            }
        } else if (value instanceof String) {
            return mapToString((String)value);
        } else {
            return value.toString();
        }
    }
    
    private String mapToString(Collection<?> value) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(value.stream()
                  .map(e->mapToString(e))
                  .collect(Collectors.joining(", ")));
        sb.append("]");
        return sb.toString();
    }
    
    private static String mapToString(String value) {
        return "\"" + value + "\"";
    }
}
