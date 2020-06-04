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

package org.leadpony.justify.internal.keyword.applicator;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * "Then" conditional keyword.
 *
 * @author leadpony
 */
@KeywordClass("then")
@Spec(SpecVersion.DRAFT_07)
public class Then extends Conditional {

    public static final KeywordType TYPE = KeywordTypes.mappingSchema("then", Then::new);

    public Then(JsonValue json, JsonSchema schema) {
        super(schema);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean canEvaluate() {
        return false;
    }
}
