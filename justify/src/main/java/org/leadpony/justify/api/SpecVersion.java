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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The versions of the JSON Schema Specification.
 *
 * @author leadpony
 */
public enum SpecVersion {
    /**
     * JSON Schema specification of Draft-04.
     */
    DRAFT_04("http://json-schema.org/draft-04/schema#"),
    /**
     * JSON Schema specification of Draft-06.
     */
    DRAFT_06("http://json-schema.org/draft-06/schema#"),
    /**
     * JSON Schema specification of Draft-07.
     */
    DRAFT_07("http://json-schema.org/draft-07/schema#");

    private static final Map<URI, SpecVersion> VERSION_MAP = Stream.of(SpecVersion.values())
            .collect(Collectors.toMap(SpecVersion::id, Function.identity()));

    private final URI id;

    SpecVersion(String id) {
        this.id = URI.create(id);
    }

    /**
     * Returns the identifier of this version, such as
     * {@code "http://json-schema.org/draft-07/schema#"}.
     *
     * @return the identifier of this version, never be {@code null}.
     */
    public URI id() {
        return id;
    }

    /**
     * Returns the current stable version.
     *
     * @return the current stable version.
     */
    public static SpecVersion current() {
        return DRAFT_07;
    }

    /**
     * Returns the version of the specified ID.
     *
     * @param id the identifier of the version, such as
     *           {@code "http://json-schema.org/draft-07/schema#"}.
     * @return found version, never be {@code null}.
     * @throws IllegalArgumentException if this enum type has no constant with the
     *                                  specified identifier.
     * @throws NullPointerException     if the specified {@code id} is {@code null}.
     *
     */
    public static SpecVersion getById(URI id) {
        Objects.requireNonNull(id, "id must not be null.");
        SpecVersion version = VERSION_MAP.get(id);
        if (version == null) {
            throw new IllegalArgumentException();
        }
        return version;
    }
}
