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

package org.leadpony.justify.internal.schema;

import java.net.URI;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaBuilder;

/**
 * A JSON schema builder interface based on JSON Schema Specification Draft-07.
 *
 * @author leadpony
 */
public interface Draft07SchemaBuilder extends JsonSchemaBuilder {

    /**
     * Adds the "$ref" keyword to the schema.
     *
     * @param ref the URI of the referenced schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code ref} is {@code null}.
     */
    JsonSchemaBuilder withRef(URI ref);

    /**
     * Adds an unknown property to the schema.
     *
     * @param name      the name of the unknown property.
     * @param subschema the subschema defined for the property.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} of
     *                              {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withUnknown(String name, JsonSchema subschema);
}
