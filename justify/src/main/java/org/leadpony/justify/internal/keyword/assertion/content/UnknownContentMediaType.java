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
package org.leadpony.justify.internal.keyword.assertion.content;

import jakarta.json.JsonValue;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.keyword.AbstractKeyword;

/**
 * Content media type with an unknown value.
 *
 * @author leadpony
 */
@KeywordClass("contentMediaType")
public class UnknownContentMediaType extends AbstractKeyword {

    /**
     * Constructs this object.
     *
     * @param json the original JSON value.
     * @param value the media type value which may include additional parameters.
     */
    public UnknownContentMediaType(JsonValue json, String value) {
        super(json);
    }
}
