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
import java.util.ResourceBundle;

import org.leadpony.justify.api.Localizable;

/**
 * The base interface of {@link Message}.
 *
 * @author leadpony
 */
interface BaseMessage extends Localizable {

    @Override
    default String getLocalized(Locale locale) {
        return getBundle(locale).getString(name());
    }

    default String format(Map<String, Object> arguments) {
        return format(arguments, Locale.getDefault());
    }

    default String format(Map<String, Object> arguments, Locale locale) {
        ResourceBundle bundle = getBundle(locale);
        String pattern = bundle.getString(name());
        MessageFormatter formatter = new MessageFormatter(pattern, bundle);
        return formatter.format(arguments);
    }

    String name();

    ResourceBundle getBundle(Locale locale);
}
