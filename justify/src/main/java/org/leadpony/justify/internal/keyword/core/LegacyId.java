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
package org.leadpony.justify.internal.keyword.core;

import java.net.URI;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.AbstractKeyword;
import org.leadpony.justify.internal.keyword.IdKeyword;
import org.leadpony.justify.internal.keyword.KeywordTypes;

/**
 * A keyword type representing "id" keyword defined by the draft-04.
 *
 * @author leadpony
 */
@KeywordClass("id")
@Spec(SpecVersion.DRAFT_04)
public class LegacyId extends AbstractKeyword implements IdKeyword {

    static final KeywordType TYPE = KeywordTypes.mappingUri("id", LegacyId::new);

    private final URI value;

    protected LegacyId(JsonValue json, URI value) {
        super(json);
        this.value = value;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public URI value() {
        return value;
    }
}
