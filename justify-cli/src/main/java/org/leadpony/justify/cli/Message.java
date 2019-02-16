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

import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

/**
 * Messages to be presented to users.
 *
 * @author leadpony
 */
enum Message {
    VALIDATE_SCHEMA,
    VALIDATE_REFERENCED_SCHEMA,
    VALIDATE_INSTANCE,
    INSPECT_SCHEMA,

    SCHEMA_ID_MISSING,
    SCHEMA_ID_INVALID,

    SCHEMA_VALID,
    SCHEMA_INVALID,
    INSTANCE_VALID,
    INSTANCE_INVALID,

    OPTION_UNRECOGNIZED,
    OPTION_MISSING,
    OPTION_ARGUMENT_MISSING,
    OPTION_ARGUMENT_INVALID,

    SCHEMA_NOT_FOUND,
    INSTANCE_NOT_FOUND,
    SCHEMA_MALFORMED,
    INSTANCE_MALFORMED,

    ACCESS_FAILED,

    WARNING
    ;

    private static final String BUNDLE_NAME = Message.class.getPackage().getName() + ".messages";
    private static final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Returns this message as a string.
     *
     * @return this message as a string.
     */
    public String get() {
        return bundle.getString(name());
    }

    /**
     * Returns this message as a string formatted with the specified arguments.
     *
     * @param arguments the arguments to format the message.
     * @return this message as a formatted string.
     */
    public String get(Object... arguments) {
        return MessageFormat.format(get(), arguments);
    }
}
