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
package org.leadpony.justify.internal.keyword.core;

import java.net.URI;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.AbstractMetadataKeyword;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * A keyword type representing "$id" keyword.
 *
 * @author leadpony
 */
@KeywordType("$id")
@Spec(value = SpecVersion.DRAFT_04, name = "id")
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Id extends AbstractMetadataKeyword<URI> {

    private final String name;

    public static KeywordMapper mapper(String name) {
        KeywordMapper.FromUri mapper = value -> new Id(name, value);
        return mapper;
    }

    public Id(URI value) {
        this("$id", value);
    }

    public Id(String name, URI value) {
        super(value);
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }
}
