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
 * A keyword type providing a single value as metadata.
 *
 * @param <T> the type of the metadata value.
 *
 * @author leadpony
 */
public abstract class AbstractMetadataKeyword<T> extends AbstractKeyword {

    private final T value;

    protected AbstractMetadataKeyword(T value) {
        this.value = value;
    }

    public final T value() {
        return value;
    }

    @Override
    public JsonValue getValueAsJson(JsonProvider jsonProvider) {
        return jsonProvider.createValue(value().toString());
    }
}
