/*
 * Copyright 2018-2020 the Justify authors.
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
package org.leadpony.justify.internal.keyword;

import jakarta.json.JsonValue;

/**
 * An unrecognized keyword.
 *
 * @author leadpony
 */
public class UnknownKeyword extends AbstractKeyword {

    private final String name;

    /**
     * Constructs this keyword.
     *
     * @param name the name of this keyword.
     * @param value the value of this keyword.
     */
    public UnknownKeyword(String name, JsonValue value) {
        super(value);
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }
}
