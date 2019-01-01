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

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message contained in the resource bundle for this library.
 * 
 * @author leadpony
 */
public class Message {
    
    private static final String BUNDLE_BASE_NAME = "org/leadpony/justify/internal/messages";

    private final String pattern;
    private final ResourceBundle bundle;
    
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
    
    public static String asString(String key) {
        return asString(key, Locale.getDefault());
    }

    public static String asString(String key, Locale locale) {
        ResourceBundle bundle = getBundle(locale);
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            throw e;
        }
    }
    
    private Message(String pattern, ResourceBundle bundle) {
        this.pattern = pattern;
        this.bundle = bundle;
    }
    
    public String format(Map<String, Object> arguments) {
        MessageFormatter formatter = new MessageFormatter(this.pattern, this.bundle);
        return formatter.format(arguments);
    }
    
    /*
     * Note that {@code ResourceBundle.Control} cannot be used in named modules.
     */
    private static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, Message.class.getClassLoader());
    }
}
