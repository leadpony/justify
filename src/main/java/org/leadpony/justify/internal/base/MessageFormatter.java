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
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author leadpony
 */
public class MessageFormatter {
    
    private static final Pattern PLACEHOLDER_PATTERN = 
            Pattern.compile("\\$\\{(\\p{Alpha}\\w*(\\.\\p{Alpha}\\w*)*)\\}"); 
    
    private static final String BUNDLE_BASE_NAME = "org.leadpony.justify.internal.message";
    
    public String format(String key, Map<String, ?> parameters, Locale locale) {
        try {
            ResourceBundle bundle = getBundle(locale);
            String pattern = bundle.getString(key);
            return replace(pattern, parameters, bundle);
        } catch (MissingResourceException e) {
            return e.getMessage();
        }
    }
    
    private String replace(String pattern, Map<String, ?> parameters, ResourceBundle bundle) {
        Matcher m = PLACEHOLDER_PATTERN.matcher(pattern);
        StringBuilder sb = new StringBuilder();
        int start = 0;
        while (m.find()) {
            sb.append(pattern.substring(start, m.start()));
            String name = m.group(1);
            Object replacement = parameters.get(name);
            if (replacement != null) {
                sb.append(toString(replacement, bundle));
            }
            start = m.end();
        }
        if (start < pattern.length()) {
            sb.append(pattern.substring(start));
        }
        return sb.toString();
    }
    
    private static String toString(Object obj, ResourceBundle bundle) {
        if (obj instanceof Collection) {
            return toString((Collection<?>)obj, bundle);
        } else if (obj instanceof Enum) {
            String className = obj.getClass().getSimpleName();
            String key = className + "." + ((Enum<?>)obj).name();
            return bundle.getString(key);
        } else {
            return obj.toString();
        }
    }
    
    private static String toString(Collection<?> collection, ResourceBundle bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(collection.stream()
                  .map(e->toString(e, bundle))
                  .collect(Collectors.joining(", ")));
        sb.append("]");
        return sb.toString();
    }
    
    private static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);    
    }
}
