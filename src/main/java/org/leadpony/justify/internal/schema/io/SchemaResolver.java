/*
 * Copyright 2018 the Justify authors.
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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.stream.JsonLocation;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.schema.Resolvable;
import org.leadpony.justify.internal.schema.SchemaReference;

/**
 * This class resolves the relative URIs specified as values of "id" or "$ref" keyword.
 *  
 * @author leadpony
 */
class SchemaResolver {

    private static final Logger log = Logger.getLogger(SchemaResolver.class.getName());
    
    private final Map<String, JsonSchema> identified = new HashMap<>();
    private final Map<SchemaReference, JsonLocation> references = new IdentityHashMap<>();
    
    private final Entry head;
    private Entry tail;
    
    SchemaResolver() {
        this.head = this.tail = new Entry();
    }
    
    void addExternalSchema(URI id, JsonSchema schema) {
        addFullyIdentifiedSchema(id, schema);
    }
    
    Entry lastEntry() {
        assert tail.next == null;
        return tail;
    }
    
    void addIdentifiedSchema(JsonSchema schema, Entry previous) {
        Entry entry = new Identified(schema, previous.next);
        previous.next = entry;
        tail = entry;
    }
    
    void addReference(SchemaReference reference, JsonLocation location) {
        if (!reference.ref().isAbsolute()) {
            Entry entry = new Reference(reference);
            tail.next = entry;
            tail = entry;
        }
        this.references.put(reference, location);
    }
    
    SchemaResolver resolveAll(URI baseURI) {
        if (this.head.next != null) {
            resolveAll(baseURI, this.head.next);
            this.head.next = null;
        }
        return this;
    }
    
    void linkAll() {
        for (SchemaReference reference : this.references.keySet()) {
            URI ref = reference.ref();
            JsonSchema schema = findReferencedSchema(ref);
            if (schema != null) {
                reference.setReferencedSchema(schema);
            } else {
                // TODO:
                log.severe("$ref target was not found: " + ref.toString());
            }
        }
    }
    
    private static void resolveAll(URI baseURI, Entry first) {
        Entry entry = first;
        while (entry != null) {
            entry.resolveURI(baseURI);
            entry = entry.next;
        }
    }
    
    private void addFullyIdentifiedSchema(URI id, JsonSchema schema) {
        String uri = id.toString();
        if (uri.endsWith("#")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        this.identified.put(uri, schema);
    }
    
    private JsonSchema findReferencedSchema(URI ref) {
        String fragment = ref.getFragment();
        if (fragment != null && (fragment.isEmpty() || fragment.startsWith("/"))) {
            String[] parts = ref.toString().split("#", -1);
            JsonSchema schema = identified.get(parts[0]);
            if (schema == null || parts.length < 2) {
                return schema;
            } else {
                return schema.getSchema(fragment);
            }
        } else {
            return identified.get(ref.toString());
        }
    }

    static class Entry {
        
        private Entry next;
        
        URI resolveURI(URI baseURI) {
            return getResolvable().resolve(baseURI);
        }
        
        protected Resolvable getResolvable() {
            return null;
        }
    }
    
    private static class Reference extends Entry {
        
        private final SchemaReference reference;
        
        Reference(SchemaReference reference) {
            this.reference = reference;
        }
        
        protected Resolvable getResolvable() {
            return reference;
        }
    }
    
    private class Identified extends Entry {

        private final JsonSchema schema;
        private final Entry descendant;
        
        Identified(JsonSchema schema, Entry firstDescendant) {
            this.schema = schema;
            this.descendant = firstDescendant;
        }
        
        @Override
        URI resolveURI(URI baseURI) {
            if (schema.hasId()) {
                baseURI = super.resolveURI(baseURI);
            }
            addFullyIdentifiedSchema(baseURI, schema);
            resolveAll(baseURI, this.descendant);
            return baseURI;
        }
        
        protected Resolvable getResolvable() {
            return (Resolvable)schema;
        }
    }
}
