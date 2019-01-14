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
 * The type which resolves external JSON schemas.
 * 
 * @author leadpony
 */
public interface JsonSchemaResolver {

    /**
     * Returns the JSON schema identified with given {@code id}.
     * 
     * @param id the identifier of the schema to return.
     * @return the schema if found, or {@code null} if not found.
     * @throws NullPointerException if {@code id} is {@code null}.
     */
    JsonSchema resolveSchema(URI id);
}
