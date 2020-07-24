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
package org.leadpony.justify.internal.schema.io;

import java.net.URI;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.JsonSchemaReference;

/**
 * @author leadpony
 */
final class DeferredJsonSchemaReference implements JsonSchemaReference {

    private final URI targetId;
    private final URI resolvedTargetId;
    private final String jsonPointer;

    private JsonSchema referencedSchema;

    DeferredJsonSchemaReference(URI baseUri, URI targetId, String jsonPointer) {
        this.targetId = targetId;
        this.resolvedTargetId = resolveTargetId(baseUri, targetId);
        this.jsonPointer = jsonPointer;
    }

    @Override
    public URI getTargetId() {
        return targetId;
    }

    @Override
    public URI getResolvedTargetId() {
        return resolvedTargetId;
    }

    @Override
    public JsonSchema getTargetSchema() {
        if (referencedSchema == null) {
            throw new IllegalStateException("No referenced schema");
        }
        return referencedSchema;
    }

    void setReferencedSchema(JsonSchema schema) {
        this.referencedSchema = schema;
    }

    String getPointer() {
        return jsonPointer;
    }

    private static URI resolveTargetId(URI baseUri, URI targetId) {
        if (targetId.isAbsolute()) {
            return targetId;
        } else {
            return baseUri.resolve(targetId);
        }
    }
}
