/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.api.keyword;

/**
 * An exception thrown if the keyword is invalid.
 *
 * @author leadpony
 * @since 4.0
 */
public class InvalidKeywordException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Construsts this exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public InvalidKeywordException(String message) {
        super(message);
    }

    /**
     * Construsts this exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause of this exception.
     */
    public InvalidKeywordException(String message, Throwable cause) {
        super(message, cause);
    }
}
