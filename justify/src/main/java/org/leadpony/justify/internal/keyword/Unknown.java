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
package org.leadpony.justify.internal.keyword;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

/**
 * An unrecognized keyword.
 *
 * @author leadpony
 */
public class Unknown extends AbstractKeyword {

    private final String name;
    private final JsonValue value;

    /**
     * Constructs this keyword.
     *
     * @param name the name of this keyword.
     * @param value the value of this keyword.
     */
    public Unknown(String name, JsonValue value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public JsonValue getValueAsJson(JsonProvider jsonProvider) {
        return value;
    }
}
