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
package org.leadpony.justify.internal.keyword.content;

import jakarta.json.JsonValue;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.keyword.AbstractKeyword;

/**
 * Content encoding with an unknown scheme.
 *
 * @author leadpony
 */
@KeywordClass("contentEncoding")
public class UnknownContentEncoding extends AbstractKeyword {

    public UnknownContentEncoding(JsonValue json, String scheme) {
        super(json);
    }
}
