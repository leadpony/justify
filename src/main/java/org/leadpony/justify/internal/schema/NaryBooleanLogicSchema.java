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

package org.leadpony.justify.internal.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.JsonSchema;

/**
 * @author leadpony
 */
public abstract class NaryBooleanLogicSchema extends BooleanLogicSchema {

    protected final List<JsonSchema> subschemas;

    /**
     * @param subschemas
     */
    protected NaryBooleanLogicSchema(Collection<JsonSchema> subschemas) {
        this.subschemas = new ArrayList<>(subschemas);
    }

    protected void toJson(JsonGenerator generator, String name) {
        generator.writeKey(name);
        generator.writeStartArray();
        this.subschemas.forEach(s->s.toJson(generator));
        generator.writeEnd();
    }
}
