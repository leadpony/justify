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
package org.leadpony.justify.internal.schema.io;

import javax.json.stream.JsonLocation;

import org.leadpony.justify.internal.schema.SchemaReference;

/**
 * A class holding a schema reference and its context.
 *
 * @author leadpony
 */
class ReferenceContext {

    private final SchemaReference reference;
    private final JsonLocation location;
    private final String pointer;

    ReferenceContext(SchemaReference reference, JsonLocation location, String pointer) {
        this.reference = reference;
        this.location = location;
        this.pointer = pointer;
    }

    SchemaReference getReference() {
        return reference;
    }

    JsonLocation getLocation() {
        return location;
    }

    String getPointer() {
        return pointer;
    }
}