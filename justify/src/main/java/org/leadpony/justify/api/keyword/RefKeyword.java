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
package org.leadpony.justify.api.keyword;

import java.net.URI;

import org.leadpony.justify.api.JsonSchema;

/**
 * A special keyword for referencing another JSON schema.
 *
 * @author leadpony
 * @since 4.0
 */
public interface RefKeyword extends EvaluationKeyword, SimpleValueKeyword<URI> {

    /**
     * Returns the JSON schema reference.
     *
     * @return the JSON schema reference, never be {@code null}.
     */
    JsonSchemaReference getSchemaReference();

    /**
     * Returns the JSON schema referenced directly.
     *
     * @return the JSON schema referenced directly.
     */
    default JsonSchema getTargetSchema() {
        return getSchemaReference().getTargetSchema();
    }
}
