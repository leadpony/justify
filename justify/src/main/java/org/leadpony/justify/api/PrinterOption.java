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
package org.leadpony.justify.api;

/**
 * Options for problem printers.
 * <p>
 * These options can be used in {@link JsonValidationService} interface.
 * </p>
 *
 * @author leadpony
 */
public enum PrinterOption {
    /**
     * Includes line and column numbers in the instance.
     */
    INCLUDE_LOCATION,
    /**
     * Includes a JSON pointer in the instance.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6901">
     *      "JavaScript Object Notation (JSON) Pointer", RFC 6901</a>
     */
    INCLUDE_POINTER
}
