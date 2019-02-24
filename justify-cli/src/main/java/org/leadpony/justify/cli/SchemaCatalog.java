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
package org.leadpony.justify.cli;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaResolver;

/**
 * A schema catalog.
 *
 * @author leadpony
 */
abstract class SchemaCatalog extends HashMap<URI, Location> implements JsonSchemaResolver {

    private static final long serialVersionUID = 1L;

    private final Map<URI, JsonSchema> cache = new HashMap<>();

    @Override
    public JsonSchema resolveSchema(URI id) {
        String fragment = id.getFragment();
        URI baseId = withoutFragment(id);
        JsonSchema schema = findRootSchema(baseId);
        if (schema == null) {
            return null;
        }
        if (fragment == null) {
            return schema;
        } else {
            return schema.getSubschemaAt(fragment);
        }
    }

    private JsonSchema findRootSchema(URI id) {
        JsonSchema schema = cache.get(id);
        if (schema != null) {
            return schema;
        }
        Location resource = get(id);
        if (resource == null) {
            return null;
        }
        schema = readReferencedSchema(resource);
        if (schema != null) {
            cache.put(id, schema);
            return schema;
        } else {
            return null;
        }
    }

    private static URI withoutFragment(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
        } catch (URISyntaxException e) {
            return uri;
        }
    }

    protected abstract JsonSchema readReferencedSchema(Location location);
}
