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
package org.leadpony.justify.internal.schema;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaVisitor;
import org.leadpony.justify.internal.base.URIs;

/**
 * @author leadpony
 */
public class IdentifiedJsonSchemaCollector implements JsonSchemaVisitor {

    private final Map<URI, JsonSchema> schemas;

    private Scope currentScope;
    private final Deque<Scope> outerScopes;

    public IdentifiedJsonSchemaCollector(URI baseUri, Map<URI, JsonSchema> schemas) {
        this.schemas = schemas;
        this.currentScope = new Scope(baseUri);
        this.outerScopes = new ArrayDeque<>();
    }

    @Override
    public Result visitSchema(JsonSchema schema, String pointer) {
        if (schema.hasId()) {
            pushIdentifiedSchema(schema);
        }

        schema.getAnchor().ifPresent(anchor -> {
            processSchemaWithAnchor(schema, anchor);
        });

        return Result.CONTINUE;
    }

    @Override
    public Result leaveSchema(JsonSchema schema, String pointer) {
        if (schema == currentScope.schema) {
            popIdentifiedSchema();
        }
        return Result.CONTINUE;
    }

    private void pushIdentifiedSchema(JsonSchema schema) {
        URI id = schema.id();
        URI baseUri = id.isAbsolute() ? id : getCurrentBaseUri().resolve(id);
        outerScopes.addLast(currentScope);
        currentScope = new Scope(baseUri, schema);
        addIdentifiedSchema(baseUri, schema);
    }

    private void popIdentifiedSchema() {
        if (!outerScopes.isEmpty()) {
            currentScope = outerScopes.removeLast();
        }
    }

    private void processSchemaWithAnchor(JsonSchema schema, String anchor) {
        URI id = getCurrentBaseUri().resolve("#" + anchor);
        addIdentifiedSchema(id, schema);
    }

    private void addIdentifiedSchema(URI id, JsonSchema schema) {
        schemas.put(URIs.removeEmptyFragment(id), schema);
    }

    private URI getCurrentBaseUri() {
        return currentScope.baseUri;
    }

    static class Scope {
        final URI baseUri;
        final JsonSchema schema;

        Scope(URI baseUri) {
            this(baseUri, null);
        }

        Scope(URI baseUri, JsonSchema schema) {
            this.baseUri = baseUri;
            this.schema = schema;
        }
    }
}
