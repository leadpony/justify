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
 * All Messages defined in the resource bundle for this library.
 *
 * @author leadpony
 */
public enum Message implements Localizable {
    LINE,
    LOCATION,
    LOCATION_UNKNOWN,

    STRING_KEY,
    STRING_VALUE,

    FORMAT_DATE,
    FORMAT_DATE_TIME,
    FORMAT_EMAIL,
    FORMAT_HOSTNAME,
    FORMAT_IDN_EMAIL,
    FORMAT_IDN_HOSTNAME,
    FORMAT_IPV4,
    FORMAT_IPV6,
    FORMAT_IRI,
    FORMAT_IRI_REFERENCE,
    FORMAT_JSON_POINTER,
    FORMAT_REGEX,
    FORMAT_RELATIVE_JSON_POINTER,
    FORMAT_TIME,
    FORMAT_URI,
    FORMAT_URI_REFERENCE,
    FORMAT_URI_TEMPLATE,

    /* Problem messages for instance validation */

    INSTANCE_PROBLEM_TYPE,
    INSTANCE_PROBLEM_TYPE_PLURAL,
    INSTANCE_PROBLEM_ENUM,
    INSTANCE_PROBLEM_CONST,

    INSTANCE_PROBLEM_NOT_TYPE,
    INSTANCE_PROBLEM_NOT_TYPE_PLURAL,
    INSTANCE_PROBLEM_NOT_ENUM,
    INSTANCE_PROBLEM_NOT_CONST,

    INSTANCE_PROBLEM_MULTIPLEOF,
    INSTANCE_PROBLEM_MAXIMUM,
    INSTANCE_PROBLEM_EXCLUSIVEMAXIMUM,
    INSTANCE_PROBLEM_MINIMUM,
    INSTANCE_PROBLEM_EXCLUSIVEMINIMUM,

    INSTANCE_PROBLEM_NOT_MULTIPLEOF,

    INSTANCE_PROBLEM_MAXLENGTH,
    INSTANCE_PROBLEM_MINLENGTH,
    INSTANCE_PROBLEM_PATTERN,

    INSTANCE_PROBLEM_NOT_MAXLENGTH,
    INSTANCE_PROBLEM_NOT_MINLENGTH,
    INSTANCE_PROBLEM_NOT_PATTERN,

    INSTANCE_PROBLEM_MAXITEMS,
    INSTANCE_PROBLEM_MINITEMS,
    INSTANCE_PROBLEM_UNIQUEITEMS,
    INSTANCE_PROBLEM_CONTAINS,

    INSTANCE_PROBLEM_MINCONTAINS,
    INSTANCE_PROBLEM_MAXCONTAINS,

    INSTANCE_PROBLEM_REDUNDANT_ITEM,
    INSTANCE_PROBLEM_NOT_UNIQUEITEMS,

    INSTANCE_PROBLEM_ARRAY_EMPTY,

    INSTANCE_PROBLEM_REQUIRED,
    INSTANCE_PROBLEM_MAXPROPERTIES,
    INSTANCE_PROBLEM_MINPROPERTIES,
    INSTANCE_PROBLEM_DEPENDENCIES,

    INSTANCE_PROBLEM_REDUNDANT_PROPERTY,

    INSTANCE_PROBLEM_NOT_REQUIRED,
    INSTANCE_PROBLEM_NOT_REQUIRED_PLURAL,
    INSTANCE_PROBLEM_NOT_DEPENDENCIES,
    INSTANCE_PROBLEM_NOT_DEPENDENCIES_PLURAL,

    INSTANCE_PROBLEM_OBJECT_EMPTY,
    INSTANCE_PROBLEM_OBJECT_NONEMPTY,

    INSTANCE_PROBLEM_ANYOF,
    INSTANCE_PROBLEM_ONEOF_FEW,
    INSTANCE_PROBLEM_ONEOF_MANY,

    INSTANCE_PROBLEM_FORMAT,
    INSTANCE_PROBLEM_NOT_FORMAT,

    INSTANCE_PROBLEM_CONTENTENCODING,
    INSTANCE_PROBLEM_NOT_CONTENTENCODING,
    INSTANCE_PROBLEM_CONTENTMEDIATYPE,
    INSTANCE_PROBLEM_NOT_CONTENTMEDIATYPE,

    INSTANCE_PROBLEM_NOT_FOUND,
    INSTANCE_PROBLEM_UNKNOWN,

    /* Problem messages for schema validation */

    SCHEMA_PROBLEM_NOT_FOUND,
    SCHEMA_PROBLEM_EMPTY,
    SCHEMA_PROBLEM_EOI,
    SCHEMA_PROBLEM_REFERENCE,
    SCHEMA_PROBLEM_REFERENCE_LOOP,
    SCHEMA_PROBLEM_KEYWORD_UNKNOWN,
    SCHEMA_PROBLEM_FORMAT_UNKNOWN,
    SCHEMA_PROBLEM_CONTENTMEDIATYPE_INVALID,

    /* Problem messages for JSON parser */

    PARSER_UNEXPECTED_EOI,
    PARSER_INVALID_TOKEN,

    /* Problem messages for JSON reader */

    READER_UNEXPECTED_EOI,
    READER_READ_ERROR,
    ;

    private static final String BUNDLE_BASE_NAME = "org/leadpony/justify/internal/messages";

    @Override
    public String getLocalized(Locale locale) {
        return getBundle(locale).getString(name());
    }

    public String format(Map<String, Object> arguments) {
        return format(arguments, Locale.getDefault());
    }

    public String format(Map<String, Object> arguments, Locale locale) {
        ResourceBundle bundle = getBundle(locale);
        String pattern = bundle.getString(name());
        MessageFormatter formatter = new MessageFormatter(pattern, bundle);
        return formatter.format(arguments);
    }

    private static ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, Message.class.getClassLoader());
    }
}
