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

package org.leadpony.justify.internal.keyword.applicator;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.ApplicatorKeyword;
import org.leadpony.justify.internal.keyword.AbstractKeyword;

/**
 * A keyword which applies subschemas.
 *
 * @author leadpony
 */
abstract class AbstractApplicatorKeyword extends AbstractKeyword implements ApplicatorKeyword {

    /**
     * Constructs this keyword as an applicator.
     *
     * @param json the JSON representation of this keyword.
     */
    protected AbstractApplicatorKeyword(JsonValue json) {
        super(json);
    }
}
