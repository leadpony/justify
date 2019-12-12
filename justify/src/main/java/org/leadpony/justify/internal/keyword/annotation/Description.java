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

package org.leadpony.justify.internal.keyword.annotation;

import javax.json.JsonValue;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.AbstractMetadataKeyword;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * An annotation keyword representing "title".
 *
 * @author leadpony
 */
@KeywordType("description")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
public class Description extends AbstractMetadataKeyword<String> implements Annotation<String> {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromString mapper = Description::new;
        return mapper;
    }

    public Description(JsonValue json, String value) {
        super(json, value);
    }
}
