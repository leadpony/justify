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
package org.leadpony.justify.api;

import java.net.URI;

/**
 * The versions of the JSON Schema Specification.
 *
 * @author leadpony
 */
public enum SpecVersion {
    /**
     * Draft-06.
     */
    DRAFT_06("http://json-schema.org/draft-06/schema#"),
    /**
     * Draft-07.
     */
    DRAFT_07("http://json-schema.org/draft-07/schema#")
    ;

    private final URI id;

    private SpecVersion(String id) {
        this.id = URI.create(id);
    }

    /**
     * Returns the identifier of this version.
     *
     * @return the identifier of this version, never be {@code null}.
     */
    public URI id() {
        return id;
    }

    /**
     * Returns the lastest version.
     *
     * @return the lastest version.
     */
    public static SpecVersion latest() {
        return DRAFT_07;
    }
}
