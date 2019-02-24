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

/**
 * An exception thrown by {@link Command}.
 *
 * @author leadpony
 */
class CommandException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    CommandException(Message message, Object... arguments) {
        super(message.format(arguments));
    }

    CommandException(Throwable cause) {
        super(cause.getLocalizedMessage(), cause);
    }

    CommandException(Message message, Throwable cause) {
        super(message.format(cause.getLocalizedMessage()), cause);
    }
}
