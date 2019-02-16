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
 * The status of commands.
 *
 * @author leadpony
 */
enum Status {
    /**
     * All of the schemas and instances turn to be valid.
     */
    VALID(0),
    /**
     * Any of the schemas and instances turns to be invalid.
     */
    INVALID(1),
    /**
     * Program stopped unexpectedly.
     */
    FAILED(2);

    private final int code;

    private Status(int code) {
        this.code = code;
    }

    /**
     * Returns the code of this status.
     *
     * @return the code of this status.
     */
    public int code() {
        return code;
    }
}
