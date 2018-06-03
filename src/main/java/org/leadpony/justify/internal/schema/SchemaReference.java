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

import java.net.URI;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;

/**
 * @author leadpony
 */
public class SchemaReference implements JsonSchema {
    
    private final URI ref;
    private JsonSchema referencedSchema;
    
    SchemaReference(URI ref) {
        this.ref = ref;
    }
    
    public URI ref() {
        return ref;
    }
    
    public void setReferencedSchema(JsonSchema schema) {
        this.referencedSchema = schema;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        if (referencedSchema == null) {
            return null;
        }
        return referencedSchema.createEvaluator(type);
    }

    @Override
    public JsonSchema negate() {
        if (referencedSchema == null) {
            return null;
        }
        return referencedSchema.negate();
    }

    @Override
    public void toJson(JsonGenerator generator) {
        generator.writeStartObject();
        generator.write("$ref", ref.toString());
        generator.writeEnd();
    }

    @Override
    public String toString() {
        return JsonSchemas.toString(this);
    }
}
