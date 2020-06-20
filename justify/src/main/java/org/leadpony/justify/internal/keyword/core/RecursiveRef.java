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
package org.leadpony.justify.internal.keyword.core;

import java.net.URI;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.KeywordTypes;

import jakarta.json.JsonValue;

/**
 * A keyword representing "$recursiveRef" keyword.
 *
 * @author leadpony
 */
@Spec(SpecVersion.DRAFT_2019_09)
public class RecursiveRef extends Ref {

    static final KeywordType TYPE = KeywordTypes.mappingUri("$recursiveRef", Ref::new);

    public RecursiveRef(JsonValue json, URI value) {
        super(json, value);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }
}